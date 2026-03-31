# How to run with maven
`mvn jetty:run`

visit http://localhost:8080/zkspringcoresec/


# Spring Security Use Cases 
This project plans to demonstrate the following use cases:

## login page with zul
* Users login with a form submission to avoid sending AU requests. Because /zkau is secured.
* require DHtmlResourceServlet (available since 8.6.2) to make zul page loading correctly if all URL are secured. 
* browser still can get zk resources including *.wpd, *.wcs, and zk bundled images. 

## Role-base permission control
### supervisor
* access extreme secure page
* access secure page
* change the account balance 
### teller
* access secure page
* change the account balance
### user
* access secure page


# Secure pages
* [extreme secure page](src/main/webapp/secure/extreme/index.zul) 
* [secure page](src/main/webapp/secure/index.zul)

# Operation permission control
* `org.zkoss.zkspringessentials.bigbank.BankService`

## Controller-Layer Authorization (Two Approaches)

Securing only the service layer (`@PreAuthorize` on `BankService`) is the minimum. Adding a guard at the controller/ViewModel layer provides a clear, early-fail boundary that produces actionable error messages before any business logic runs.

### Why authorize at the controller layer?

- **Fail fast** — the exception is raised at the UI boundary, where error feedback is most meaningful
- **No rollback needed** — a controller handler often preprocesses inputs (parsing, validation, loading related objects) before calling the service. If the authorization check only exists at the service layer, that preprocessing has already run when the exception is thrown; with no transaction wrapping it, there is nothing to undo automatically. Checking authorization first at the controller avoids this problem entirely.
- **Defense in depth** — even if a service method is refactored or a new call path is added, the controller guard still fires
- **Readable intent** — the policy is visible alongside the UI code, not buried in a service

### Approach 1 — Spring AOP (CGLIB proxy) + `@RequiresRole`

Works for **MVC Composers** (`SelectorComposer`) where event handlers receive a `MouseEvent` parameter. CGLIB copies method-level annotations (`@Listen`, `@RequiresRole`) to its generated proxy, and no parameter annotations are involved, so nothing is lost.

**Example:** [`listAccounts3.html`](src/main/webapp/WEB-INF/zul/listAccounts3.zul) → [`BigbankComposer`](src/main/java/org/zkoss/zkspringessentials/bigbank/web/BigbankComposer.java)

```java
@Listen("onClick = #accountGrid row button")
@RequiresRole({"ROLE_SUPERVISOR", "ROLE_TELLER"})
public void onAdjustBalance(MouseEvent event) { ... }
```

`ZkAuthorizationAspect` intercepts the call via Spring AOP; no AspectJ CTW or build plugin required.

> **Does not work for MVVM `@Command` methods with `@BindingParam`.** CGLIB strips `RuntimeVisibleParameterAnnotations` from proxy overrides, so all `@BindingParam` parameters become `null`. See [`tasks/authorization-solution.md`](tasks/authorization-solution.md) for the full analysis.

### Approach 2 — AspectJ Compile-Time Weaving (CTW) + `@RequiresRole`

Works for **MVVM ViewModels** where `@Command` methods use `@BindingParam`. CTW injects advice directly into the compiled bytecode — no CGLIB proxy is generated, so ZK sees the original class with all annotations intact.

**Example:** [`listAccounts2.html`](src/main/webapp/WEB-INF/zul/listAccounts2.zul) → [`BigbankViewModel2`](src/main/java/org/zkoss/zkspringessentials/bigbank/web/BigbankViewModel2.java)

```java
@Command
@RequiresRole({"ROLE_SUPERVISOR", "ROLE_TELLER"})
public void adjustBalance(@BindingParam("accountId") Long id,
                          @BindingParam("amount") Double amount) { ... }
```

Requires `aspectj-maven-plugin` in `pom.xml` and `CtwAuthorizationAspect` (plain `@Aspect`, not a Spring bean).

### Approach 3 — Explicit delegate call (baseline, always works)

Both MVC and MVVM are supported. Inject a `@Service`-layer bean that carries `@PreAuthorize` and call it as the first line of the handler. No AOP involved.

**Example:** [`listAccounts.html`](src/main/webapp/WEB-INF/zul/listAccounts.zul) → [`BigbankViewModel`](src/main/java/org/zkoss/zkspringessentials/bigbank/web/BigbankViewModel.java) (calls `bigbankSecurityService.assertCanAdjustBalance()`)

### Comparison

| Approach | Pattern | Proxy type | `@BindingParam` safe | Boilerplate |
|---|---|---|---|---|
| Spring AOP + `@RequiresRole` | MVC Composer | CGLIB | N/A (no param annotations) | Low |
| AspectJ CTW + `@RequiresRole` | MVVM ViewModel | None (bytecode weave) | Yes | Medium (build plugin) |
| Explicit delegate call | MVC or MVVM | None on ViewModel | Yes | Explicit per-method call |

For details, see [authorization-solution.md](authorization-solution.md)
---

## handle zkau 302
* if you try to change the account balance without authentication, you will be redirected to login page. 
* If you change the account balance without proper authorization (e.g. with USER), you will see [ajaxDenied.zul](src/main/webapp/errors/ajaxDenied.zul)

## handle 302 when loading a zul
* if you try to access a secure page without authentication, you will be redirected to login page.
* if you try to access a secure page without proper authorization, you will see [denied.zul](src%2Fmain%2Fwebapp%2Ferrors%2Fdenied.zul)

## redirect-after-login parameter
if you visit login page with `redirect-after-login` parameter, you will be redirected to the specified page after login e.g. `/login.zul?redirect-after-login=/secure/index.zul` 