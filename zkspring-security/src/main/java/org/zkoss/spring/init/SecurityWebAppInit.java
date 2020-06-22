/* WebAppInit.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 16, 2009 2:57:10 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.zkoss.lang.Library;
import org.zkoss.lang.SystemException;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.Configuration;

/**
 * Adds ZK Spring Security listeners and register security related variable resolver ("zkspring-security.jar:metainfo/zk/config.xml")
 *
 * @author henrichen, Robert
 * @see SecurityContextAwareExecutionListener
 * @since 3.0
 */
public class SecurityWebAppInit implements org.zkoss.zk.ui.util.WebAppInit {
	private static final Logger logger = LoggerFactory.getLogger(SecurityWebAppInit.class);

	private static String RESOLVER_CLASS = CoreWebAppInit.RESOLVER_CLASS;
	private static String SECURITY_RESOLVER = "org.zkoss.spring.init.SecurityVariableResolver";
	public static String SECURITY_CONTEXT_AWARE_EXECUTION_LISTENER_ENABLED = "org.zkoss.spring.init.SecurityContextAwareExecutionListener.enabled";

	public void init(WebApp wapp) throws Exception {

		boolean springSecurityCoreAvailable = ClassUtils.isPresent("org.springframework.security.core.SpringSecurityCoreVersion", null);
		boolean springSecurityWebAvailable = ClassUtils.isPresent("org.springframework.security.web.context.HttpSessionSecurityContextRepository", null);
		boolean springSecurityAclAvailable = ClassUtils.isPresent("org.springframework.security.acls.model.Acl", null);

		if (!springSecurityCoreAvailable) {
			throw new SystemException("spring-security-core missing on classpath: zkspring-security failed to initialize. " +
					"either add the dependency 'spring-security-core' or remove 'zkspring-security' if not used");
		}
		if (!springSecurityAclAvailable) {
			logger.warn("zkspring-security optional dependency 'spring-security-acl' not present: " +
					"'SecurityUtil.isAccessible(...)' and corresponding taglib 'sec:isAccessible(...)' won't be available");
		}

		if ("true".equals(Library.getProperty(SECURITY_CONTEXT_AWARE_EXECUTION_LISTENER_ENABLED, "true"))) {
			if (springSecurityWebAvailable) {
				final Configuration conf = wapp.getConfiguration();
				conf.addListener(SecurityContextAwareExecutionListener.class);
			} else {
				logger.warn("optional dependency spring-security-web not present: " +
						"org.zkoss.spring.init.SecurityContextAwareExecutionListener will be disabled");
			}
		}

		String classes = Library.getProperty(RESOLVER_CLASS);
		if (classes == null) {
			Library.setProperty(RESOLVER_CLASS, SECURITY_RESOLVER);
		} else {
			Library.setProperty(RESOLVER_CLASS, classes + "," + SECURITY_RESOLVER);
		}
	}
}
