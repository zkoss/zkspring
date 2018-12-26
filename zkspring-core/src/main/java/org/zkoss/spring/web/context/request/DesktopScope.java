/* DesktopScope.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 17, 2008 7:04:45 PM, Created by henrichen
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
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

/**
 * ZK Desktop scope; accessible only in ZK event handling request.
 *  
 * @author henrichen
 * @since 1.2
 */
public class DesktopScope implements Scope {
	private static final String DESKTOP_SCOPE = "ZK_SPRING_DESKTOP_SCOPE";

	public Object get(String name, ObjectFactory<?> objectFactory) {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final Desktop desktop = exec.getDesktop();
			Map desktopScope = (Map) desktop.getAttribute(DESKTOP_SCOPE);
			if (desktopScope == null) {
				desktop.setAttribute(DESKTOP_SCOPE, desktopScope = new HashMap());
			}
			Object scopedObject = desktopScope.get(name);
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				desktopScope.put(name, scopedObject);
			}
			return scopedObject;
		}
		throw new IllegalStateException("Unable to get desktop scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public String getConversationId() {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final Desktop desktop = exec.getDesktop();
			if (desktop != null) {
				return desktop.getId();
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
			final Desktop desktop = exec.getDesktop();
			final Map desktopScope = (Map) desktop.getAttribute(DESKTOP_SCOPE);
			return (desktopScope != null) ? desktopScope.remove(name) : null;
		}
		throw new IllegalStateException("Unable to get desktop scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public Object resolveContextualObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
