/* ZkComponenetProxyFactoryBeansDefinitionParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 25, 2008 12:18:24 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.zkoss.spring.bean.ZkComponentFactoryBean;
import org.zkoss.util.CollectionsX;

/**
 * Allows for mass convenient creation of {@link ZkComponentFactoryBean} 
 * beans for bijection of ZK component and Spring bean.
 *
 * @author henrichen
 * @since 1.2
 */
public class ZkComponentFactoryBeansDefinitionParser implements BeanDefinitionParser {
	public static final String ATT_IDS = "ids";
	public static final String ATT_SCOPE = "scope";

	public BeanDefinition parse(Element element, ParserContext pc) {
		String scope =  element.getAttribute(ATT_SCOPE);
		if (!StringUtils.hasText(scope)) {
			scope = "idspace";
		}
		
		final BeanDefinitionRegistry reg = pc.getRegistry();
        final String idsStr = element.getAttribute(ATT_IDS);
    	if (StringUtils.hasText(idsStr)) {
            final BeanDefinitionBuilder builder = 
            	BeanDefinitionBuilder.rootBeanDefinition(ZkComponentFactoryBean.class);
            builder.setScope(scope);
            final BeanDefinition bd = builder.getBeanDefinition();
    		final Collection ids = CollectionsX.parse(new LinkedHashSet(), idsStr, ',');
    		for (final Iterator it = ids.iterator(); it.hasNext();) {
    			final String id = (String) it.next();
    			reg.registerBeanDefinition(id, bd);
    		}
		}
		return null;
	}
}
