# How to Run
```bash
mvn jetty:run
```
Visit http://localhost:8080/zkspringcoresec/

Demo accounts:

| User   | Password | Roles                    |
|--------|----------|--------------------------|
| rod    | koala    | SUPERVISOR, USER, TELLER |
| dianne | emu      | USER, TELLER             |
| scott  | wombat   | USER                     |
| peter  | opal     | USER                     |


# Spring Security Use Cases 
This project demonstrates common Spring Security integration patterns with ZK Framework.

## Login Page with ZUL
* Users login with a form submission to avoid sending AU requests, because `/zkau` is secured.
* Requires `DHtmlResourceServlet` (available since ZK 8.6.2) so ZUL pages load correctly when all URLs are secured.
* The browser can still fetch ZK resources including `*.wpd`, `*.wcs`, and bundled images.

## URL-Based Authorization (Page Level)
Spring Security secures pages by URL pattern. See [`SecurityConfig`](src/main/java/org/zkoss/zkspringessentials/config/SecurityConfig.java).

* **supervisor** -- access extreme secure page, secure page, change account balance
* **teller** -- access secure page, change account balance
* **user** -- access secure page only

Secured pages:
* [extreme secure page](src/main/webapp/secure/extreme/index.zul) -- requires `SUPERVISOR`
* [secure page](src/main/webapp/secure/index.zul) -- requires `USER`


# Controller-Layer Authorization (Operation Level)

ZK's `/zkau` endpoint is a single HTTP entry point. URL-based rules can only gate the entire endpoint, not individual event handlers or commands. Every `@Command` and `@Listen` handler is independently reachable from the browser and must be individually authorized.

Securing only the service layer (`@PreAuthorize` on `BankService`) is the minimum. Adding a guard at the controller/ViewModel layer provides:

- **Fail fast** -- the exception is raised at the UI boundary, where error feedback is most meaningful
- **Defense in depth** -- even if a service method is refactored or a new call path is added, the controller guard still fires
- **Readable intent** -- the policy is visible alongside the UI code, not buried in a service

This project demonstrates two approaches. Both work with MVC and MVVM patterns.

---

## Approach 1 -- `@PreAuthorize` + AspectJ Compile-Time Weaving

Annotate `@Command` or `@Listen` methods directly with `@PreAuthorize`. The `spring-security-aspects` library weaves the security check into bytecode at compile time, so no CGLIB proxy is generated and all ZK annotations (including `@BindingParam`) remain intact.

### Why Not Standard Spring AOP?

When Spring creates a CGLIB proxy for a ViewModel, it copies method-level annotations (`@Command`) but **not** parameter annotations (`@BindingParam`). The command fires but all parameters arrive as `null`. AspectJ CTW avoids this by modifying the original class bytecode directly.

### Step 1 -- Add Dependencies to `pom.xml`

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.22</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.9.22</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-aspects</artifactId>
    <version>${springsecurity.version}</version>
</dependency>
```

### Step 2 -- Configure the AspectJ Maven Plugin in `pom.xml`

This plugin weaves `PreAuthorizeAspect` from `spring-security-aspects` into your classes at compile time.

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.15.0</version>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>1.9.22</version>
        </dependency>
    </dependencies>
    <configuration>
        <complianceLevel>17</complianceLevel>
        <source>17</source>
        <target>17</target>
        <showWeaveInfo>true</showWeaveInfo>
        <encoding>UTF-8</encoding>
        <Xlint>ignore</Xlint>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>org.springframework.security</groupId>
                <artifactId>spring-security-aspects</artifactId>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
    <executions>
        <execution>
            <goals><goal>compile</goal></goals>
        </execution>
    </executions>
</plugin>
```

### Step 3 -- Set `AdviceMode.ASPECTJ` in `SecurityConfig`

This tells Spring Security **not** to create CGLIB proxies for method security. Without this, both CTW and Spring AOP would intercept `@PreAuthorize`, and the proxy would still strip `@BindingParam`.

```java
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, mode = AdviceMode.ASPECTJ)
public class SecurityConfig { ... }
```

See [`SecurityConfig`](src/main/java/org/zkoss/zkspringessentials/config/SecurityConfig.java).

### Step 4 -- Annotate Your Handlers

**MVVM** -- [`BigbankViewModel2`](src/main/java/org/zkoss/zkspringessentials/bigbank/web/BigbankViewModel2.java)

```java
@Command
@PreAuthorize("hasAnyRole('SUPERVISOR', 'TELLER')")
public void adjustBalance(@BindingParam("accountId") Long id,
                          @BindingParam("amount") Double amount) {
    // business logic -- @BindingParam values arrive correctly
}
```

**MVC** -- [`BigbankComposer`](src/main/java/org/zkoss/zkspringessentials/bigbank/web/BigbankComposer.java)

```java
@Listen("onClick = #accountGrid row button")
@PreAuthorize("hasAnyRole('SUPERVISOR', 'TELLER')")
public void onAdjustBalance(MouseEvent event) {
    // MVC handlers use event objects, no parameter annotations involved
}
```

**MVC note:** `@Listen` in `SelectorComposer` binds to components at `doAfterCompose` time. If you replace a model item via `ListModelList.set()`, the row re-renders and creates new buttons without the listener binding. Update the UI directly instead.

---

## Approach 2 -- Explicit Delegate Call (No Build Plugin)

Inject a separate `@Service` that carries `@PreAuthorize` and call it as the first line of the handler. Spring proxies the service safely since no ZK annotations are involved on the service bean.

### Step 1 -- Create a Security Service

[`BigbankSecurityService`](src/main/java/org/zkoss/zkspringessentials/bigbank/web/BigbankSecurityService.java)

```java
@Service
public class BigbankSecurityService {
    @PreAuthorize("hasRole('ROLE_SUPERVISOR') or hasRole('ROLE_TELLER')")
    public void assertCanAdjustBalance() {
        // Spring Security enforces the role check before this method returns
    }
}
```

### Step 2 -- Call the Security Service from Your Handler

[`BigbankViewModel`](src/main/java/org/zkoss/zkspringessentials/bigbank/web/BigbankViewModel.java)

```java
@Autowired private BigbankSecurityService securityService;

@Command
public void adjustBalance(@BindingParam("accountId") Long id,
                          @BindingParam("amount") Double amount) {
    securityService.assertCanAdjustBalance();   // throws AccessDeniedException if denied
    // ... business logic
}
```

No AspectJ plugin or special configuration is required. Standard Spring AOP proxies the `@Service` bean, and the ViewModel remains unproxied so all ZK annotations work normally.

---

## Comparison

| Approach | `@BindingParam` safe | Build plugin required | Boilerplate |
|---|---|---|---|
| `@PreAuthorize` + AspectJ CTW | Yes | Yes (`aspectj-maven-plugin`) | None -- annotation only |
| Explicit delegate call | Yes | No | One line per handler |

**Recommendation:** Use AspectJ CTW for projects that already use the AspectJ Maven plugin. Use the explicit delegate approach for simpler projects that want to avoid the build plugin dependency.

For the full technical analysis (including the CGLIB proxy problem), see [authorization-solution.md](authorization-solution.md).

---

## Handling Unauthorized Access

* If you try to change the account balance without authentication, you will be redirected to the login page.
* If you change the account balance without proper authorization (e.g. logged in as `scott` with only `USER` role), you will see [ajaxDenied.zul](src/main/webapp/errors/ajaxDenied.zul).

## Handling 302 When Loading a ZUL

* If you try to access a secure page without authentication, you will be redirected to the login page.
* If you try to access a secure page without proper authorization, you will see [denied.zul](src/main/webapp/errors/denied.zul).

## Redirect-After-Login Parameter
If you visit the login page with a `redirect-after-login` parameter, you will be redirected to the specified page after login, e.g. `/login.zul?redirect-after-login=/secure/index.zul`
