/* ZkSpringWebflowNamespaceHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Feb 7, 2011 10:06:59 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.webflow.config.ZkFlowBuilderServicesBeanDefinitionParser;
import org.springframework.webflow.config.ZkFlowControllerBeanDefinitionParser;

/**
 * Registers the bean definition parsers for the ZK + Spring Webflow namespace.
 * (http://www.zkoss.org/2008/zkspring-webflow"). 
 * @author ashish
 * @since 3.0
 */
public class ZkSpringWebflowNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		//--Spring Web Flow--//
		//<flow-builder-services/>
		registerBeanDefinitionParser("flow-builder-services", new ZkFlowBuilderServicesBeanDefinitionParser());
		registerBeanDefinitionParser("flow-controller", new ZkFlowControllerBeanDefinitionParser());
	}
}
