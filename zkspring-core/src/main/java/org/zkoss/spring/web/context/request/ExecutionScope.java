/* ExecutionScope.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 17, 2008 6:54:10 PM, Created by henrichen
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
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

/**
 * ZK Execution scope; accessible only in one ZK execution(e.g. ZK event handling).
 * @author henrichen
 * @since 1.2
 */
public class ExecutionScope implements Scope {
	private static final String EXEC_SCOPE = "ZK_SPRING_EXEC_SCOPE";
	
	public Object get(String name, ObjectFactory<?> objectFactory) {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			Map execScope = (Map) exec.getAttribute(EXEC_SCOPE);
			if (execScope == null) {
				ZKProxy.getProxy().setAttribute(exec, EXEC_SCOPE, execScope = new HashMap());
			}
			Object scopedObject = execScope.get(name);
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				execScope.put(name, scopedObject);
			}
			return scopedObject;
		}
		throw new IllegalStateException("Unable to get execution scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public String getConversationId() {
		return null;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		//do nothing
	}

	public Object remove(String name) {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final Map execScope = (Map) exec.getAttribute(EXEC_SCOPE);
			return execScope != null ? execScope.remove(name) : null;
		}
		throw new IllegalStateException("Unable to get execution scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public Object resolveContextualObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}
}
