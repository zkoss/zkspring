/* ZkFlowControllerBeanDefinitionParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 24, 2008 5:18:06 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.springframework.webflow.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.webflow.mvc.servlet.FlowController;
import org.w3c.dom.Element;
import org.zkoss.spring.js.ajax.ZkAjaxHandler;
import org.zkoss.spring.webflow.config.ZkBeanIds;
import org.zkoss.spring.webflow.context.servlet.ZkFlowUrlHandler;
import org.zkoss.spring.webflow.mvc.servlet.ZkFlowHandlerAdapter;

/**
 * <zksp:flow-controller flow-executor=""/>
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowControllerBeanDefinitionParser 
extends AbstractSingleBeanDefinitionParser {
	private static final String FLOW_EXECUTOR = "flow-executor";
	private static final String ZK_FLOW_CONTROLLER_CLASS = "org.springframework.webflow.mvc.servlet.FlowController";
	private static final String ZK_FLOW_HANDLER_ADAPTER_CLASS = "org.zkoss.spring.webflow.mvc.servlet.ZkFlowHandlerAdapter";
	private static final String ZK_FLOW_URL_HANDLER_CLASS = "org.zkoss.spring.webflow.context.servlet.ZkFlowUrlHandler";
	private static final String ZK_FLOW_AJAX_HANDLER_CLASS = "org.zkoss.spring.js.ajax.ZkAjaxHandler";
	
	




	protected Class getBeanClass(Element element) {
		return FlowController.class;
	}
	protected String getBeanClassName(Element element) {
		return ZK_FLOW_CONTROLLER_CLASS;
	}
	protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		registerFlowHandlerAdaptor(element, context, builder);
	}
	
	private void registerFlowHandlerAdaptor(Element elm, ParserContext pc, BeanDefinitionBuilder rootBuilder) {
//        final BeanDefinitionBuilder builder = 
//        	BeanDefinitionBuilder.rootBeanDefinition(ZkFlowHandlerAdapter.class);
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
		.genericBeanDefinition(ZK_FLOW_HANDLER_ADAPTER_CLASS);
		
        registerFlowExecutor(elm, pc, builder);
        registerFlowUrlHandler(elm, pc, builder);
        registerAjaxHandler(elm, pc, builder);
        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_FLOW_HANDLER_ADAPTER, builder.getBeanDefinition());
        rootBuilder.addPropertyReference("flowHandlerAdapter", ZkBeanIds.ZK_FLOW_HANDLER_ADAPTER);

//		String flowHandlerAdapter = registerInfrastructureComponent(elm, pc, builder,ZkBeanIds.ZK_FLOW_HANDLER_ADAPTER);
//		rootBuilder.addPropertyReference("flowHandlerAdapter", flowHandlerAdapter);
	}
	
	private void registerFlowUrlHandler(Element elm, ParserContext pc, BeanDefinitionBuilder rootBuilder) {
//        final BeanDefinitionBuilder builder = 
//        	BeanDefinitionBuilder.rootBeanDefinition(ZkFlowUrlHandler.class);
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
			.genericBeanDefinition(ZK_FLOW_URL_HANDLER_CLASS);
        pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_FLOW_URL_HANDLER, builder.getBeanDefinition());
        rootBuilder.addPropertyReference("flowUrlHandler", ZkBeanIds.ZK_FLOW_URL_HANDLER);
//		String urlhandler = registerInfrastructureComponent(elm, pc, builder,ZkBeanIds.ZK_FLOW_URL_HANDLER);
//		rootBuilder.addPropertyReference("flowUrlHandler", urlhandler);
	}
	
	private void registerAjaxHandler(Element elm, ParserContext pc, BeanDefinitionBuilder rootBuilder) {
//        final BeanDefinitionBuilder builder = 
//        	BeanDefinitionBuilder.rootBeanDefinition(ZkAjaxHandler.class);
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
		.genericBeanDefinition(ZK_FLOW_AJAX_HANDLER_CLASS);
        
		pc.getRegistry().registerBeanDefinition(ZkBeanIds.ZK_AJAX_HANDLER, builder.getBeanDefinition());
      rootBuilder.addPropertyReference("ajaxHandler", ZkBeanIds.ZK_AJAX_HANDLER);

//		String ajaxhandler = registerInfrastructureComponent(elm, pc, builder,ZkBeanIds.ZK_AJAX_HANDLER);
//        rootBuilder.addPropertyReference("ajaxHandler", ajaxhandler);
	}
	
	private void registerFlowExecutor(Element elm, ParserContext pc, BeanDefinitionBuilder rootBuilder) {
        final String flowExecutor = elm.getAttribute(FLOW_EXECUTOR); 
        rootBuilder.addPropertyReference("flowExecutor", StringUtils.hasText(flowExecutor) ? flowExecutor : "flowExecutor");
	}
	
	private String registerInfrastructureComponent(Element element, ParserContext context,
			BeanDefinitionBuilder componentBuilder, String beanName) {
//		String beanName = context.getReaderContext().generateBeanName(componentBuilder.getRawBeanDefinition());
		componentBuilder.getRawBeanDefinition().setSource(context.extractSource(element));
		componentBuilder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_APPLICATION);
		context.registerBeanComponent(new BeanComponentDefinition(componentBuilder.getBeanDefinition(), beanName));
		return beanName;
	}

}
