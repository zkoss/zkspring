/* ZkResourceViewResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 11, 2008 09:13:23 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.webflow.execution;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.web.servlet.View;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.mvc.servlet.ServletMvcViewFactory;
import org.springframework.webflow.mvc.view.AbstractMvcView;
import org.springframework.webflow.mvc.view.FlowViewResolver;
import org.springframework.webflow.validation.WebFlowMessageCodesResolver;

/**
 * ZK-specific {@link ViewFactory} implementation.
 * <p>
 * This factory is responsible for provide ZK specific (see {@link ZkFlowView}) 
 * that can resolve a ZK event to a Spring Web Flow event.
 * </p>
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowViewFactory extends ServletMvcViewFactory {
	public ZkFlowViewFactory(Expression viewId, FlowViewResolver viewResolver, ExpressionParser expressionParser,
			ConversionService conversionService, BinderConfiguration binderConfiguration) {
		super(viewId, viewResolver, expressionParser, conversionService, binderConfiguration, new WebFlowMessageCodesResolver());
	}
	
	protected AbstractMvcView createMvcView(View view, RequestContext context) {
		return new ZkFlowView(view, context);
	}
}
