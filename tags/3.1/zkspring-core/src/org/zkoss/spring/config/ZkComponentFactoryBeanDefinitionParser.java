/* ZkComponentProxyFactoryBeanDefinitionParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2008 5:00:39 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.zkoss.spring.bean.ZkComponentFactoryBean;

/**
 * Allows for convenient creation of a {@link ZkComponentFactoryBean} 
 * bean for bijection of ZK component and Spring bean.
 *
 * @author henrichen
 * @since 1.2
 */
public class ZkComponentFactoryBeanDefinitionParser 
extends	AbstractSingleBeanDefinitionParser {
    //<ui-lookup>
    public static final String ATT_PATH = "path";
    public static final String ATT_TYPE = "type";
    public static final String ATT_SCOPE = "scope";
//    public static final String ATT_PROXY_INTERFACE = "proxy-interface";
    public static final String ATT_CONTROLLER = "controller";
	
    protected Class getBeanClass(Element element) {
   		return ZkComponentFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext pc, BeanDefinitionBuilder builder) {
        final String path = element.getAttribute(ATT_PATH);
    	if (StringUtils.hasText(path)) {
    		builder.addPropertyValue("path", path);
    	}
        final String type = element.getAttribute(ATT_TYPE);
    	if (StringUtils.hasText(type)) {
    		builder.addPropertyValue("type", type);
    	}
    	
/*        final String proxyInterface = element.getAttribute(ATT_PROXY_INTERFACE);
    	if (StringUtils.hasText(proxyInterface)) {
    		builder.addPropertyValue("proxyInterface", proxyInterface);
    	}
*/
        final String controller = element.getAttribute(ATT_CONTROLLER);
    	if (StringUtils.hasText(controller)) {
    		builder.addConstructorArgValue(controller);
    	}
    	
    	final String scope = element.getAttribute(ATT_SCOPE);
    	if (StringUtils.hasText(scope)) {
    		builder.setScope(scope);
    	} else {
    		builder.setScope("idspace"); //default to idspace scope
    	}
	}
}
