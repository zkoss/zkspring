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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.zkoss.spring.binding.convert.service.ZkConversionService;
import org.zkoss.spring.webflow.config.ZkBeanIds;
import org.zkoss.spring.web.servlet.view.ZkFlowResourceViewResolver;
import org.zkoss.spring.web.servlet.view.ZkView;
import org.zkoss.spring.webflow.engine.builder.ZkFlowViewFactoryCreator;
import org.zkoss.spring.webflow.expression.el.ZkExpressionParser;

/**
 * Allows for convenient creation of a {@link ZkEventProcessDefinitionSource} 
 * bean for use with a ZkEventProcessInterceptor.
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowBuilderServicesBeanDefinitionParser 
extends	AbstractSimpleBeanDefinitionParser {
	private static final String FLOW_BUILDER_SERVICES_CLASS_NAME = "org.springframework.webflow.engine.builder.support.FlowBuilderServices";

	private static final String SPRING_EL_EXPRESSION_PARSER_CLASS_NAME = "org.springframework.expression.spel.standard.SpelExpressionParser";
	private static final String ZK_EL_EXPRESSION_PARSER_CLASS_NAME = "org.zkoss.spring.webflow.expression.el.ZkExpressionParser";

	private static final String ZK_CONVERSION_SERVICE_CLASS_NAME = "org.zkoss.spring.binding.convert.service.ZkConversionService";

	private static final String MVC_VIEW_FACTORY_CREATOR_CLASS_NAME = "org.zkoss.spring.webflow.engine.builder.ZkFlowViewFactoryCreator";

	private static final String DEFAULT_EXPRESSION_FACTORY = "org.jboss.el.ExpressionFactoryImpl";

	private static final String EXPRESSION_PARSER_ATTR = "expression-parser";
	private static final String EXPRESSION_PARSER_PROPERTY = "expressionParser";
	private static final String CONVERSION_SERVICE_PROPERTY = "conversionService";
	private static final String VIEW_FACTORY_CREATOR_ATTR = "view-factory-creator";
	private static final String VIEW_FACTORY_CREATOR_PROPERTY = "viewFactoryCreator";



	protected String getBeanClassName(Element element) {
		return FLOW_BUILDER_SERVICES_CLASS_NAME;
	}

	//override
	protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		registerConversionService(element, context, builder);
		registerExpressionParser(element, context, builder);
		registerMvcViewFactoryCreator(element, context, builder);
	}
	private String registerInfrastructureComponent(Element element, ParserContext context,
			BeanDefinitionBuilder componentBuilder, String beanName) {
//		String beanName = context.getReaderContext().generateBeanName(componentBuilder.getRawBeanDefinition());
		componentBuilder.getRawBeanDefinition().setSource(context.extractSource(element));
		componentBuilder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_APPLICATION);
		context.registerBeanComponent(new BeanComponentDefinition(componentBuilder.getBeanDefinition(), beanName));
		return beanName;
	}

	private void registerConversionService(Element element, ParserContext pc, BeanDefinitionBuilder definitionBuilder) { 
//        final BeanDefinitionBuilder builder = 
//        	BeanDefinitionBuilder.rootBeanDefinition(ZkConversionService.class);
		BeanDefinitionBuilder conversionServiceBuilder = BeanDefinitionBuilder
		.genericBeanDefinition(ZK_CONVERSION_SERVICE_CLASS_NAME);
        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_CONVERSION_SERVICE, conversionServiceBuilder.getBeanDefinition());
        definitionBuilder.addPropertyReference("conversionService", ZkBeanIds.ZK_CONVERSION_SERVICE);
//		String conversionService = registerInfrastructureComponent(element, pc, conversionServiceBuilder,ZkBeanIds.ZK_CONVERSION_SERVICE);
//		definitionBuilder.addPropertyReference(CONVERSION_SERVICE_PROPERTY, conversionService);
	}
	private void registerExpressionParser(Element element, ParserContext pc, BeanDefinitionBuilder definitionBuilder) {
//		String expressionParser = element.getAttribute(EXPRESSION_PARSER_ATTR);
//		if (!StringUtils.hasText(expressionParser)) {

//			BeanDefinitionBuilder springElExpressionParserBuilder = BeanDefinitionBuilder
//					.genericBeanDefinition(SPRING_EL_EXPRESSION_PARSER_CLASS_NAME);
//			BeanDefinitionBuilder builder = BeanDefinitionBuilder
//					.genericBeanDefinition(ZkExpressionParser.class);
//			builder.addConstructorArgValue(springElExpressionParserBuilder
//							.getBeanDefinition());
//

			BeanDefinitionBuilder springElExpressionParserBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(DEFAULT_EXPRESSION_FACTORY);
			BeanDefinitionBuilder zkElExpressionParserBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(ZK_EL_EXPRESSION_PARSER_CLASS_NAME);
			zkElExpressionParserBuilder
					.addConstructorArgValue(springElExpressionParserBuilder
							.getBeanDefinition());
//			zkElExpressionParserBuilder
//					.addConstructorArgReference(getConversionService(definitionBuilder));
			pc.getRegistry()
			.registerBeanDefinition(ZkBeanIds.ZK_EXPRESSION_PARSER,
					zkElExpressionParserBuilder.getBeanDefinition());
			definitionBuilder.addPropertyReference("expressionParser",
					ZkBeanIds.ZK_EXPRESSION_PARSER);

//			expressionParser = registerInfrastructureComponent(element,
//					pc, webFlowElExpressionParserBuilder,ZkBeanIds.ZK_EXPRESSION_PARSER);
//		} 
//		definitionBuilder.addPropertyReference(EXPRESSION_PARSER_PROPERTY, expressionParser);
	}
	
	private String getConversionService(BeanDefinitionBuilder definitionBuilder) {
		RuntimeBeanReference conversionServiceReference = (RuntimeBeanReference) definitionBuilder.getBeanDefinition().getPropertyValues().getPropertyValue(CONVERSION_SERVICE_PROPERTY).getValue();
		return conversionServiceReference.getBeanName();
	}

	private void registerMvcViewFactoryCreator(Element element, ParserContext pc, BeanDefinitionBuilder definitionBuilder) { 
//        final BeanDefinitionBuilder builder = 
//        	BeanDefinitionBuilder.rootBeanDefinition(ZkFlowViewFactoryCreator.class);
//        registerViewResolvers(element, pc, builder);
//		String viewFactoryCreator = element.getAttribute(VIEW_FACTORY_CREATOR_ATTR);
//		if (!StringUtils.hasText(viewFactoryCreator)) {

		BeanDefinitionBuilder viewFactoryCreatorBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(MVC_VIEW_FACTORY_CREATOR_CLASS_NAME);
			registerViewResolvers(element, pc, viewFactoryCreatorBuilder);
	        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_FLOW_VIEW_FACTORY_CREATOR, viewFactoryCreatorBuilder.getBeanDefinition());
	        definitionBuilder.addPropertyReference("viewFactoryCreator", ZkBeanIds.ZK_FLOW_VIEW_FACTORY_CREATOR);

//			viewFactoryCreator = registerInfrastructureComponent(element, pc, viewFactoryCreatorBuilder, ZkBeanIds.ZK_FLOW_VIEW_FACTORY_CREATOR);
//		}
//		definitionBuilder.addPropertyReference(VIEW_FACTORY_CREATOR_PROPERTY, viewFactoryCreator);
	}
	private void registerViewResolvers(Element element, ParserContext pc, BeanDefinitionBuilder rootBuilder) {
        final BeanDefinitionBuilder builder = 
        	BeanDefinitionBuilder.rootBeanDefinition(ZkFlowResourceViewResolver.class);
		//TODO: allow config prefix and suffix
        builder.addPropertyValue("suffix", ".zul");
        builder.addPropertyValue("viewClass", ZkView.class);
        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_FLOW_RESOURCE_VIEW_RESOLVER, builder.getBeanDefinition());
        rootBuilder.addPropertyReference("viewResolvers", ZkBeanIds.ZK_FLOW_RESOURCE_VIEW_RESOLVER);
	}
}
