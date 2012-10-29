/* ZkClassPathBeanDefinitionScanner.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 26, 2008 6:25:34 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.context.annotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.zkoss.spring.bean.ZkComponentFactoryBean;
import org.zkoss.util.CollectionsX;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;

//this class was disabled since revision 20 because of the GenericSpringComposer introduced.
//since we have SelectorComposer and GenericForwardComposer, so we also deprecated AppliedTo
/**
 * @deprecated after release of zkspring 3.1, suggest to use SelectorComposer or GenericForwardComposer
 * 
 * An enhanced {@link ClassPathBeanDefinitionScanner} which not only detects bean 
 * candidates on the classpath but also those ZK component fields (with 
 * {@link @Resource} annotation) defined in such candidate beans annotated as 
 * {@link @Controller} and with a {@link @AppliedTo} associated ZK component.
 *  
 * annotation.
 * 
 * @author henrichen
 */
public class ZkClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
	public ZkClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		super(registry, useDefaultFilters);
	}
	public ZkClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		super(registry);
	}

	/**
	 * Apply further settings to the given bean definition,
	 * beyond the contents retrieved from scanning the component class.
	 * @param beanDefinition the scanned bean definition
	 * @param beanName the generated bean name for the given bean
	 * @throws ClassNotFoundException 
	 */
	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
		super.postProcessBeanDefinition(beanDefinition, beanName);
		if (beanDefinition instanceof AnnotatedBeanDefinition) {
			final AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) beanDefinition;
			final AnnotationMetadata amd = abd.getMetadata();
			if (amd.hasAnnotation("org.springframework.stereotype.Controller")) {
				//register @AppliedTo
				BeanDefinition forbd = null; 
				String forBean = null;
				if (amd.hasAnnotation("org.zkoss.spring.context.annotation.AppliedTo")) {
					final Map<String, Object> attrs = amd.getAnnotationAttributes("org.zkoss.spring.context.annotation.AppliedTo");
					if (attrs.containsKey("value")) {
						forBean = (String) attrs.get("value");
						forbd = registerZkComponentFactoryBean(forBean, beanName, true, "idspace");
					} else {
						throw new UiException("ZK component id required in @AppliedTo annoation");
					}
				}
				//scan all fields/method of this bean
				try {
					final String classnm = beanDefinition.getBeanClassName();
					final Class klass = ClassUtils.forName(classnm, Thread.currentThread().getContextClassLoader());
					//for the klass scan fields/methods
					registerZkComponentFactoryBean0(forbd, forBean, klass, beanName);
					
				} catch(ClassNotFoundException ex) {
					//shall never come here, the class is scanned inside .class files
					throw UiException.Aide.wrap(ex);
				}
			}
		}
	}
	
	private void registerZkComponentFactoryBean0(final BeanDefinition forbd, final String forbdId, final Class klass, final String controllername) {
		ReflectionUtils.doWithFields(klass, new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) {
				if (field.isAnnotationPresent(Resource.class)) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@Resource annotation is not supported on static fields");
					}
					if (Component.class.isAssignableFrom(field.getType()) && !Components.isImplicit(field.getName())) {
						registerZkComponentFactoryBean(field.getName(), controllername, false, "idspace");
					}
				}
			}
		});
		ReflectionUtils.doWithMethods(klass, new ReflectionUtils.MethodCallback() {
			@SuppressWarnings("deprecation")
			public void doWith(Method method) {
				if (method.isAnnotationPresent(Resource.class) &&
						method.equals(ClassUtils.getMostSpecificMethod(method, klass))) {
					if (Modifier.isStatic(method.getModifiers())) {
						throw new IllegalStateException("@Resource annotation is not supported on static methods");
					}
					final Class[] paramTypes = method.getParameterTypes();
					if (paramTypes.length != 1) {
						throw new IllegalStateException("@Resource annotation requires a single-arg method: " + method);
					}
					if (Component.class.isAssignableFrom(paramTypes[0])) {
						PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
						if (!Components.isImplicit(pd.getName())) {
							registerZkComponentFactoryBean(pd.getName(), controllername, false, "idspace");
						}
					}
				} 
				if (forbd != null && method.isAnnotationPresent(EventHandler.class) &&
						method.equals(ClassUtils.getMostSpecificMethod(method, klass))) {
					if (Modifier.isStatic(method.getModifiers())) {
						throw new IllegalStateException("@EventHandler annotation is not supported on static methods: " + method);
					}
					final Class[] paramTypes = method.getParameterTypes();
					if (paramTypes.length > 1) {
						throw new IllegalStateException("@EventHandler annotation requires no argument or a single org.zkoss.zk.ui.event.Event argument method: " + method);
					} else if (paramTypes.length == 1 && !Event.class.isAssignableFrom(paramTypes[0])) {
						throw new IllegalStateException("@EventHandler annotation requires no argument or a single org.zkoss.zk.ui.event.Event argument method: " + method);
					}
					final String methodname = method.getName();
					final String listenerid = controllername+"."+methodname;
					registerZkEventListener(listenerid, controllername, methodname, "idspace");

					final EventHandler on = method.getAnnotation(EventHandler.class);
					final String onvalue = on.value();
					registerOnZkComponentFactoryBean(forbd, listenerid, controllername, onvalue, methodname, "idspace");
				} 
				if (forbd != null && method.isAnnotationPresent(AfterCompose.class) &&
						method.equals(ClassUtils.getMostSpecificMethod(method, klass))) {
					if (Modifier.isStatic(method.getModifiers())) {
						throw new IllegalStateException("@AfterCompose annotation is not supported on static methods: "+method);
					}
					final Class[] paramTypes = method.getParameterTypes();
					if (paramTypes.length > 1) {
						throw new IllegalStateException("@AfterCompose annotation requires no argument or a single org.zkoss.zk.ui.event.Event argument method: " + method);
					} else if (paramTypes.length == 1 && !Event.class.isAssignableFrom(paramTypes[0])) {
						throw new IllegalStateException("@AfterCompose annotation requires no argument or a single org.zkoss.zk.ui.event.Event argument method: " + method);
					}
					final String methodname = method.getName();
					final String listenerid = controllername+"."+methodname;
					registerZkEventListener(listenerid, controllername, methodname, "idspace");
					
					final String onvalue = forbdId+".onAfterCompose";
					registerOnZkComponentFactoryBean(forbd, listenerid, controllername, onvalue, methodname, "idspace");
				}
			}
		});
	}
	
	private BeanDefinition registerZkComponentFactoryBean(String id, String controller, boolean applied, String scope) {
		BeanDefinition bd = null;
		if (getRegistry().containsBeanDefinition(id)) {
            bd = getRegistry().getBeanDefinition(id);
		} else {
	        final BeanDefinitionBuilder builder = 
	        	BeanDefinitionBuilder.rootBeanDefinition(ZkComponentFactoryBean.class);
	        builder.setScope(scope);
        	builder.addConstructorArgValue(applied);
	        bd = builder.getBeanDefinition();
			getRegistry().registerBeanDefinition(id, bd);
		}
        if (applied) {
        	addApplyController(bd, controller);
        }
        return bd;
	}
	
	//handle registration of @EventHandler("abc.onXyz") ZkMethodEventListener
	private void registerZkEventListener(String beanid, String controllername, String methodname, String scope) {
        final BeanDefinitionBuilder builder = 
        	BeanDefinitionBuilder.rootBeanDefinition(org.zkoss.spring.bean.ZkMethodEventListener.class);
        builder.setScope(scope);
       	builder.addConstructorArgValue(controllername);
       	builder.addConstructorArgValue(methodname);
        final BeanDefinition bd = builder.getBeanDefinition();
		getRegistry().registerBeanDefinition(beanid, bd);
	}
	
	//handle registration of @EventHandler("abc.onXyz") ZkComponentFactoryBean + setEventListeners
	private void registerOnZkComponentFactoryBean(BeanDefinition forbd, String listenerid, String controllerid, String onvalue, String methodname, String scope) {
		final Collection comps = CollectionsX.parse(new ArrayList(), onvalue, ',');
		for (final Iterator it = comps.iterator(); it.hasNext();) {
			final String event = (String) it.next();
			final int j = event.lastIndexOf('.');
			if (j < 0) {
				//no event name, convert from the method name
				final String compid = event;
				if (methodname.length() > 3 && methodname.startsWith("do") && Character.isUpperCase(methodname.charAt(2))) {
					registerZkComponentFactoryBean(compid, controllerid, false, scope);
					String eventname = event + ".on"+methodname.substring(2);
					addZkEventListener(forbd, eventname, listenerid);
				} else {
					throw new UiException("@EventHandler annotation expects a method with doXxx name if event is not specified: " + event);
				}
			} else {
				final String compid = event.substring(0, j);
				final String eventname = event.substring(j+1);
				if (eventname.startsWith("on")) {
					registerZkComponentFactoryBean(compid, controllerid, false, scope);
					addZkEventListener(forbd, event, listenerid);
				} else {
					throw new UiException("@EventHandler annotation expects ZK component id and event in the form of \"abc.onXyz\": "+event);
				} 
			}
		}
	}
	
	private void addZkEventListener(BeanDefinition bd, String eventname, String listenerid) {
		final MutablePropertyValues pvs = bd.getPropertyValues();
		PropertyValue pv = pvs.getPropertyValue("eventListeners");
		if (pv == null) {
			pvs.addPropertyValue("eventListeners", new ManagedMap());
			pv = pvs.getPropertyValue("eventListeners");
		}
		final Map eventListenersMap = (Map) pv.getValue(); //(comp.eventname, Set(listeners))
		Set listenersSet = (Set) eventListenersMap.get(new TypedStringValue(eventname));
		if (listenersSet == null) {
			listenersSet = new ManagedSet();
			eventListenersMap.put(new TypedStringValue(eventname), listenersSet);
		}
		listenersSet.add(new RuntimeBeanReference(listenerid));
	}
	
	private void addApplyController(BeanDefinition bd, String controllerId) {
		final MutablePropertyValues pvs = bd.getPropertyValues();
		PropertyValue pv = pvs.getPropertyValue("controllerIds");
		if (pv == null) {
			pvs.addPropertyValue("controllerIds", new ManagedSet());
			pv = pvs.getPropertyValue("controllerIds");
		}
		final Set ids = (Set) pv.getValue(); //(controller)
		ids.add(controllerId);
	}
}
