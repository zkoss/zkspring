/* DelegatingVariableResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun  1 13:53:53     2006, Created by henrichen
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.spring;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.xel.VariableResolver;
import org.zkoss.zk.ui.Components;

/**
 * DelegatingVariableResolver, a spring bean variable resolver.
 *
 * <p>It defines a variable called <code>springContext</code> to represent
 * the instance of <code>org.springframework.context.ApplicationContext</code>.
 * It also looks variables for beans defined in <code>springContext</code>.
 *
 * <p>Usage:<br>
 * <code>&lt;?variable-resolver class="org.zkoss.spring.DelegatingVariableResolver"?&gt;</code>
 *
 * @author henrichen
 * @since 1.0
 */
public class DelegatingVariableResolver implements VariableResolver {
	protected ApplicationContext _ctx;
	protected SecurityVariableResolver _secResolver;
	
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
		
		if ("authentication".equals(name)) {
			if(_secResolver == null) {
				final String cls = Library.getProperty("org.zkoss.spring.SecurityVariableResolver");
				if (cls != null && cls.length() > 0) {
					try {
						_secResolver = (SecurityVariableResolver)Classes.newInstanceByThread(cls);
					} catch (Throwable ex) {
						return null;
					}
				} else {
					return null;
				}
			}
			return _secResolver.getAuthentication();
		}
		
		//might recursive ZK implicit object here, always return null
		if (Components.isImplicit(name)
			//#bug 2681819: normal page throws exception after installed zkspring
			//work around for 3.6.0 and before
		|| "event".equals(name)) { 
			return null;
		}
		
		try {
			return getApplicationContext().getBean(name);
		} catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}
}
