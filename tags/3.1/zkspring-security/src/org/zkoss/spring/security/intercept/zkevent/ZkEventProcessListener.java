/* ZkEventProcessListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 1, 2008 6:17:08 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.intercept.zkevent;

import org.zkoss.spring.SpringUtil;
import org.zkoss.spring.security.ui.ZkExceptionTranslationListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.EventInterceptor;

/**
 * Secure event processing (per the component path and event name).
 * 
 * @author henrichen
 * @since 1.0
 */
public class ZkEventProcessListener implements EventInterceptor {
	private static final String DEFAULT_EVENT_PROCESS_INTERCEPTOR_NAME = "zkEventProcessInterceptor";
	
	private ZkEventProcessInterceptor _interceptor;
	
	public void afterProcessEvent(Event event) {
		_interceptor.afterInvocation(event);
	}

	public Event beforeProcessEvent(Event event) {
		//if in ZK event exception translation, then ignore any further events
		if (Executions.getCurrent().getAttribute(ZkExceptionTranslationListener.ZK_EXCEPTION_TRANSLATION) != null) {
			return event;
		}
		final String name = DEFAULT_EVENT_PROCESS_INTERCEPTOR_NAME;
		_interceptor = (ZkEventProcessInterceptor) 
			SpringUtil.getBean(name, ZkEventProcessInterceptor.class);
		if (_interceptor == null) {
			_interceptor = new ZkEventProcessInterceptor(); 
		}
		
		return _interceptor.beforeInvocation(event);
	}

	public Event beforePostEvent(Event event) {
		//do nothing
		return event;
	}

	public Event beforeSendEvent(Event event) {
		//do nothing
		return event;
	}
}
