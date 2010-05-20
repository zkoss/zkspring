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
import org.zkoss.spring.bean.ZkSpringUiFactory;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import org.zkoss.zk.ui.util.Configuration;

/**
 * Automatically disable ZK event thread mechanism and install a ZK Spring UiFactory. 
 * @author henrichen
 * @see metainfo/zk/config.xml
 * @since 1.2.0
 */
public class WebAppInit implements org.zkoss.zk.ui.util.WebAppInit {
	public void init(WebApp wapp) throws Exception {
		final Configuration conf = wapp.getConfiguration();
		String value = conf.getPreference("org.zkoss.spring.VariableResolver", null);
		if(value == null) { 
			conf.setPreference("org.zkoss.spring.VariableResolver", "org.zkoss.spring.DelegatingVariableResolver");
		} else {
			conf.setPreference("org.zkoss.spring.VariableResolver", value + ",org.zkoss.spring.DelegatingVariableResolver");
		}
		//If user does not give UiFactoryClass in zk.xml
		if (conf.getUiFactoryClass() == null) { 
			//<disable-event-thread/>
			conf.enableEventThread(false); 
			//<ui-factory-class>org.zkoss.spring.bean.ZkSpringUiFactory</ui-factory-class>
			try {
				((WebAppCtrl) wapp).setUiFactory(new ZkSpringUiFactory());
			} catch (AbstractMethodError ex) {
				//ignore
			}
		}

	}
}
