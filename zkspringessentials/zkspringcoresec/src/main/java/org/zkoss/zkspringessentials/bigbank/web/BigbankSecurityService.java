package org.zkoss.zkspringessentials.bigbank.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Security gateway for the BigBank ViewModel layer.
 * Declaring @PreAuthorize here (on a singleton @Service) avoids CGLIB-proxying
 * the ViewModel itself, which would strip ZK's @Command/@BindingParam annotations
 * from the proxy's method overrides.
 */
@Service
public class BigbankSecurityService {

	@PreAuthorize("hasRole('ROLE_SUPERVISOR') or hasRole('ROLE_TELLER')")
	public void assertCanAdjustBalance() {
		// security gate — Spring Security enforces the role check before this returns
	}
}
