/* DelegatingVariableResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun  1 13:53:53     2006, Created by ashish
}}IS_NOTE

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.lang.Objects;
import org.zkoss.xel.VariableResolver;

/**
 * <p>
 * DelegatingVariableResolver for resolving Spring beans,
 * Spring Security variables and Spring Webflow variables.
 * </p>
 * <p>
 * It delegates variable resolving to ZK Spring core, ZK Spring Security
 * and ZK Spring FlowResolver if they are on application classpath.
 * <p>
 * Usage:<br>
 * <code>&lt;?variable-resolver class="org.zkoss.spring.DelegatingVariableResolver"?&gt;</code>
 * 
 * <p>Developers can specify a list of class names separated with comma in
 * a library property called <code>org.zkoss.spring.VariableResolver.class</code>,
 * such they are used as the default variable resolvers.
 *
 * @author henrichen
 * @since 3.0
 */
public class DelegatingVariableResolver extends org.zkoss.zkplus.spring.DelegatingVariableResolver {
	public static String RESOLVER_CLASS = "org.zkoss.spring.VariableResolver.class";
	/**
	 * Holds list of variable resolvers for Spring core (3.0 and later),
	 * Spring security(3.0 and later)
	 */
	protected List _variableResolvers = new ArrayList();

	public DelegatingVariableResolver() {
		final String classes = Library.getProperty(RESOLVER_CLASS);
		
		String[] vrClss = classes.split(",");
		for (int i = 0; i < vrClss.length; i++) {
			try {
				Object o = Classes.newInstanceByThread(vrClss[i]);
				if(!_variableResolvers.contains(o)) {
					_variableResolvers.add(o);
				}
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Resolves variable name by name. It can resolve a spring bean, spring
	 * security authentication and spring web flow variables depending upon ZK
	 * Spring libraries in the classpath
	 */
	public Object resolveVariable(String name) {
		Object o = null;
		for (final Iterator it = _variableResolvers.iterator(); it.hasNext();) {
			VariableResolver resolver = (VariableResolver) it.next();
			o = resolver.resolveVariable(name);
			if (o != null) {
				return o;
			}
		}
		return o;
	}

	public int hashCode() {
		return Objects.hashCode(_variableResolvers);
	}

	public boolean equals(Object obj) {
		return this == obj || (obj instanceof DelegatingVariableResolver
				&& Objects.equals(_variableResolvers, ((DelegatingVariableResolver) obj)._variableResolvers));
	}
}
