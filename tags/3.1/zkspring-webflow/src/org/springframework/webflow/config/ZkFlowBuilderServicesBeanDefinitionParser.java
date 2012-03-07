/* ZkEventProcessDefinitionSourceBeanDefinitionParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 1, 2008 7:17:38 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.springframework.webflow.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.zkoss.spring.web.servlet.view.ZkView;
import org.zkoss.spring.webflow.config.ZkBeanIds;

/**
 * Allows for convenient creation of a {@link ZkEventProcessDefinitionSource} 
 * bean for use with a ZkEventProcessInterceptor.
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowBuilderServicesBeanDefinitionParser 
extends	FlowBuilderServicesBeanDefinitionParser {
	
	private static final String ZK_EL_EXPRESSION_PARSER_CLASS_NAME = "org.zkoss.spring.webflow.expression.el.ZkExpressionParser";

	private static final String ZK_CONVERSION_SERVICE_CLASS_NAME = "org.zkoss.spring.binding.convert.service.ZkConversionService";

	private static final String MVC_VIEW_FACTORY_CREATOR_CLASS_NAME = "org.zkoss.spring.webflow.engine.builder.ZkFlowViewFactoryCreator";

	private static final String MVC_FLOW_VIEW_RESOLVER_CLASS_NAME = "org.zkoss.spring.web.servlet.view.ZkFlowResourceViewResolver";
	
	private static final String DEFAULT_EXPRESSION_FACTORY = "org.jboss.el.ExpressionFactoryImpl";

	private static final String EXPRESSION_PARSER_PROPERTY = "expressionParser";
	private static final String CONVERSION_SERVICE_PROPERTY = "conversionService";
	private static final String VIEW_FACTORY_CREATOR_PROPERTY = "viewFactoryCreator";



	//override
	protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		registerConversionService(element, context, builder);
		registerExpressionParser(element, context, builder);
		registerMvcViewFactoryCreator(element, context, builder);
	}

	private void registerConversionService(Element element, ParserContext pc, BeanDefinitionBuilder definitionBuilder) { 
		BeanDefinitionBuilder conversionServiceBuilder = BeanDefinitionBuilder
		.genericBeanDefinition(ZK_CONVERSION_SERVICE_CLASS_NAME);
        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_CONVERSION_SERVICE, conversionServiceBuilder.getBeanDefinition());
        definitionBuilder.addPropertyReference(CONVERSION_SERVICE_PROPERTY, ZkBeanIds.ZK_CONVERSION_SERVICE);
	}
	
	private void registerExpressionParser(Element element, ParserContext pc, BeanDefinitionBuilder definitionBuilder) {
			BeanDefinitionBuilder unifiedExpressionFactory = BeanDefinitionBuilder
					.genericBeanDefinition(DEFAULT_EXPRESSION_FACTORY);
			BeanDefinitionBuilder zkElExpressionParserBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(ZK_EL_EXPRESSION_PARSER_CLASS_NAME);
			zkElExpressionParserBuilder
					.addConstructorArgValue(unifiedExpressionFactory
							.getBeanDefinition());
			pc.getRegistry()
			.registerBeanDefinition(ZkBeanIds.ZK_EXPRESSION_PARSER,
					zkElExpressionParserBuilder.getBeanDefinition());
			definitionBuilder.addPropertyReference(EXPRESSION_PARSER_PROPERTY,
					ZkBeanIds.ZK_EXPRESSION_PARSER);
	}
	

	private void registerMvcViewFactoryCreator(Element element, ParserContext pc, BeanDefinitionBuilder definitionBuilder) { 
		BeanDefinitionBuilder viewFactoryCreatorBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(MVC_VIEW_FACTORY_CREATOR_CLASS_NAME);
			registerViewResolvers(element, pc, viewFactoryCreatorBuilder);
	        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_FLOW_VIEW_FACTORY_CREATOR, viewFactoryCreatorBuilder.getBeanDefinition());
	        definitionBuilder.addPropertyReference(VIEW_FACTORY_CREATOR_PROPERTY, ZkBeanIds.ZK_FLOW_VIEW_FACTORY_CREATOR);
	}
	private void registerViewResolvers(Element element, ParserContext pc, BeanDefinitionBuilder rootBuilder) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
		.genericBeanDefinition(MVC_FLOW_VIEW_RESOLVER_CLASS_NAME);
		//TODO: allow config prefix and suffix
        builder.addPropertyValue("suffix", ".zul");
        builder.addPropertyValue("viewClass", ZkView.class);
        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_FLOW_RESOURCE_VIEW_RESOLVER, builder.getBeanDefinition());
        rootBuilder.addPropertyReference("viewResolvers", ZkBeanIds.ZK_FLOW_RESOURCE_VIEW_RESOLVER);
	}
}
