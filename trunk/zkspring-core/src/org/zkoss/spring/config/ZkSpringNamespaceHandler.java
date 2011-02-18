/* ZkSpringNamespaceHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 2, 2008 2:40:59 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.zkoss.spring.context.annotation.ZkComponentScanBeanDefinitionParser;

/**
 * Registers the bean definition parsers for the ZK + Spring namespace.
 * (http://www.zkoss.org/2008/zkspring"). 
 * @author henrichen
 * @since 1.0
 */
public class ZkSpringNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		//--Spring bean--//
		//<ui-lookup id="..." type="..." [path="..."] [scope="..."]/>
		registerBeanDefinitionParser("ui-lookup", new ZkComponentFactoryBeanDefinitionParser());
		//<ui-lookup-all [ids="id1,id2,..."] [scope="..."]/>
		registerBeanDefinitionParser("ui-lookup-all", new ZkComponentFactoryBeansDefinitionParser());
		//<zk-config/>
		registerBeanDefinitionParser("zk-config", new ZkConfigDefinitionParser());
		// Following feature is commented as we introduce GenericSpringComposer as an equivalent feature
		//<component-scan />
//		registerBeanDefinitionParser("component-scan", new ZkComponentScanBeanDefinitionParser());
	}
}
