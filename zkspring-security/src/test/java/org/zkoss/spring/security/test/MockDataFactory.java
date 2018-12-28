package org.zkoss.spring.security.test;

import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

public class MockDataFactory {
	public static SecurityContext createMockSecurityContext(String name) {
		final Principal mockPrincipal = Mockito.mock(Principal.class);
		final Authentication mockAuthentication = Mockito.mock(Authentication.class);
		Collection<? extends GrantedAuthority> roles = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"));
		Mockito.doReturn(roles).when(mockAuthentication).getAuthorities();
		Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockPrincipal);
		Mockito.when(mockAuthentication.getName()).thenReturn(name);
		final SecurityContext securityContext = new SecurityContextImpl();
		securityContext.setAuthentication(mockAuthentication);
		return securityContext;
	}

	public static SecurityContext createAnonymousSecurityContext() {
		final Principal mockPrincipal = Mockito.mock(Principal.class);
		final Authentication anonymousAuthentication = new AnonymousAuthenticationToken("key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
		final SecurityContext securityContext = new SecurityContextImpl();
		securityContext.setAuthentication(anonymousAuthentication);
		return securityContext;
	}
}
