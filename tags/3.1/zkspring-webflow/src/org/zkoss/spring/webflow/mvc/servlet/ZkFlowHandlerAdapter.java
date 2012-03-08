/* ZkFlowHandlerAdapter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 13, 2008 11:09:27 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.webflow.mvc.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
import org.zkoss.spring.webflow.context.servlet.ZkFlowContextManager;
import org.zkoss.spring.webflow.context.servlet.ZkFlowResourceListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.http.WebManager;
import org.zkoss.zk.ui.util.Configuration;

/**
 * Adapter for ZK Spring Web Flow (Spring MVC)
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowHandlerAdapter extends FlowHandlerAdapter {
    protected final Log logger = LogFactory.getLog(getClass());

    private WebApp _webApp;
		
	private void setupZkFlowListeners() {
		if (_webApp == null) {
			final WebManager webman = WebManager.getWebManager(getServletContext());
			_webApp = webman.getWebApp();
			Configuration conf = _webApp.getConfiguration();
			try {
				conf.addListener(ZkFlowControllerListener.class);
				conf.addListener(ZkFlowResourceListener.class);
			} catch (Exception e) {
				//log and ignore
		        if (logger.isDebugEnabled()) {
		            logger.debug("Added ZK Spring Listeners Failed: " + e);
		        }
			}
		}
	}
	
	public boolean supports(Object handler) {
		return (handler instanceof Controller) || super.supports(handler);
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
	throws Exception {
		setupZkFlowListeners();
		
		//SimpleControllerHandler
		if (handler instanceof Controller) {
			return ((Controller)handler).handleRequest(request, response);
		} else {
			final ModelAndView mav = super.handle(request, response, handler);
			//if shall throw Exception in ZkView.renderFragmentOrRedirect()
			//throw it here
			final Exception ex = ZkFlowContextManager.getWrongValueException(Executions.getCurrent());
			if (ex != null) {
				throw ex;
			}
			return mav;
		}
	}

	//override, super class does not handle ZK Ajax case
	protected void defaultHandleExecutionOutcome(String flowId, FlowExecutionOutcome outcome,
			ServletExternalContext context, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (!context.isResponseComplete()) {
			final AjaxHandler ajaxHandler = getAjaxHandler();
			if (ajaxHandler.isAjaxRequest(request, response)) {
				final String url = getFlowUrlHandler().createFlowDefinitionUrl(flowId, outcome.getOutput(), request); 
				ajaxHandler.sendAjaxRedirect(url, request, response, false);
			} else {
				super.defaultHandleExecutionOutcome(flowId, outcome, context, request, response);
			}
		}
	}
}
