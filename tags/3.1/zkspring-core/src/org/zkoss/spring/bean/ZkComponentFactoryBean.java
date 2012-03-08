/* ZkComponentBeanFactory.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2008 10:07:14 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.bean;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.NamingException;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.Scope;
import org.zkoss.zk.ui.ext.Scopes;
import org.zkoss.zk.ui.sys.ExecutionCtrl;
import org.zkoss.zkplus.spring.SpringUtil;

/**
 * <p>BeanFactory that looks up a ZK component object. Exposes the components 
 * created by ZK Ajax framework for Spring bean references, e.g. access ZK 
 * components inside Spring beans as a ZK event handling bean.</p>
 *
 * @author henrichen
 * @see ZkComponentProxyTargetSource
 * @since 1.2
 */
public class ZkComponentFactoryBean implements FactoryBean, InitializingBean, BeanNameAware {
	private static final String BIND_EVENTLISTNERS = "zkoss.spring.BIND_EVENTLISTNERS";

	private Object _component;
	private String _path;
	private Class _type;
	private String _beanName;
	private Map _eventListeners;
	private EventListener _afterCompose;
	private Set _controllerIds;
	private boolean _applied;
	
	public ZkComponentFactoryBean() {
	}
	
	public ZkComponentFactoryBean(boolean applied) {
		_applied = applied;
	}
	
	public void setControllerIds(Set ids) {
		_controllerIds = ids;
	}
	
	public Set getControllerIds() {
		return _controllerIds;
	}
	
	public boolean isApplied() {
		return _applied;
	}
	
	/**
	 * Specify the ZK component path to look up. If not specified, will use
	 * bean id to locate the ZK component.
	 * 
	 * @param path the Component path to look up
	 */
	public void setPath(String path) {
		this._path = path;
	}

	/**
	 * Return the ZK component path to look up.
	 */
	public String getPath() {
		return this._path;
	}

	/**
	 * Specify the component class that the found ZK component is supposed
	 * to be assignable to, if any.
	 */
	public void setType(Class expectedType) {
		this._type = expectedType;
	}

	/**
	 * Return the component class that the found ZK component is supposed
	 * to be assignable to, if any.
	 */
	public Class getType() {
		return this._type;
	}

	public Object getObject() throws Exception {
		return _component;
	}
	
	private Object getObject0() {
		final Execution exec = Executions.getCurrent();
		if (exec != null) {
			final String path = getPath();
			if (path != null && path.startsWith("/")) { //absolute path
				return Path.getComponent(path);
			} else {
				final Page page = ((ExecutionCtrl)exec).getCurrentPage();
				final Component self = (Component) page.getAttribute("self");
				if (self != null) { //loading page
					if (Components.isImplicit(getBeanName())) {
						return Components.getImplicit(self, getBeanName());
					}
					if (path == null) {
						//sometimes, Spring can instantiate partially, the bean name might be null
						if (getBeanName() != null) { 
							return self.getAttribute(getBeanName(), false);
						}
					} else 	if (path.startsWith(".")) { //relative path
						return Path.getComponent(self.getSpaceOwner(), path);
					} else {
						return self.getAttribute(path, false);
					}
				} else { //handling event
					final Scope scope = Scopes.getCurrent(page);
					if (scope != null) {
						final Component xself = (Component) scope.getAttribute("self", true);
						if (Components.isImplicit(getBeanName())) {
							return Components.getImplicit(xself, getBeanName());
						}
						if (path == null) {
							//sometimes, Spring can instantiate partially, the bean name might be null
							if (getBeanName() != null) { 
								return scope.getAttribute(getBeanName(), true);
							}
						} else if (path.startsWith(".")) { //relative path
							return Path.getComponent(xself.getSpaceOwner(), path);
						} else {
							return scope.getAttribute(path, true);
						}
					}
				}
			}
		}
		return null;
	}

	public Class getObjectType() {
		if (_component != null) {
			return _component.getClass();
		}

		final Object obj = getObject0();
		
		return obj == null ? getType() : obj.getClass();
	}

	public boolean isSingleton() {
		return false;
	}
	
	public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
		final Class expectedType = getObjectType();
		if (expectedType != null) {
			this._component = getObject0();
			addEventListeners(getEventListeners());
		} else {
			throw new IllegalArgumentException("Must specify a component class(i.e. 'type')");
		}
	}

	public void setBeanName(String name) {
		_beanName = name;
	}
	
	public String getBeanName() {
		return _beanName;
	}
	
	public void setEventListeners(Map eventListeners) {
		_eventListeners = eventListeners;
	}
	
	public Map getEventListeners() {
		return _eventListeners;
	}
	
	public void setAfterCompose(EventListener afterCompose) {
		_afterCompose = afterCompose;
	}
	
	public EventListener getAfterCompose(String controllerId) {
		return _afterCompose;
	}

	private boolean hasBoundListeners(Object applied) {
		if (applied instanceof Page) {
			final Page applied0 = (Page) applied;
			if (applied0.getAttribute(BIND_EVENTLISTNERS) == null) {
				applied0.setAttribute(BIND_EVENTLISTNERS, Boolean.TRUE);
				return false;
			}
		} else if (applied instanceof Component) {
			final Component applied0 = (Component) applied;
			if (applied0.getAttribute(BIND_EVENTLISTNERS) == null) {
				applied0.setAttribute(BIND_EVENTLISTNERS, Boolean.TRUE);
				return false;
			}
		}
		return true;
	}
	//add Page/Component the specified event listeners 
	private void addEventListeners(Map eventListenersMap) {
		if (eventListenersMap != null && !hasBoundListeners(_component)) {
			for (final Iterator it=eventListenersMap.entrySet().iterator();it.hasNext();) {
				final Entry entry = (Entry) it.next();
				final String[] evtname = ((String) entry.getKey()).split("\\.");
				final Set listeners = (Set) entry.getValue();
				//if this bean, avoid endless getBean() method calls 
				final Object target = evtname[0].equals(getBeanName()) ? _component : SpringUtil.getBean(evtname[0]);
				addEventListeners0(target, evtname[1], listeners);
			}
		}
	}
	
	private void addEventListeners0(Object target, String evtname, Set listeners) {
		if (target instanceof Component) {
			final Component target0 = (Component) target;
			for (final Iterator it = listeners.iterator(); it.hasNext();) {
				final EventListener listener = (EventListener) it.next();
				target0.addEventListener(evtname, listener);
			}
		} else if (target instanceof Page) {
			final Page target0 = (Page) target;
			for (final Iterator it = listeners.iterator(); it.hasNext();) {
				final EventListener listener = (EventListener) it.next();
				target0.addEventListener(evtname, listener);
			}
		}
	}
}
