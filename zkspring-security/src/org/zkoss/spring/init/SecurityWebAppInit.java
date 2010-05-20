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

import javax.servlet.ServletContext;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.lang.Library;
import org.zkoss.spring.security.config.ZkBeanIds;
import org.zkoss.spring.security.config.ZkSecurityContextListener;
import org.zkoss.spring.security.intercept.zkevent.ZkEventProcessListener;
import org.zkoss.spring.security.ui.ZkExceptionTranslationListener;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.Configuration;

/**
 * Adds ZK Spring Security listeners. 
 * @author henrichen
 * @see metainfo/zk/config.xml
 * @since 1.2.0
 */
public class SecurityWebAppInit implements org.zkoss.zk.ui.util.WebAppInit {
	public void init(WebApp wapp) throws Exception {
		final Configuration conf = wapp.getConfiguration();
		
		final ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) 
			WebApplicationContextUtils.getRequiredWebApplicationContext((ServletContext)wapp.getNativeContext());
		
		//add listener for ZK + Security
		if (ctx.containsBeanDefinition(ZkBeanIds.ZK_DESKTOP_REUSE_FILTER)) {
			conf.addListener(ZkSecurityContextListener.class);
			conf.addListener(ZkExceptionTranslationListener.class);
			conf.addListener(ZkEventProcessListener.class);
		}
		
		String value = conf.getPreference("org.zkoss.spring.VariableResolver", null);
		if(value == null) { 
			conf.setPreference("org.zkoss.spring.VariableResolver", "org.zkoss.spring.security.DelegatingVariableResolver");
		} else {
			conf.setPreference("org.zkoss.spring.VariableResolver", value + ",org.zkoss.spring.security.DelegatingVariableResolver");
		}
	}
}
