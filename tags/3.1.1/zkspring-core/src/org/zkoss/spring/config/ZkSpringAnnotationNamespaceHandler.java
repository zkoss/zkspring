/* ZkSpringNamespaceHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 29, 2008 2:25:40 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registers the bean definition parsers for the ZK + Spring with Annotation namespace.
 * (http://www.zkoss.org/2008/zkspring-annot"). 
 * @author henrichen
 * @since 1.2
 */
public class ZkSpringAnnotationNamespaceHandler extends NamespaceHandlerSupport {

	@SuppressWarnings("deprecation")
	public void init() {
		//--Spring bean--//
		//<component-scan />
		registerBeanDefinitionParser("component-scan", new org.zkoss.spring.context.annotation.ZkComponentScanBeanDefinitionParser());
	}
}
