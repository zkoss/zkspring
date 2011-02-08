/* ZkFlowContextResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 25, 2008 4:06:08 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.webflow.context.servlet;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.expression.el.RequestContextELResolver;
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Window;

/**
 * Utility class that manage the flow context (flow id, flow execution key, flow request URI, flow data context) 
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowContextManager {
	public static final String FLOW_ID = "zkoss.spring.webflow.FLOW_ID";
	public static final String FLOW_EXECUTION_KEY = "zkoss.spring.webflow.FLOW_EXECUTION_KEY";
	public static final String FLOW_REQUEST_URI = "zkoss.spring.webflow.FLOW_REQUEST_URI";
	public static final String FLOW_CONTEXT = "zkoss.spring.webflow.FLOW_CONTEXT";
	public static final String FLOW_POPUP_WINDOW = "zkoss.spring.webflow.FLOW_POPUP_WINDOW";
	public static final String SELF = "zkoss.spring.webflow.SELF";
	public static final String WRONG_VALUE = "zkoss.spring.webflow.WRONG_VALUE";

	public static void setWrongValueException(Execution exec, Exception ex) {
		ZKProxy.getProxy().setAttribute(exec, WRONG_VALUE, ex);
	}
	
	public static Exception getWrongValueException(Execution exec) {
		return exec == null ? null : (Exception) exec.getAttribute(WRONG_VALUE);
	}
	
	public static void setSelf(Execution exec, Component comp) {
		ZKProxy.getProxy().setAttribute(exec, SELF, comp);
	}
	
	public static Component getSelf(Execution exec) {
		return (Component) exec.getAttribute(SELF);
	}
	
	public static String getFlowId(Execution exec) {
		final Component self  = getSelf(exec);
		return self != null ? (String) self.getVariable(FLOW_ID, false) : null;
	}
	
	public static String getFlowExecutionKey(Execution exec) {
		final Component self = getSelf(exec);
		return self != null ? (String) self.getVariable(FLOW_EXECUTION_KEY, false) : null;
	}
	
	public static String getFlowRequestURI(Execution exec) {
		final Component self = getSelf(exec);
		return self != null ? (String) self.getVariable(FLOW_REQUEST_URI, false) : null;
	}
	
	public static RequestContext getFlowRequestContext(Execution exec) {
		final Component self = getSelf(exec);
		return self != null ? (RequestContext) self.getVariable(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME, false) : null;
	}

	public static Window getPopupWindow(Execution exec) {
		final Component self = getSelf(exec);
		return self != null ? (Window) self.getVariable(FLOW_POPUP_WINDOW, false) : null;
	}
	
	public static void storeFlowContext(Execution exec) {
		//remember ZK Flow control parameters to current Desktop
		final String flowId = (String) exec.getAttribute(FLOW_ID);

		if (flowId != null) {
			//Store FLOW related context in current ZK view state root
			final Object popupWin = exec.getAttribute(FLOW_POPUP_WINDOW);
			if (popupWin != null) {
				storeFlowContextInPopupWin((Window)popupWin, exec);
			} else {
				storeFlowContextInPages(exec);
			}
		}
	}
	
	private static void storeFlowContextInPopupWin(Window win, Execution exec) {
		win.setVariable(FLOW_POPUP_WINDOW, win, true);
		win.setVariable(FLOW_ID, exec.getAttribute(FLOW_ID), true);
		win.setVariable(FLOW_EXECUTION_KEY, exec.getAttribute(FLOW_EXECUTION_KEY), true);
		win.setVariable(FLOW_REQUEST_URI, exec.getAttribute(FLOW_REQUEST_URI), true);
		win.setVariable(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME, 
				exec.getAttributes().get(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME), true);
	}
	
	private static void storeFlowContextInPages(Execution exec) {
		final Desktop desktop = exec.getDesktop();
		final Collection pages= desktop.getPages();
		for (final Iterator it = pages.iterator(); it.hasNext();) {
			final Page page = (Page) it.next();
			page.setVariable(FLOW_ID, exec.getAttribute(FLOW_ID));
			page.setVariable(FLOW_EXECUTION_KEY, exec.getAttribute(FLOW_EXECUTION_KEY));
			page.setVariable(FLOW_REQUEST_URI, exec.getAttribute(FLOW_REQUEST_URI));
			page.setVariable(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME, 
					exec.getAttributes().get(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME));
		}
	}
}
