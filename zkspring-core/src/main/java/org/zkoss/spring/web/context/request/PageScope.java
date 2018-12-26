/* PageScope.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2008 6:12:28 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

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
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.sys.ExecutionCtrl;

/**
 * ZK Page scope; accessible only in ZK event handling request.
 *  
 * @author henrichen
 * @since 1.2
 */
public class PageScope implements Scope {
	private static final String PAGE_SCOPE = "ZK_SPRING_PAGE_SCOPE";
	
	public Object get(String name, ObjectFactory<?> objectFactory) {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final Page page = ((ExecutionCtrl)exec).getCurrentPage();
			Map pageScope = (Map) page.getAttribute(PAGE_SCOPE);
			if (pageScope == null) {
				page.setAttribute(PAGE_SCOPE, pageScope = new HashMap());
			}
			Object scopedObject = pageScope.get(name);
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				pageScope.put(name, scopedObject);
			}
			return scopedObject;
		}
		throw new IllegalStateException("Unable to get page scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public String getConversationId() {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final Page page = ((ExecutionCtrl)exec).getCurrentPage();
			if (page != null) {
				return page.getId();
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
			final Page page = ((ExecutionCtrl)exec).getCurrentPage();
			final Map pageScope = (Map) page.getAttribute(PAGE_SCOPE);
			return pageScope != null ? pageScope.remove(name) : null;
		}
		throw new IllegalStateException("Unable to get page scope bean: "+name+". Do you access it in ZK event listener?");
	}

	public Object resolveContextualObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
