/* WebflowWebAppInit.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 16, 2011 2:57:10 PM, Created by ashish
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

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
 * Rgisters WebflowVariableResolver with the DelegatingVariableResolver
 * @author ashish
 *
 */
public class WebflowWebAppInit implements org.zkoss.zk.ui.util.WebAppInit {
	private static String RESOLVER_CLASS = org.zkoss.spring.DelegatingVariableResolver.RESOLVER_CLASS;
	private static String WEBFLOW_RESOLVER = "org.zkoss.spring.init.WebflowVariableResolver";
	public void init(WebApp wapp) throws Exception {
		final Configuration conf = wapp.getConfiguration();
		String classes = Library.getProperty(RESOLVER_CLASS);
		if (classes == null) {
			Library.setProperty(RESOLVER_CLASS, WEBFLOW_RESOLVER);
		} else {
			Library.setProperty(RESOLVER_CLASS, classes + ","+ WEBFLOW_RESOLVER);
		}
	}
}
