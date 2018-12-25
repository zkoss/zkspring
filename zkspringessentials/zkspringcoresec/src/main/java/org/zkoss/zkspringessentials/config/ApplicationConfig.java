package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.*;
import org.zkoss.spring.config.ZkScopesConfigurer;
import org.zkoss.zkspringessentials.beans.Person;
import org.zkoss.zkspringessentials.beans.SimpleBean;

@Configuration
@ComponentScan("org.zkoss.zkspringessentials")
@Import(ZkScopesConfigurer.class) //enable zk's custom scopes
public class ApplicationConfig {

	@Bean
	public SimpleBean simpleBean() {
		return new SimpleBean("Hello from a simple bean");
	}

	@Bean
	@Scope("desktop")
	public Person person() {
		return new Person(123, "ZkSpring", "TestUser");
	}
}
