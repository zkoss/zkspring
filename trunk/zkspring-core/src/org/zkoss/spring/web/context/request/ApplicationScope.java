/* ApplicationScope.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 24, 2008 10:35:49 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.web.context.request;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;

/**
 * ZK WebApp scope; accessible only in ZK event handling request.
 * @author henrichen
 * @since 1.2
 */
public class ApplicationScope implements Scope {
	private static final String APP_SCOPE = "ZK_SPRING_APP_SCOPE";
	
	public Object get(String name, ObjectFactory objectFactory) {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final WebApp app = exec.getDesktop().getWebApp();
			Map appScope = (Map) app.getAttribute(APP_SCOPE);
			if (appScope == null) {
				app.setAttribute(APP_SCOPE, appScope = new HashMap());
			}
			Object scopedObject = appScope.get(name);
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				appScope.put(name, scopedObject);
			}
			return scopedObject;
		}
		throw new IllegalStateException("Unable to get application scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public String getConversationId() {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final WebApp app = exec.getDesktop().getWebApp();
			if (app != null) {
				return app.getAppName();
			}
		}
		return null;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		// do nothing
	}

	public Object remove(String name) {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final WebApp app = exec.getDesktop().getWebApp();
			final Map appScope = (Map) app.getAttribute(APP_SCOPE);
			return (appScope != null) ? appScope.remove(name) : null;
		}
		throw new IllegalStateException("Unable to get application scope bean: "+name+". Do you access it in ZK event listener?");
	}

	@Override
	public Object resolveContextualObject(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
