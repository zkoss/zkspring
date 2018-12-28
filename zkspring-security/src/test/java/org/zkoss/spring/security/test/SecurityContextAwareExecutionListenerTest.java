package org.zkoss.spring.security.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.spring.init.SecurityContextAwareExecutionListener;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

public class SecurityContextAwareExecutionListenerTest {
	public static final String TEST_USER_NAME = "test_user_name";

	private ServletContext mockServletContext = Mockito.mock(ServletContext.class);
	private Execution mockExecution = Mockito.mock(Execution.class);
	private Desktop mockDesktop = Mockito.mock(Desktop.class);
	private WebApp mockWebApp = Mockito.mock(WebApp.class);


	@Before
	public void setup() {
		Mockito.reset(mockServletContext, mockWebApp, mockDesktop, mockExecution);
		Mockito.when(mockWebApp.getServletContext()).thenReturn(mockServletContext);
		Mockito.when(mockDesktop.getWebApp()).thenReturn(mockWebApp);
		Mockito.when(mockExecution.getDesktop()).thenReturn(mockDesktop);
		final Map<String, Object> mockScope = new HashMap<String, Object>();

		Mockito.when(mockExecution.setAttribute(Mockito.anyString(), Mockito.any(SecurityContext.class))).thenAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				return mockScope.put((String) invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
			}
		});
		Mockito.when(mockExecution.removeAttribute(Mockito.anyString())).thenAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				return mockScope.remove((String) invocationOnMock.getArgument(0));
			}
		});
		ExecutionsCtrl.setCurrent(mockExecution);
		SecurityContextHolder.clearContext();
	}

	@After
	public void cleanup() {
		SecurityContextHolder.clearContext();
		ExecutionsCtrl.setCurrent(null);
	}

	@Test
	public void testNonExistingSecurityContext() throws Exception {
		final SecurityContext securityContext = MockDataFactory.createMockSecurityContext(TEST_USER_NAME);
		SecurityContextAwareExecutionListener listener = createTestListener(securityContext);

		Assert.assertNull("Authentication should be empty before init", SecurityContextHolder.getContext().getAuthentication());

		listener.init(mockExecution, null);
		Assert.assertSame(securityContext, SecurityContextHolder.getContext());
		Assert.assertEquals(TEST_USER_NAME, SecurityContextHolder.getContext().getAuthentication().getName());

		listener.cleanup(mockExecution, null, null);
		Assert.assertNull("Authentication should be empty after init", SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	public void testAnonymousSecurityContext() throws Exception {
		final SecurityContext securityContext = MockDataFactory.createMockSecurityContext(TEST_USER_NAME);
		SecurityContextAwareExecutionListener listener = createTestListener(securityContext);

		final SecurityContext anonymousSecurityContext = MockDataFactory.createAnonymousSecurityContext();

		//the listener should not touch an exising SecurityContext
		SecurityContextHolder.setContext(anonymousSecurityContext);
		Assert.assertSame(anonymousSecurityContext, SecurityContextHolder.getContext());

		listener.init(mockExecution, null);
		Assert.assertSame(anonymousSecurityContext, SecurityContextHolder.getContext());

		listener.cleanup(mockExecution, null, null);
		Assert.assertSame(anonymousSecurityContext, SecurityContextHolder.getContext());
	}

	@Test
	public void testExistingSecurityContext() throws Exception {
		final SecurityContext securityContext = MockDataFactory.createMockSecurityContext(TEST_USER_NAME);
		SecurityContextAwareExecutionListener listener = createTestListener(securityContext);

		final SecurityContext existingUser = MockDataFactory.createMockSecurityContext("existing_user");

		//the listener should not touch an exising SecurityContext
		SecurityContextHolder.setContext(existingUser);
		Assert.assertSame(existingUser, SecurityContextHolder.getContext());

		listener.init(mockExecution, null);
		Assert.assertSame(existingUser, SecurityContextHolder.getContext());
		Assert.assertEquals("existing_user", SecurityContextHolder.getContext().getAuthentication().getName());

		listener.cleanup(mockExecution, null, null);
		Assert.assertSame(existingUser, SecurityContextHolder.getContext());
	}

	private SecurityContextAwareExecutionListener createTestListener(final SecurityContext securityContext) {
		return new SecurityContextAwareExecutionListener() {
				@Override
				protected SecurityContext loadSecurityContext(Execution execution) {
					return securityContext;
				}
			};
	}

}
