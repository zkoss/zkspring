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

import org.zkoss.lang.Library;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.Configuration;

/**
 * Adds ZK Spring Security listeners and register security related variable resolver. 
 * @author henrichen, Robert
 * @see metainfo/zk/config.xml
 * @since 3.0
 */
public class SecurityWebAppInit implements org.zkoss.zk.ui.util.WebAppInit {
	private static String RESOLVER_CLASS = CoreWebAppInit.RESOLVER_CLASS;
	private static String SECURITY_RESOLVER = "org.zkoss.spring.init.SecurityVariableResolver";
	public static String SECURITY_CONTEXT_AWARE_EXECUTION_LISTENER_ENABLED = "org.zkoss.spring.init.SecurityContextAwareExecutionListener.enabled";

	public void init(WebApp wapp) throws Exception {
		if("true".equals(Library.getProperty(SECURITY_CONTEXT_AWARE_EXECUTION_LISTENER_ENABLED, "true"))) {
			final Configuration conf = wapp.getConfiguration();
			conf.addListener(SecurityContextAwareExecutionListener.class);
		}

		String classes = Library.getProperty(RESOLVER_CLASS);
		if (classes == null) {
			Library.setProperty(RESOLVER_CLASS, SECURITY_RESOLVER);
		} else {
			Library.setProperty(RESOLVER_CLASS, classes + ","+ SECURITY_RESOLVER);
		}
	}
}
