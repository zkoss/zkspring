package org.zkoss.spring.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zkoss.spring.SpringUtil;
import org.zkoss.spring.test.config.TestAppConfig;
import org.zkoss.spring.test.config.TestBean;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;
import org.zkoss.zk.ui.sys.WebAppsCtrl;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import javax.servlet.ServletContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
@WebAppConfiguration("src/test/webapp")
public class SpringUtilTest {

	@Autowired
	private ServletContext servletContext;

	private Execution mockExecution = Mockito.spy(Execution.class);
	private Desktop mockDesktop = Mockito.spy(Desktop.class);
	private WebApp mockWebApp = Mockito.mock(WebApp.class);

	private DelegatingVariableResolver vr;

	@Test
	public void testSpringUtil() {
		vr = new DelegatingVariableResolver();

		Mockito.when(mockWebApp.getServletContext()).thenReturn(servletContext);
		Mockito.when(mockDesktop.getWebApp()).thenReturn(mockWebApp);
		Mockito.when(mockExecution.getDesktop()).thenReturn(mockDesktop);

		WebAppsCtrl.setCurrent(mockWebApp);
		ExecutionsCtrl.setCurrent(mockExecution);

		testScopedBean("singleton");
		testScopedBean("webapp");
		testScopedBean("desktop");
		testScopedBean("execution");
	}

	private void testScopedBean(String scopeName) {
		TestBean scopedBean = (TestBean) SpringUtil.getBean(scopeName + "ScopedBean");
		Assert.assertEquals(scopeName, scopedBean.getScopeName());
		final TestBean resolvedBean = (TestBean) vr.resolveVariable(scopeName + "ScopedBean");
		Assert.assertEquals(scopeName, resolvedBean.getScopeName());
	}
}
