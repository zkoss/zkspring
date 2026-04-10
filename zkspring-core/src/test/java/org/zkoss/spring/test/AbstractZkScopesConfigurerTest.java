package org.zkoss.spring.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zkoss.spring.web.context.request.*;

import org.junit.jupiter.api.Assertions;

@ExtendWith(SpringExtension.class)
public abstract class AbstractZkScopesConfigurerTest {
	@Autowired
	private ConfigurableListableBeanFactory beanFactory;

	@Test
	public void testScopesInitialized() {
		final String[] registeredScopeNames = beanFactory.getRegisteredScopeNames();
		Assertions.assertArrayEquals(new String[]{"webapp", "desktop", "page", "idspace", "execution"}, registeredScopeNames);
		Assertions.assertTrue(beanFactory.getRegisteredScope("webapp") instanceof ApplicationScope);
		Assertions.assertTrue(beanFactory.getRegisteredScope("desktop") instanceof DesktopScope);
		Assertions.assertTrue(beanFactory.getRegisteredScope("page") instanceof PageScope);
		Assertions.assertTrue(beanFactory.getRegisteredScope("idspace") instanceof IdSpaceScope);
		Assertions.assertTrue(beanFactory.getRegisteredScope("execution") instanceof ExecutionScope);
	}
}
