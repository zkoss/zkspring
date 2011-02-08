/* ZkExpressionParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 5, 2008 11:03:30 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT

Partial codes is based on the Spring Web Flow's WebFlowELExpressionParser
implementation by Keith Donald.
*/
package org.zkoss.spring.webflow.expression.el;

import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

import org.springframework.binding.expression.el.DefaultELResolver;
import org.springframework.binding.expression.el.ELContextFactory;
import org.springframework.binding.expression.el.ELExpressionParser;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.expression.el.ActionMethodELResolver;
import org.springframework.webflow.expression.el.FlowResourceELResolver;
import org.springframework.webflow.expression.el.ImplicitFlowVariableELResolver;
import org.springframework.webflow.expression.el.RequestContextELResolver;
import org.springframework.webflow.expression.el.ScopeSearchingELResolver;
import org.springframework.webflow.expression.el.SpringBeanWebFlowELResolver;

/**
 * A ZK-specific ExpressionParser that allows beans and components
 * managed by ZK, Spring, or Web Flow to be referenced in
 * expressions in the FlowDefinition.
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkExpressionParser extends ELExpressionParser {

	/**
	 * Creates a new ZK Web Flow EL expression parser.
	 */
	public ZkExpressionParser(ExpressionFactory expressionFactory) {
		super(expressionFactory);
		putContextFactory(RequestContext.class, new ZkRequestContextELContextFactory());
	}

	private static class ZkRequestContextELContextFactory implements ELContextFactory {
		public ELContext getELContext(Object target) {
			final RequestContext context = (RequestContext) target;
			final List resolvers = new ArrayList();
			resolvers.add(new RequestContextELResolver(context));
			resolvers.add(new FlowResourceELResolver(context));
			resolvers.add(new ImplicitFlowVariableELResolver(context));
			resolvers.add(new ScopeSearchingELResolver(context));
			resolvers.add(new SpringBeanWebFlowELResolver(context));
			resolvers.add(new ActionMethodELResolver());
			resolvers.add(new ZkELResolver()); //resolve ZK variables and implicit object
			final ELResolver resolver = new DefaultELResolver(resolvers);
			return new ZkWebFlowELContext(resolver);
		}
	}

	private static class ZkWebFlowELContext extends ELContext {

		private ELResolver _resolver;

		public ZkWebFlowELContext(ELResolver resolver) {
			_resolver = resolver;
		}

		public ELResolver getELResolver() {
			return _resolver;
		}

		public FunctionMapper getFunctionMapper() {
			return null;
		}

		public VariableMapper getVariableMapper() {
			return null;
		}
	}

}