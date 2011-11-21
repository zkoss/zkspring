/* ZkSpringBeanBindingComposer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 24, 2008 2:35:50 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.config;

import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.ExecutionCtrl;
import org.zkoss.zk.ui.util.Composer;

/**
 * Composer to bind ZK bean as Spring bean. 
 * @author henrichen
 * @since 1.2
 */
public class ZkSpringBeanBindingComposer implements Composer {
	private static final String BIND_CONTROLLER =  "zkoss.spring.BIND_CONTROLLER";

	public void doAfterCompose(Component comp) throws Exception {
		bindComponent(comp);
	}
	
	private void bindComponent(Component comp) {
		if (comp.getId() != comp.getUuid()) { //with user specified id
			final Execution exec = Executions.getCurrent();
			if (exec != null) {
				final ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) SpringUtil.getApplicationContext();
				final ConfigurableBeanFactory factory = ctx.getBeanFactory();
				final String fid = "&"+comp.getId();
				if (factory.containsBean(fid)) {
					final Page page = ((ExecutionCtrl)exec).getCurrentPage();
					final Object self = page.hasAttribute("self") ? 
							page.getAttribute("self", false) : null; 
					try {
						page.setAttribute("self", comp);
						//trigger component binding
						final Object proxy = SpringUtil.getBean(comp.getId(), Component.class);
						if (proxy != null) {
							final BeanDefinition bd = factory.getMergedBeanDefinition(fid);
							final MutablePropertyValues mpv = bd.getPropertyValues();
							final PropertyValue pv = mpv.getPropertyValue("controllerIds");
							final Set controllerIds = (pv != null) ? (Set) pv.getValue() : null;
							if (controllerIds != null) {
								bindControllers(comp, controllerIds, fid);
							}
						}
					} finally {
						if (self == null) {
							page.removeAttribute("self");
						} else {
							page.setAttribute("self", self);
						}
					}
				}
			}
		}
	}
	
	private void bindControllers(Component target, Set ids, String fid) {
		for(final Iterator it = ids.iterator(); it.hasNext();) {
			bindController(target, (String) it.next(), fid);
		}
	}
	
	private void bindController(Component target, String controllerId, String fid) {
		if (controllerId != null && target.getAttribute(BIND_CONTROLLER+"_"+controllerId) == null) {
			target.setAttribute(BIND_CONTROLLER+"_"+controllerId, Boolean.TRUE);
			final Object controller = SpringUtil.getBean(controllerId);
			if (controller != null) {
				//bind event listener and add forwards for special name pattern method
				Events.addEventListeners(target, controller);
				Components.addForwards(target, controller);
				
				//calling method annotated with @AfterCompose == @EventHandler("win.onAfterCompose")
				Events.sendEvent(new Event("onAfterCompose", target, controllerId));
			}
		}
	}
}
