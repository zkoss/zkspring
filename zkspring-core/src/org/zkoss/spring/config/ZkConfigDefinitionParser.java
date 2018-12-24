/* ZkScopeConfigDefinitionParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2008 6:25:33 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.zkoss.spring.security.config.ZkBeanIds;

/**
 * Register zk specific scopes
 *
 * @author henrichen
 * @since 1.2
 */
public class ZkConfigDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext pc) {
		final BeanDefinitionRegistry reg = pc.getRegistry();
		//register ZK scopes
		reg.registerBeanDefinition(ZkBeanIds.ZK_SCOPE_CONFIG, new RootBeanDefinition(ZkScopesConfigurer.class));
		return null;
	}
}
