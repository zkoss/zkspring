package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.*;
import org.zkoss.spring.config.ZkScopesConfigurer;
import org.zkoss.zkspringessentials.app.beans.Person;
import org.zkoss.zkspringessentials.app.beans.SimpleBean;

@Configuration
@ComponentScan({"org.zkoss.zkspringessentials.config", "org.zkoss.zkspringessentials.app"})
@Import(ZkScopesConfigurer.class) //enable zk's custom scopes
@PropertySource("classpath:values.properties")
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
