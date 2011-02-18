/* ZkCDIIntegrationContext.java
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 31, 2010 10:30:14 AM, Created by ashish
}}IS_NOTE

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

 */
package org.zkoss.spring.util;

import org.zkoss.xel.VariableResolver;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.xel.impl.ExecutionResolver;

/**
 * <p>
 * This class provides ThreadLocal storage of context components. Nomally it is
 * used to set context components prior to injecting child ZK compoents in
 * subclasses extending {@link GenericComposer}
 * </p
 * <p>
 * This class also provides utility methods to set/get self component used while
 * publishing events using CDI event notification model
 * </p>
 * 
 * @author ashish
 * @see GenericComposer
 */
public class ZkSpringIntegrationContext {

	/**
	 * Stores context components for later use during injection of child zk
	 * components
	 */
	private static ThreadLocal<Component> components = new ThreadLocal<Component>();

	/**
	 * returns current thread's context component
	 * 
	 * @return Component
	 */
	public static Component getContextComponent() {
		return components.get();
	}

	/**
	 * Sets context component which is later required for ZK component injection
	 * 
	 * @param component
	 *            parent component
	 */
	public static void setContextComponent(Component component) {
		components.set(component);
	}

	/**
	 * Clears context component from ZK CDI integration context that was set
	 * prior to ZK component injection
	 */
	public static void clearContextComponent() {
		components.remove();
	}

	/**
	 * Sets self context component which is later required for ZK event
	 * processing using CDI event notificatio model
	 * 
	 * @param component
	 *            target component
	 */
	public static void setSelfContextComponent(Component component) {
		final Execution exec = Executions.getCurrent();
		final VariableResolver vresolver = exec.getVariableResolver();
		((ExecutionResolver) vresolver).setSelf(component);
	}

	/**
	 * Returns current self context component
	 * 
	 * @returns component
	 */
	public static Component getSelfContextComponent() {
		final Execution exec = Executions.getCurrent();
		final VariableResolver vresolver = exec.getVariableResolver();
		return (Component) ((ExecutionResolver) vresolver).getSelf();
	}
}
