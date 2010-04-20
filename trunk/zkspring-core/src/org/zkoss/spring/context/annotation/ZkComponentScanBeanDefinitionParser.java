/* ZkComponentScanBeanDefinitionParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 26, 2008 11:45:32 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.context.annotation;

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Enhanced version to handle annotation based ZK component Spring bean definition.
 * @author henrichen
 */
public class ZkComponentScanBeanDefinitionParser extends ComponentScanBeanDefinitionParser {
	protected ClassPathBeanDefinitionScanner createScanner(XmlReaderContext readerContext, boolean useDefaultFilters) {
		return new ZkClassPathBeanDefinitionScanner(readerContext.getRegistry(), useDefaultFilters);
	}
}
