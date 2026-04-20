# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ZK Spring is a library that provides Spring Framework integration for the ZK Framework, a Java-based AJAX web framework. The project enables developers to use Spring beans in ZK applications and provides Spring Security integration for ZK-specific security features.

**Official Documentation**: [ZK Spring Essentials](http://books.zkoss.org/wiki/ZK_Spring_Essentials)

## Project Structure

This is a multi-module Maven project with three main modules:

1. **zkspring-core** (`zkspring-core/`) - Core Spring integration library
   - Provides ZK-specific Spring scopes (desktop, page, idspace, execution, webapp)
   - Variable resolvers for accessing Spring beans from ZUL files
   - Configuration support via `ZkScopesConfigurer`

2. **zkspring-security** (`zkspring-security/`) - Spring Security integration
   - Depends on zkspring-core
   - Security utilities (`SecurityUtil`, `SecurityUtilAcl`)
   - Security context propagation through ZK executions
   - Custom request matchers for ZK-specific URLs (e.g., `ZkDesktopRemoveRequestMatcher`)

3. **zkspringessentials/zkspringcoresec** - Example/demo application (WAR)
   - Demonstrates zkspring-core and zkspring-security usage
   - Contains example security configurations and use cases

## Technology Stack

- **Java**: Java 17 (modules) / Java 11 (essentials)
- **ZK Framework**: 9.6.0-jakarta (Jakarta EE 9+)
- **Spring Framework**: 6.2.1
- **Spring Security**: 6.4.2
- **Testing**: JUnit 4, Mockito, ZATS (ZK Application Test Suite) 4.0.0

## Build Commands

### Build All Modules
```bash
mvn clean install
```

### Build Specific Module
```bash
# Build zkspring-security and its dependencies
mvn clean install -pl zkspring-security -am

# Build zkspring-core only
mvn clean install -pl zkspring-core
```

### Run Tests
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl zkspring-core

# Skip tests during build
mvn clean install -DskipTests
```

### Run Example Application
```bash
cd zkspringessentials/zkspringcoresec
mvn jetty:run
```

The example app will be available at http://localhost:8080

### Create Release Artifacts
```bash
# Freshly build (appends timestamp)
./release

# Official release (removes -SNAPSHOT)
./release official
```

The release script:
- Extracts version from `zkspring-core/pom.xml`
- Updates version across modules using `./upVer` script
- Builds with source and javadoc jars
- Creates bundle for Maven repository
- Places artifacts in `target/to_upload/`

### Update Version
```bash
# Usage: ./upVer <old-version> <new-version> <FL>
./upVer 6.2.1-SNAPSHOT 6.2.2-SNAPSHOT FL
```

## Key Architecture Concepts

### ZK-Specific Spring Scopes

ZK Spring provides custom Spring bean scopes that align with ZK's component lifecycle:

- **webapp** - Application-wide scope (similar to singleton but ZK-aware)
- **desktop** - Per ZK Desktop (browser window/tab)
- **page** - Per ZK page
- **idspace** - Per ID space (component hierarchy)
- **execution** - Per ZK execution (request-response cycle)

These are registered via `ZkScopesConfigurer` (`zkspring-core/src/main/java/org/zkoss/spring/config/ZkScopesConfigurer.java:12`)

### Variable Resolvers

ZK uses variable resolvers to expose Spring beans in ZUL markup and controllers:

- **CoreVariableResolver** - Resolves Spring beans in ZUL expressions
- **SecurityVariableResolver** - Provides security-related variables

These are automatically registered by `CoreWebAppInit` and `SecurityWebAppInit` through ZK's `WebAppInit` mechanism.

### Spring Security Integration

The zkspring-security module handles special ZK-specific security concerns:

1. **CSRF Protection**: ZK has its own CSRF protection via Desktop ID, so Spring Security's CSRF must be disabled when using ZK login forms

2. **Request Matchers**: Custom matchers handle ZK-specific URLs:
   - `/zkres/**` - ZK resources (DHtmlResourceServlet)
   - `/zkau/**` - ZK AU (AJAX update) requests
   - Desktop removal requests need special handling

3. **Security Context Propagation**: `SecurityContextAwareExecutionListener` ensures Spring's SecurityContext is available throughout ZK executions

4. **403 for AJAX**: ZK AJAX requests return 403 instead of 302 redirects (see `SecurityConfig.java:59`)

### Example Security Configuration Pattern

Reference: `zkspringessentials/zkspringcoresec/src/main/java/org/zkoss/zkspringessentials/config/SecurityConfig.java`

Key patterns:
- Permit ZK resources and login pages explicitly
- Use `ZkDesktopRemoveRequestMatcher` to allow desktop cleanup
- Configure `Http403ForbiddenEntryPoint` for `/zkau` POST requests
- Use `NullRequestCache` to avoid redirecting to `/zkau` after login
- Disable Spring CSRF (ZK has its own)

## Important Utilities

### SpringUtil
Located at: `zkspring-core/src/main/java/org/zkoss/spring/SpringUtil.java`

```java
// Get Spring ApplicationContext from ZK execution
ApplicationContext ctx = SpringUtil.getApplicationContext();

// Get bean by name
MyBean bean = (MyBean) SpringUtil.getBean("myBean");
```

### SecurityUtil
Located at: `zkspring-security/src/main/java/org/zkoss/spring/security/SecurityUtil.java`

```java
// Check if user has any/all/none of specified roles
SecurityUtil.isAnyGranted("ROLE_USER,ROLE_ADMIN");
SecurityUtil.isAllGranted("ROLE_USER,ROLE_SUPERVISOR");
SecurityUtil.isNoneGranted("ROLE_GUEST");

// Get current authentication
Authentication auth = SecurityUtil.getAuthentication();

// Get authentication property
String username = (String) SecurityUtil.getAuthentication("principal.username");

// Check permissions (requires spring-security-acl)
SecurityUtil.isAccessible("1,2", domainObject);
```

## Development Notes

### Maven Repository Configuration

The project uses custom ZK repositories:
- `https://mavensync.zkoss.org/maven2` - ZK CE (Community Edition)
- `https://maven.zkoss.org/repo/zk/ee` - ZK EE (Enterprise Edition)
- `https://mavensync.zkoss.org/eval` - ZK EE Evaluation

### Spring and ZK Dependencies

Spring dependencies (`spring-web`, `spring-context`) are marked as `provided` scope in zkspring-core, allowing users to choose their own Spring versions (must be compatible with declared version).

ZK dependencies (`zkplus`, `zul`) are also `provided` scope.

### Testing with ZATS

The project uses ZATS (ZK Application Test Suite) for testing ZK components without a servlet container. Test files use ZATS mimic engine to simulate user interactions.

### Configuration Files

- `src/main/resources/metainfo/zk/config.xml` - ZK library descriptor, registers WebAppInit classes
- `src/main/resources/metainfo/tld/config.xml` - Tag library descriptor for ZUL taglibs

## Common Workflows

### Adding a New Spring Scope
1. Create scope class implementing `org.springframework.beans.factory.config.Scope` in `zkspring-core/src/main/java/org/zkoss/spring/web/context/request/`
2. Register in `ZkScopesConfigurer.java` constructor
3. Add tests in `zkspring-core/src/test/`

### Adding Security Utilities
1. Add methods to `SecurityUtil.java` or `SecurityUtilAcl.java`
2. If exposing to ZUL, add taglib definition in `zkspring-security/src/main/resources/metainfo/tld/config.xml`
3. Add corresponding tests

### Working with Spring Security Configuration
When modifying security configurations for ZK applications:
- Always permit `/zkres/**` for ZK resources
- Always permit ZK desktop removal requests
- Consider AJAX requests need 403 instead of 302
- Remember to disable Spring CSRF or configure properly with ZK
- Use `LoginZkauMatcher` or similar if login page uses ZK AU requests
