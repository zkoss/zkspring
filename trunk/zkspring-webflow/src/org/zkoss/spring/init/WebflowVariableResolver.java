/* WebflowVariableResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 25, 2008 2:10:00 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.init;

import java.io.Serializable;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.expression.el.RequestContextELResolver;
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.spring.webflow.context.servlet.ZkFlowContextManager;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

/**
 * A Spring Web Flow related variables resolver.
 * @author henrichen
 * @since 1.1
 */
public class WebflowVariableResolver implements VariableResolver, Serializable {
	transient private static final String IN_GETTING_FLOW_CTX = "zkoss.spring.webflow.IN_GETTING_FLOW_CTX";

	//Resolve web flow relative variables
	public Object resolveVariable(String name) throws XelException {
		final Execution exec = Executions.getCurrent();
		if ("currentUser".equals(name) && exec != null) { 
			return Executions.getCurrent().getUserPrincipal();
		}
		
		RequestContext flowctx = (RequestContext) exec.getAttribute(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME);  
		if (flowctx == null) {
			final Component target = (Component) ZkFlowContextManager.getSelf(exec);
			if (target != null) {
				if (exec.getAttribute(IN_GETTING_FLOW_CTX) != null) { //avoid endless loop
					return null;
				}
				ZKProxy.getProxy().setAttribute(exec, IN_GETTING_FLOW_CTX, Boolean.TRUE);
				try {
					flowctx = (RequestContext) target.getVariable(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME, false); //recursive
				} finally {
					ZKProxy.getProxy().removeAttribute(exec, IN_GETTING_FLOW_CTX);
				}
			}
		}
		if (flowctx != null) {
			//check special EL variables
			if ("flowScope".equals(name)) {
				return flowctx.getFlowScope();
			} else if ("viewScope".equals(name)) {
				return flowctx.getViewScope();
			} else if ("requestScope".equals(name)) {
				return flowctx.getRequestScope();
			} else if ("flshScope".equals(name)) {
				return flowctx.getFlashScope();
			} else if ("conversationScope".equals(name)) {
				return flowctx.getConversationScope();
			} else if ("requestParameters".equals(name)) {
				return flowctx.getRequestParameters();
			} else if ("currentEvent".equals(name)) {
				return flowctx.getCurrentEvent();
			} else if ("currentUser".equals(name)) {
				return Executions.getCurrent().getUserPrincipal();
			} else if ("messageContext".equals(name)) {
				return flowctx.getMessageContext();
			} else if ("resourceBundle".equals(name)) {
				return flowctx.getActiveFlow().getApplicationContext();
				//throw new UiException("Unsupported variable: "+name);
			} else if ("flowRequestContext".equals(name)) {
				return flowctx;
			} else if ("flowExecutionContext".equals(name)) {
				return flowctx.getFlowExecutionContext();
			} else if ("flowExceutionUrl".equals(name)) {
				return flowctx.getFlowExecutionUrl();
			} else if ("externalContext".equals(name)) {
				return flowctx.getExternalContext();
			}
			//check requestScope
			final MutableAttributeMap requestScope = flowctx.getRequestScope();
			if (requestScope.contains(name)) {
				return requestScope.get(name);
			}
			//check flashScope
			final MutableAttributeMap flashScope = flowctx.getFlashScope();
			if (flashScope.contains(name)) {
				return flashScope.get(name);
			}
			//check viewScope
			final MutableAttributeMap viewScope = flowctx.getViewScope();
			if (viewScope.contains(name)) {
				return viewScope.get(name);
			}
			//check flowScope
			final MutableAttributeMap flowScope = flowctx.getFlowScope();
			if (flowScope.contains(name)) {
				return flowScope.get(name);
			}
			//check conversationScope
			final MutableAttributeMap conversationScope = flowctx.getConversationScope();
			if (conversationScope.contains(name)) {
				return conversationScope.get(name);
			}
		}
		return null;
	}
	public int hashCode() {
		return getClass().hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

}
