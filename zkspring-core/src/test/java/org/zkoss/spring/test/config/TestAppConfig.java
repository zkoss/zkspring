package org.zkoss.spring.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.zkoss.spring.config.ZkScopesConfigurer;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApps;

@Configuration
@Import(ZkScopesConfigurer.class)
public class TestAppConfig {

	@Bean
	public TestBean singletonScopedBean() {
		return new TestBean("singleton");
	}

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
