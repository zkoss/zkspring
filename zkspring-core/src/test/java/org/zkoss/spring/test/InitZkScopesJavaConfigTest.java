package org.zkoss.spring.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zkoss.spring.test.config.InitZkScopesConfig;
import org.zkoss.spring.web.context.request.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InitZkScopesConfig.class})
public class InitZkScopesJavaConfigTest {

	@Autowired
	private ConfigurableListableBeanFactory beanFactory;

	@Test
	public void testScopesInitialized() {
		final String[] registeredScopeNames = beanFactory.getRegisteredScopeNames();
		Assert.assertArrayEquals(registeredScopeNames, new String[] {"webapp", "desktop", "page", "idspace", "execution"});
		Assert.assertTrue(beanFactory.getRegisteredScope("webapp") instanceof ApplicationScope);
		Assert.assertTrue(beanFactory.getRegisteredScope("desktop") instanceof DesktopScope);
		Assert.assertTrue(beanFactory.getRegisteredScope("page") instanceof PageScope);
		Assert.assertTrue(beanFactory.getRegisteredScope("idspace") instanceof IdSpaceScope);
		Assert.assertTrue(beanFactory.getRegisteredScope("execution") instanceof ExecutionScope);
	}
}
