# ZK Authorization: MVC & MVVM Patterns

## The Core Problem

ZK's `/zkau` endpoint is a single HTTP entry point — URL-based Spring Security rules (e.g. `requestMatchers("/zkau").hasRole(...)`) can only gate the entire endpoint, not individual event handlers or commands. Every `onClick`, `@Listen`, or `@Command` handler is independently reachable from the browser and must be individually authorized.

---

## Recommended Architecture: Two-Layer Defense

```
Browser → /zkau → [1] Controller/ViewModel layer (fast fail, visible to developers)
                    ↓
               [2] Service layer (@PreAuthorize — the real security gate)
```

**Layer 1** (controller/ViewModel) is a developer-facing guard that provides clear error semantics at the boundary closest to the UI. **Layer 2** is mandatory defense-in-depth — it works even if layer 1 is forgotten, miscoded, or bypassed by future refactors.

---

## The CGLIB Proxy Problem

When any Spring-managed bean has a method annotated with `@PreAuthorize` (or any Spring AOP advice), Spring Security wraps it in a **CGLIB proxy**. This breaks ZK annotation scanning in different ways depending on the ZK version.

### How ZK 10 scans ViewModel methods: `getMethods()` not `getDeclaredMethods()`

Decompiling `zkbind-10.3.0.1-jakarta.jar` (`BinderImpl.class`) confirms ZK 10 uses `Class.getMethods()`:

```
// BinderImpl bytecode (javap output)
invokevirtual #129  // Method java/lang/Class.getMethods:()[Ljava/lang/reflect/Method;
```

`getMethods()` returns all public methods including those inherited from superclasses. This means ZK 10 **can** find annotations on parent class methods even through a CGLIB proxy — but only partially.

### What CGLIB does to overridden methods

When Spring creates a CGLIB proxy of `BigbankViewModel` (because `adjustBalance()` has `@RequiresRole` triggering our aspect):

```
BigbankViewModel$$SpringCGLIB$$0  extends  BigbankViewModel
  └── adjustBalance(Long, Double)   ← overridden for AOP interception
        method-level annotations:   [@Command]           ← Spring CGLIB *copies* these ✓
        parameter annotations:      [nothing, nothing]   ← CGLIB does NOT copy these ✗
```

Spring 5+ CGLIB copies method-level annotations to the proxy's generated overrides (Spring needs this for its own annotation scanning). But it **does not copy parameter annotations** — this is a bytecode generation limitation; parameter annotations live in a separate JVM attribute (`RuntimeVisibleParameterAnnotations`) that CGLIB's ASM-generated code does not reproduce.

### Net result for ZK 10

| ZK annotation | Mechanism | Through proxy? |
|---|---|---|
| `@Command` (method-level) | `getMethods()` → proxy's override → annotation copied by CGLIB | **Yes** — command IS invoked |
| `@BindingParam` (parameter-level) | `method.getParameterAnnotations()` → proxy's override → not copied | **No** — parameters arrive as `null` |
| `@Init` (method-level) | `getMethods()` → found on parent class | **Yes** — but moot: we use `@PostConstruct` |

**Conclusion:** Putting any Spring AOP advice on a ViewModel method makes `@BindingParam` parameters null. The command fires but receives no data.

### Can CGLIB be made to copy parameter annotations?

No. It is a bytecode generation limitation, not a Spring configuration option. CGLIB generates proxy method signatures from the parameter type descriptors only; it does not read or reproduce `RuntimeVisibleParameterAnnotations`. Spring has no API to change this behaviour.

Using `AopUtils.getTargetClass()` also does not help: it returns the original class, which is useful for *our* code to look up annotations — but ZK reads parameter annotations from the `Method` object it already holds (the proxy's override), before our aspect runs.

---

## Solution: Delegate to a Dedicated Security Service

The only approach that avoids CGLIB proxying the ViewModel is to keep `@PreAuthorize` off it entirely. Delegate to a separate singleton `@Service` that Spring proxies safely (no ZK annotations involved).

### MVVM Pattern

```java
// Security gate — singleton, Spring proxies this safely
@Service
public class BigbankSecurityService {

    @PreAuthorize("hasRole('ROLE_SUPERVISOR') or hasRole('ROLE_TELLER')")
    public void assertCanAdjustBalance() { }
}

// ViewModel — no @PreAuthorize, never CGLIB-proxied
@Component
@Scope("prototype")
public class BigbankViewModel {

    @Autowired private BankService bankService;
    @Autowired private BigbankSecurityService securityService;

    private ListModelList<Account> accounts;

    @PostConstruct                    // Spring lifecycle — runs before proxy creation
    public void init() {
        accounts = new ListModelList<>(bankService.findAccounts());
    }

    @Command
    public void adjustBalance(@BindingParam("accountId") Long id,
                              @BindingParam("amount") Double amount) {
        securityService.assertCanAdjustBalance();   // throws AccessDeniedException if denied
        final Account account = bankService.readAccount(id);
        account.setBalance(bankService.post(account, amount).getBalance());
        BindUtils.postNotifyChange(null, null, account, "balance");
    }

    public ListModelList<Account> getAccounts() { return accounts; }
}
```

**Why `@PostConstruct` instead of `@Init`:** Spring calls `@PostConstruct` on the raw target object during construction, before the proxy is created. ZK later receives the proxy but `getAccounts()` delegates to the already-initialised target.

**ZUL — reference bean by Spring name:**
```xml
<?variable-resolver class="org.zkoss.zkplus.spring.DelegatingVariableResolver"?>
<window viewModel="@id('vm') @init(bigbankViewModel)">
```

### MVC Pattern (SelectorComposer)

Composers in this project are already Spring-managed (`@Component @Scope("desktop")`). Same principle: no `@PreAuthorize` on the Composer itself, delegate to the security service.

```java
@Component
@Scope("desktop")
public class BigbankController extends SelectorComposer<Component> {

    @WireVariable
    private BankService bankService;

    @Autowired
    private BigbankSecurityService securityService;

    @Listen("onClick = #adjustBtn")
    public void onAdjust() {
        securityService.assertCanAdjustBalance();
        // ... business logic
    }
}
```

**Handling `AccessDeniedException`:** The existing `ErrorHandlingController` already catches `AccessDeniedException`. No additional error handling needed.

---

## Comparison: MVC vs MVVM

| Concern | MVC (SelectorComposer) | MVVM (ViewModel) |
|---|---|---|
| Spring management | `@Component @Scope("desktop")` | `@Component @Scope("prototype")` |
| Initialisation | `doAfterCompose()` or `@Listen("onCreate")` | `@PostConstruct` (not `@Init`) |
| Authorization guard | `securityService.assertXxx()` first line of `@Listen` handler | `securityService.assertXxx()` first line of `@Command` method |
| Error propagation | `AccessDeniedException` → `ErrorHandlingController` | `AccessDeniedException` → ZK global error handler |
| Direct `@PreAuthorize` on handler? | **No** | **No** |

---

## Tested: Spring AOP Aspect with `@RequiresRole`

To avoid the 1-line-per-handler explicit call, a declarative annotation approach was implemented and tested.

### What was built

- `RequiresRole.java` — custom method annotation
- `ZkAuthorizationAspect.java` — `@Aspect @Component` intercepting `@annotation(requiresRole)`
- `@EnableAspectJAutoProxy` added to `ApplicationConfig`
- `aspectjweaver 1.9.22` added to `pom.xml`
- `BigbankViewModel.adjustBalance()` annotated with `@RequiresRole({"ROLE_SUPERVISOR", "ROLE_TELLER"})`, explicit service call removed

### Test result

| Behaviour | Expected | Actual |
|---|---|---|
| Application starts | ✓ | ✓ |
| Accounts grid loads | ✓ | ✓ |
| `adjustBalance` command fires | ✓ | ✓ (`@Command` found via `getMethods()`) |
| `id` and `amount` parameters | non-null | **null** — `@BindingParam` stripped by CGLIB |

### Root cause (confirmed)

Spring CGLIB copies method-level annotations to proxy overrides. It does **not** copy parameter annotations. ZK reads `@BindingParam` from `method.getParameterAnnotations()` on the proxy's override → empty array → both params null.

The parameter annotation loss happens during ZK's binding step, before our aspect runs. There is no hook in standard Spring AOP or ZK to prevent this.

### Current state

`BigbankViewModel` has been reverted to the explicit delegate call. `RequiresRole.java` and `ZkAuthorizationAspect.java` remain in `app/security/` — they work correctly for MVC Composers (whose `@Listen` methods have no parameter annotations to lose).

---

## If Truly Zero-Boilerplate Annotation Is Required: AspectJ CTW

AspectJ **Compile-Time Weaving** injects advice directly into bytecode at build time. No proxy class is generated; ZK sees the original class with all annotations intact.

### pom.xml changes

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.15.0</version>
    <configuration>
        <complianceLevel>17</complianceLevel>
        <source>17</source>
        <target>17</target>
    </configuration>
    <executions>
        <execution><goals><goal>compile</goal></goals></execution>
    </executions>
</plugin>
```

With CTW, `@RequiresRole` on a ViewModel method works correctly — ZK sees the original class, all ZK annotations intact, the advice runs before the method body. **Trade-off:** requires AspectJ Maven plugin; slightly longer build.

---

## Summary: What Works vs. What Doesn't

| Approach | `@Command` found | `@BindingParam` intact | Zero boilerplate | Notes |
|---|---|---|---|---|
| `securityService.assertXxx()` explicit call | ✓ | ✓ | ✗ | **Recommended** |
| `@PreAuthorize` directly on ViewModel method | ✓ | ✗ | ✗ | param annotations lost |
| `@RequiresRole` + Spring AOP aspect (runtime) | ✓ | ✗ | ✓ | param annotations lost — **tested & confirmed broken** |
| `@RequiresRole` + AspectJ CTW (compile-time) | ✓ | ✓ | ✓ | requires build change |

## Recommended Approach

1. **Service-layer `@PreAuthorize`** (already on `BankService.post()`) — the non-negotiable security gate.
2. **Delegate service pattern** (`securityService.assertXxx()`) at the controller/ViewModel layer — explicit, readable, proven to work.
3. **AspectJ CTW** only if the project is large enough that explicit calls become a maintenance burden.
