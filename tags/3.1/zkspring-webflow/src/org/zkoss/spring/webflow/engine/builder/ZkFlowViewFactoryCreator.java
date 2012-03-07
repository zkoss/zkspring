/* ZkFlowViewFactoryCreator.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 12, 2008 4:13:23 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.webflow.engine.builder;

import java.util.List;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.mvc.builder.DelegatingFlowViewResolver;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;
import org.springframework.webflow.mvc.view.FlowViewResolver;
import org.zkoss.spring.webflow.execution.ZkFlowViewFactory;

/**
 * Returns {@link ViewFactory} that create ZK event based views. 
 * Used by a FlowBuilder to configure a flow's view states.
 * 
 * <p>Like other Spring native implmentation, this one creates 
 * view factories that resolve their views by loading flow-relative 
 * resources, such as .zul templates located in a flow working directory. 
 * This class also supports rendering views resolved by
 * pre-existing Spring MVC {@link ViewResolver}.
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowViewFactoryCreator extends MvcViewFactoryCreator {
	private FlowViewResolver flowViewResolver;
	
	public ZkFlowViewFactoryCreator() {
	}

	//-- ViewFactoryCreator
	public ViewFactory createViewFactory(Expression viewId, ExpressionParser expressionParser,
			ConversionService conversionService, BinderConfiguration binderConfiguration) {
		AbstractMvcViewFactory viewFactory = createMvcViewFactory(viewId, expressionParser, conversionService,
				binderConfiguration);
		return viewFactory;
	}

	/**
	 * Sets the chain of Spring MVC {@link ViewResolver view resolvers} to delegate to resolve views selected by flows.
	 * Allows for reuse of existing View Resolvers configured in a Spring application context. If multiple resolvers are
	 * to be used, the resolvers should be ordered in the manner they should be applied.
	 * @param viewResolvers the view resolver list
	 */
	public void setViewResolvers(List viewResolvers) {
		flowViewResolver = new DelegatingFlowViewResolver(viewResolvers);
		setFlowViewResolver(flowViewResolver);
	}

	protected AbstractMvcViewFactory createMvcViewFactory(Expression viewId, ExpressionParser expressionParser,
			ConversionService conversionService, BinderConfiguration binderConfiguration) {
		return new ZkFlowViewFactory(viewId, flowViewResolver, expressionParser, conversionService,
					binderConfiguration);
	}
}
