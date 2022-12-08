# Features

## Variable Resolver
* `org.zkoss.spring.init.CoreVariableResolver`
* `org.zkoss.spring.DelegatingVariableResolver`

## Custom Scope Bean
Provide ZK custom scope including:
* `webapp`
* `desktop`
* `page`
* `idspace`
* `execution`

See `ZkScopesConfigurer`.

### Usage
```java
@Configuration
@Import(ZkScopesConfigurer.class)
public class TestAppConfig {

    @Bean
    @Scope("webapp")
    public TestBean webappScopedBean() {
        return new TestBean("webapp");
    }

    @Bean
    @Scope("desktop")
    public TestBean desktopScopedBean() {
        return new TestBean("desktop");
    }

    @Bean
    @Scope("execution")
    public TestBean executionScopedBean() {
        return new TestBean("execution");
    }
}
```

# Reference
[Spring framework](https://spring.io/projects/spring-framework)