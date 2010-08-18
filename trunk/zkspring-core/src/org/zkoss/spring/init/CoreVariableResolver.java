/* CoreVariableResolver.java

	Purpose:
		
	Description:
		
	History:
		Aug 18, 2010 3:01:19 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.spring.init;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.zkoss.spring.SpringUtil;
import org.zkoss.xel.VariableResolver;
import org.zkoss.zk.ui.Components;

/**
 * VariableResolver for the ZK Spring Core module.
 * @author henrichen
 * @since 3.0
 */
public class CoreVariableResolver implements VariableResolver {
	protected ApplicationContext _ctx;
	
	/**
	 * Get the spring application context.
	 */
	protected ApplicationContext getApplicationContext() {
		if (_ctx != null)
			return _ctx;
			
		_ctx = SpringUtil.getApplicationContext();
		return _ctx;
	}
	
	/**
	 * Get the spring bean by the specified name.
	 */		
	public Object resolveVariable(String name) {
	
		if ("springContext".equals(name)) {
			return getApplicationContext();
		}
		
		//might recursive ZK implicit object here, always return null
		if (Components.isImplicit(name)
			//#bug 2681819: normal page throws exception after installed zkspring
			//work around for 3.6.0 and before
		|| "event".equals(name)) { 
			return null;
		}
		
		try {
			if (getApplicationContext().containsBean(name)) {
				return getApplicationContext().getBean(name);
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// ignore
		}
		return null;
	}
	
	public int hashCode() {
		return getClass().hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
}
