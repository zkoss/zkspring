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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.zkoss.spring.bean.TypePropertyEditor;
import org.zkoss.spring.bean.ZkComponentFactoryBean;
import org.zkoss.spring.security.config.ZkBeanIds;
import org.zkoss.spring.web.context.request.ApplicationScope;
import org.zkoss.spring.web.context.request.DesktopScope;
import org.zkoss.spring.web.context.request.ExecutionScope;
import org.zkoss.spring.web.context.request.IdSpaceScope;
import org.zkoss.spring.web.context.request.PageScope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;

/**
 * Register zk specific scopes, Spring bean binding composer, 
 * implicit objects definition, and 'type' to class PropertyEditor.
 * @author henrichen
 * @since 1.2
 */
public class ZkConfigDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext pc) {
		final BeanDefinitionRegistry reg = pc.getRegistry();
		
		//register ZK scopes
        final BeanDefinitionBuilder builder = 
        	BeanDefinitionBuilder.rootBeanDefinition(CustomScopeConfigurer.class);
		final Map scopes = new HashMap();
		scopes.put("application", new ApplicationScope());
		scopes.put("desktop", new DesktopScope());
		scopes.put("page", new PageScope());
		scopes.put("idspace", new IdSpaceScope());
		scopes.put("execution", new ExecutionScope());
		builder.addPropertyValue("scopes", scopes);
        reg.registerBeanDefinition(ZkBeanIds.ZK_SCOPE_CONFIG, builder.getBeanDefinition());
        
        //register ZK implicit object factory bean
        registerImplicitObjects(reg);
		
        //register singleton ZkSpringBeanBindingComposer (used to bind ZK component as Spring bean), 
        //see ZkDesktopReuseUiFactory#applyZkSpringBeanBindingComposer(PageDefinition pd)
        final BeanDefinitionBuilder bbuilder = 
        	BeanDefinitionBuilder.rootBeanDefinition(ZkSpringBeanBindingComposer.class);
        reg.registerBeanDefinition(ZkBeanIds.ZK_BINDING_COMPOSER, bbuilder.getBeanDefinition());
        
        //register PropertyEditor for ZkComponentProxyFactoryBean#type
        final BeanDefinitionBuilder xbuilder = 
        	BeanDefinitionBuilder.rootBeanDefinition(CustomEditorConfigurer.class);
        final Map editors = new HashMap();
        editors.put("org.zkoss.spring.bean.TypePropertyEditor", "org.zkoss.spring.bean.TypePropertyEditor");
        xbuilder.addPropertyValue("customEditors", editors);
        reg.registerBeanDefinition(ZkBeanIds.ZK_TYPE_PROPERTY_EDITOR, xbuilder.getBeanDefinition());

        return null;
	}
	
	private void registerImplicitObject(BeanDefinitionRegistry reg, String scope, String id, String type) {
		final BeanDefinitionBuilder builder = 
			BeanDefinitionBuilder.rootBeanDefinition(ZkComponentFactoryBean.class);
        builder.setScope(scope); //application scope
        builder.addPropertyValue("type", type);
        reg.registerBeanDefinition(id, builder.getBeanDefinition());
	}
	
    //register ZK implicit object factory beans
	private void registerImplicitObjects(BeanDefinitionRegistry reg) {
        //application scope
		registerImplicitObject(reg, "application", "application", WebApp.class.getName());
		registerImplicitObject(reg, "application", "applicationScope", Map.class.getName());

        //session scope
		registerImplicitObject(reg, "session", "session", Session.class.getName());
		registerImplicitObject(reg, "session", "sessionScope", Map.class.getName());
        
        //desktop scope
		registerImplicitObject(reg, "desktop", "desktop", Desktop.class.getName());
		registerImplicitObject(reg, "desktop", "desktopScope", Map.class.getName());
        
        //page scope
		registerImplicitObject(reg, "page", "page", Page.class.getName());
		registerImplicitObject(reg, "page", "pageScope", Map.class.getName());
        
        //idspace scope
		registerImplicitObject(reg, "idspace", "spaceOwner", IdSpace.class.getName());
		registerImplicitObject(reg, "idspace", "spaceScope", Map.class.getName());
		registerImplicitObject(reg, "idspace", "componentScope", Map.class.getName());
        
        //execution scope
		registerImplicitObject(reg, "execution", "self", Component.class.getName());
		registerImplicitObject(reg, "execution", "execution", Execution.class.getName());
		registerImplicitObject(reg, "execution", "requestScope", Map.class.getName());
		registerImplicitObject(reg, "execution", "arg", Map.class.getName());
		registerImplicitObject(reg, "execution", "param", Map.class.getName());
		registerImplicitObject(reg, "execution", "event", Event.class.getName());
	}
}
