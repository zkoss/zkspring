/* LoginTemplateComposer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 25, 2008 8:00:52 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.web.servlet.view;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Creates Spring-MVC Internal Resource view to render a flow-relative view 
 * resource such as a JSP template.
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowResourceViewResolver extends ZkResourceViewResolver {
	private String getFlowRelativeUrl(String viewId) {
		ApplicationContext flowContext = RequestContextHolder.getRequestContext().getActiveFlow().getApplicationContext();
		if (flowContext == null) {
			throw new IllegalStateException("A Flow ApplicationContext is required to resolve Flow View Resources");
		}
		Resource viewResource = flowContext.getResource(viewId);
		if (!(viewResource instanceof ContextResource)) {
			throw new IllegalStateException(
					"A ContextResource is required to get relative view paths within this context. view: "+viewId);
		}
		return ((ContextResource) viewResource).getPathWithinContext();
	}
	
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		final String viewPath = getFlowRelativeUrl(viewName); 
		return (AbstractUrlBasedView) super.buildView(viewPath);
	}
}
