/* CoreWebAppInit.java

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

/**
 * Register spring core related variable resolver, configured in "zkspring-core.jar:metainfo/zk/config.xml".
 * @author henrichen, Robert
 *
 * @since 3.0
 */
public class CoreWebAppInit implements org.zkoss.zk.ui.util.WebAppInit {
	public static String RESOLVER_CLASS = "org.zkoss.spring.VariableResolver.class";
	private static String CORE_RESOLVER = "org.zkoss.spring.init.CoreVariableResolver";
	public void init(WebApp wapp) throws Exception {
		String classes = Library.getProperty(RESOLVER_CLASS);
		if (classes == null) {
			Library.setProperty(RESOLVER_CLASS, CORE_RESOLVER);
		} else {
			Library.setProperty(RESOLVER_CLASS, classes + ","+ CORE_RESOLVER);
		}
	}
}
