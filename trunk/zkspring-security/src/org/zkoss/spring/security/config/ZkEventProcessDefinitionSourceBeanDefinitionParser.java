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
package org.zkoss.spring.security.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.http.ZkEventSecurityBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.zkoss.spring.security.intercept.zkevent.EventProcessKey;
import org.zkoss.spring.security.intercept.zkevent.ZkEventProcessDefinitionSource;
import org.zkoss.spring.security.intercept.zkevent.ZkEventProcessDefinitionSourceImpl;
import org.zkoss.spring.security.util.UrlMatcher;

/**
 * Allows for convenient creation of a {@link ZkEventProcessDefinitionSource} 
 * bean for use with a ZkEventProcessInterceptor.
 * @author henrichen
 * @since 1.0
 */
public class ZkEventProcessDefinitionSourceBeanDefinitionParser 
extends	AbstractSingleBeanDefinitionParser {

    protected Class<ZkEventProcessDefinitionSourceImpl> getBeanClass(Element element) {
        return ZkEventProcessDefinitionSourceImpl.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List interceptUrls = DomUtils.getChildElementsByTagName(element, ZkElements.INTERCEPT_EVENT);
        
        UrlMatcher matcher = ZkEventSecurityBeanDefinitionParser.createUrlMatcher(element);
        
        LinkedHashMap<EventProcessKey, Collection<ConfigAttribute>> requestMap = 
        	parseInterceptEventsForZkEventProcessMap(interceptUrls, parserContext);
        builder.addConstructorArgValue(matcher);
        builder.addConstructorArgValue(requestMap);
    }
    
    @SuppressWarnings({ "unchecked", "deprecation" })
	public static LinkedHashMap<EventProcessKey, Collection<ConfigAttribute>> parseInterceptEventsForZkEventProcessMap(List elms,  ParserContext parserContext) {
        LinkedHashMap<EventProcessKey, Collection<ConfigAttribute>> eventProcessDefinitionMap = new LinkedHashMap<EventProcessKey, Collection<ConfigAttribute>>();
        
        org.springframework.security.access.ConfigAttributeEditor editor = new org.springframework.security.access.ConfigAttributeEditor();
        for (final Iterator it = elms.iterator(); it.hasNext();) {
            Element elm = (Element) it.next();

            String path = elm.getAttribute(ZkEventSecurityBeanDefinitionParser.ATT_PATH);

            if(!StringUtils.hasText(path)) {
                parserContext.getReaderContext().error("path attribute cannot be empty or null", elm);
            }

            String event = elm.getAttribute(ZkEventSecurityBeanDefinitionParser.ATT_ZK_EVENT);
            if (!StringUtils.hasText(event)) {
                event = null;
            }

            String access = elm.getAttribute(ZkEventSecurityBeanDefinitionParser.ATT_ACCESS_CONFIG);

            // Convert the comma-separated list of access attributes to a ConfigAttributeDefinition
            if (StringUtils.hasText(access)) {
                editor.setAsText(access);
                eventProcessDefinitionMap.put(new EventProcessKey(path, event), (Collection<ConfigAttribute>) editor.getValue());
            }
        }
        
        return eventProcessDefinitionMap;
    }

}
