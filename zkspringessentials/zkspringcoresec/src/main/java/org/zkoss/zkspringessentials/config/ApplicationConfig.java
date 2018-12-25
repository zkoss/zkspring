package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.zkoss.spring.config.ZkScopesConfigurer;
import org.zkoss.zkspringessentials.beans.Person;
import org.zkoss.zkspringessentials.beans.SimpleBean;

@Configuration
@ComponentScan(basePackages = {"bigbank", "org.zkoss.zkspringessentials"})
@Import({ZkScopesConfigurer.class, SecurityConfig.class})
public class ApplicationConfig {

	@Bean
	public SimpleBean simpleBean() {
		return new SimpleBean("Hello from a simple bean");
	}

	@Bean
	public Person person() {
		final Person person = new Person();
		person.setFirstName("Ashish");
		person.setLastName("Dasnurkar");
		return person;
	}
}
