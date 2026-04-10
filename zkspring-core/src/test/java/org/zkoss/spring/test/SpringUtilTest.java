package org.zkoss.spring.test;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestAppConfig.class})
@WebAppConfiguration("src/test/webapp")
public class SpringUtilTest {

	@Autowired
	private ServletContext servletContext;

	private Execution mockExecution = Mockito.mock(Execution.class);
	private Desktop mockDesktop = Mockito.mock(Desktop.class);
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
		Assertions.assertEquals(scopeName, scopedBean.getScopeName());
		final TestBean resolvedBean = (TestBean) vr.resolveVariable(scopeName + "ScopedBean");
		Assertions.assertEquals(scopeName, resolvedBean.getScopeName());
	}
}
