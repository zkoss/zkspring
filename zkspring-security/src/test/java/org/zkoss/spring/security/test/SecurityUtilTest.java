package org.zkoss.spring.security.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.spring.security.SecurityUtil;

public class SecurityUtilTest {
	public static final String TEST_USER_NAME = "test_user_name";

	@After
	@Before
	public void cleanup() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testAuthentication() {
		Assert.assertNull("Expect null authentication before setup", SecurityUtil.getAuthentication());
		SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
		Assert.assertNull("Expect null authentication for empty context", SecurityUtil.getAuthentication());

		final SecurityContext securityContext = MockDataFactory.createMockSecurityContext(TEST_USER_NAME);
		SecurityContextHolder.setContext(securityContext);

		Assert.assertSame("Authentication should be present", securityContext.getAuthentication(), SecurityUtil.getAuthentication());
		Assert.assertNotNull("Principal should be present", SecurityUtil.getAuthentication("principal"));
		Assert.assertEquals("User name should be present", TEST_USER_NAME, SecurityUtil.getAuthentication("name"));

		SecurityContextHolder.clearContext();
		Assert.assertNull("Expect null authentication after cleanup", SecurityUtil.getAuthentication());
		Assert.assertNull("Expect null authentication after cleanup", SecurityUtil.getAuthentication("principal"));
		Assert.assertNull("Expect null authentication after cleanup", SecurityUtil.getAuthentication("name"));
	}

	@Test
	public void testRoles() {
		Assert.assertFalse(SecurityUtil.isAllGranted(""));
		Assert.assertFalse(SecurityUtil.isAllGranted(null));
		Assert.assertFalse(SecurityUtil.isAnyGranted(""));
		Assert.assertFalse(SecurityUtil.isAnyGranted(null));
		Assert.assertFalse(SecurityUtil.isNoneGranted(""));
		Assert.assertFalse(SecurityUtil.isNoneGranted(null));

		SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
		Assert.assertFalse(SecurityUtil.isAllGranted("ROLE_USER,ROLE_ADMIN"));
		Assert.assertFalse(SecurityUtil.isAnyGranted("ROLE_USER,ROLE_ADMIN"));
		Assert.assertTrue(SecurityUtil.isNoneGranted("ROLE_USER,ROLE_ADMIN"));

		SecurityContextHolder.setContext(MockDataFactory.createMockSecurityContext(TEST_USER_NAME));

		Assert.assertTrue(SecurityUtil.isAllGranted("ROLE_USER,ROLE_ADMIN"));
		Assert.assertTrue(SecurityUtil.isAllGranted("ROLE_USER"));
		Assert.assertTrue(SecurityUtil.isAllGranted("ROLE_ADMIN"));
		Assert.assertFalse(SecurityUtil.isAllGranted("ROLE_ACCOUNTANT,ROLE_COLLECTOR"));
		Assert.assertFalse(SecurityUtil.isAllGranted("ROLE_ADMIN,ROLE_USER,ROLE_COLLECTOR"));

		Assert.assertTrue(SecurityUtil.isAnyGranted("ROLE_USER,ROLE_ADMIN"));
		Assert.assertTrue(SecurityUtil.isAnyGranted("ROLE_USER"));
		Assert.assertTrue(SecurityUtil.isAnyGranted("ROLE_USER,ROLE_COLLECTOR"));
		Assert.assertFalse(SecurityUtil.isAnyGranted("ROLE_ACCOUNTANT,ROLE_COLLECTOR"));

		Assert.assertFalse(SecurityUtil.isNoneGranted("ROLE_COLLECTOR,ROLE_ADMIN"));
		Assert.assertTrue(SecurityUtil.isNoneGranted("ROLE_COLLECTOR,ROLE_ACCOUNTANT"));
	}

}
