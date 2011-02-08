/* ZkFlowControllerListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 4, 2008 3:15:17 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.webflow.mvc.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.webflow.mvc.servlet.FlowController;
import org.zkoss.spring.SpringUtil;
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.spring.js.ajax.ZkAjaxHandler;
import org.zkoss.spring.webflow.context.servlet.ZkFlowContextManager;
import org.zkoss.util.CollectionsX;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.Annotation;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.util.EventInterceptor;

/**
 * A bridge listener to dispatch ZK actions to a Flow.
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowControllerListener implements EventInterceptor {
	private static final String DEFAULT_FLOW_CONTROLLER_NAME = "flowController";
	
	private FlowController _flowController;

	/**
	 * Check the event id and route to FlowExecutor
	 */
	public Event beforeProcessEvent(Event event) {
		//remember the current event target component
		final Component comp = event.getTarget();
		ZkFlowContextManager.setSelf(Executions.getCurrent(), comp);
		
		return event;
	}

	public void afterProcessEvent(Event event) {
		if (_flowController == null) {
			final String name = DEFAULT_FLOW_CONTROLLER_NAME;
			_flowController = (FlowController) SpringUtil.getBean(name, FlowController.class);
		}
		if (_flowController != null) {
			final String eventName = event.getName(); 
			if (eventName.equals(ZkAjaxHandler.POPUP_EVENT)){ //for flow popup
				final Execution exec = Executions.getCurrent();
				
				//Do the Flow state transition operation
				final HttpServletRequest request = new PopupHttpServletRequestWrapper(
					(HttpServletRequest)exec.getNativeRequest(), (String)event.getData());
				final HttpServletResponse response = (HttpServletResponse) exec.getNativeResponse();
				final Object oldFlowEvent = exec.getAttribute("action");
				final Object oldEvent = exec.getAttribute("actionEvent");
				ZKProxy.getProxy().removeAttribute(exec, "action"); //virtual forwarding, not a event handling
				ZKProxy.getProxy().removeAttribute(exec, "actionEvent"); //virtual forwarding, not a event handling
				try {
					_flowController.handleRequest(request, response);
				} catch (Exception e) {
					throw UiException.Aide.wrap(e);
				} finally {
					ZKProxy.getProxy().setAttribute(exec, "action", oldFlowEvent);
					ZKProxy.getProxy().setAttribute(exec, "actionEvent", oldEvent);
				}
			} else {
				final Component comp = event.getTarget();
				final Annotation annt = ((ComponentCtrl)comp).getAnnotation("action"); //@{action(search)}
				if (annt != null) {
					String flowWhen = annt.getAttribute("when");
					if (flowWhen == null) { //default to "onClick"
						flowWhen = "onClick";
					}
					final String flowEvent = annt.getAttribute("value");
					if (flowEvent != null && eventName.equals(flowWhen)) {
						final Execution exec = Executions.getCurrent();
						
						//see ZkELResolver
						//see ZkExpressionParser
						ZKProxy.getProxy().setAttribute(exec, "action", flowEvent);
						ZKProxy.getProxy().setAttribute(exec, "actionEvent", event);
						
						//Do the Flow state transition operation
						final HttpServletRequest request = (HttpServletRequest) exec.getNativeRequest();
						final HttpServletResponse response = (HttpServletResponse) exec.getNativeResponse();
						try {
							_flowController.handleRequest(request, response);
						} catch (Exception e) {
							throw UiException.Aide.wrap(e);
						}
					}
				}
			}
		}
	}
	
	public Event beforePostEvent(Event event) {
		return event;
	}

	public Event beforeSendEvent(Event event) {
		return event;
	}
	
	private static class PopupHttpServletRequestWrapper extends HttpServletRequestWrapper {
		private Map _paramMap;
		private String _contextPath;
		private String _pathInfo;
		public PopupHttpServletRequestWrapper(HttpServletRequest request, String targetUrl) {
			super(request);
			_paramMap = new HashMap();
			final int j = targetUrl.lastIndexOf('?');
			if (j >= 0) {
				_pathInfo = targetUrl.substring(0, j);
				final String paramsStr = targetUrl.substring(j+1);
				final Collection paramsList = 
					CollectionsX.parse(new ArrayList(), paramsStr, '&');
				for (final Iterator it=paramsList.iterator(); it.hasNext();) {
					final String pair = (String) it.next();
					final int k = pair.indexOf('=');
					if (k >= 0) {
						final String key = pair.substring(0, k).trim();
						final String val = (k+1) >= pair.length() ? null : pair.substring(k+1).trim();
						putInMap(key, val);
					} else {
						final String key = pair.trim();
						final String val = null;
						putInMap(key, val);
					}
				}
			} else {
				_pathInfo = targetUrl;
			}
			_contextPath = request.getContextPath();
		}
		private void putInMap(String key, String val) {
			if (key != null && key.length() > 0) {
				List vals = (List) _paramMap.get(key);
				if (vals == null && val != null) {
					vals = new ArrayList();
					_paramMap.put(key, vals);
				}
				if (val != null) {
					vals.add(val);
				}
			}
		}
		//override
		public String getParameter(String name) {
			String[] values = getParameterValues(name);
			return values != null && (values.length >= 1) ? values[0] : null; 
		}
		//override
		public Map getParameterMap() {
			return Collections.unmodifiableMap(_paramMap);
		}
		//override
		public String getRequestURI() {
			return _contextPath + _pathInfo;
		}
		//override
		public Enumeration getParameterNames() {
			return Collections.enumeration(getParameterMap().keySet());
		}
		//override
		public String[] getParameterValues(String name) {
			final List vals = (List) getParameterMap().get(name);
			return vals == null ? null : (String[]) vals.toArray(new String[vals.size()]);
		}
	}
}
