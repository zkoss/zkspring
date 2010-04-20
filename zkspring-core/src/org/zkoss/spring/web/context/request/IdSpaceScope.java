/* IdSpaceScope.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 23, 2008 3:50:11 PM, Created by henrichen
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
import org.zkoss.zk.scripting.Namespace;
import org.zkoss.zk.scripting.Namespaces;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.sys.ExecutionCtrl;

/**
 * ZK IdSpace scope; accessible only in ZK event handling request.
 * 
 * @author henrichen
 * @since 1.2
 */
public class IdSpaceScope implements Scope {
	private static final String IDSPACE_SCOPE = "ZK_SPRING_IDSPACE_SCOPE";

	public Object get(String name, ObjectFactory<?> objectFactory) {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final Component self = ZKProxy.getProxy().getSelf((ExecutionCtrl)exec);
			final IdSpace idspace = 
				self == null ?	null : self.getSpaceOwner();
			Map idspaceScope = null;
			if (idspace instanceof Component) {
				idspaceScope = (Map) ((Component)idspace).getAttribute(IDSPACE_SCOPE);
				if (idspaceScope == null) {
					((Component)idspace).setAttribute(IDSPACE_SCOPE, idspaceScope = new HashMap());
				}
			} else if (idspace instanceof Page) {
				idspaceScope = (Map) ((Page)idspace).getAttribute(IDSPACE_SCOPE);
				if (idspaceScope == null) {
					((Page)idspace).setAttribute(IDSPACE_SCOPE, idspaceScope = new HashMap());
				}
			} else {
				throw new UiException("Unknown idspace: "+idspace);
			}
				
			Object scopedObject = idspaceScope.get(name);
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				idspaceScope.put(name, scopedObject);
			}
			return scopedObject;
		}
		throw new IllegalStateException("Unable to get idspace scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public String getConversationId() {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final Component self = ZKProxy.getProxy().getSelf((ExecutionCtrl)exec);
			final IdSpace idspace = self.getSpaceOwner();
			if (idspace instanceof Component) {
				return ((Component)idspace).getUuid();
			} else {
				return ((Page)idspace).getId();
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
			final Component self = ZKProxy.getProxy().getSelf((ExecutionCtrl)exec);
			final IdSpace idspace = self.getSpaceOwner();
			Map idspaceScope = null;
			if (idspace instanceof Component) {
				idspaceScope = (Map) ((Component)idspace).getAttribute(IDSPACE_SCOPE);
			} else {
				idspaceScope = (Map) ((Page)idspace).getAttribute(IDSPACE_SCOPE);
			}
			return idspaceScope != null ? idspaceScope.remove(name) : null;
		}
		throw new IllegalStateException("Unable to remove idspace scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public Object resolveContextualObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
