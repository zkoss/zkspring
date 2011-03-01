/* SecurityVariableResolver.java

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
package org.zkoss.spring.init;

import org.zkoss.spring.security.SecurityUtil;
import org.zkoss.xel.VariableResolver;

/**
 * DelegatingVariableResolver, a spring security variable resolver.
 *
 * <p>It resolves a Spring Security variable <code>authentication</code> to represent
 * the implementation instance of <code>org.springframework.security.core.Authentication</code>.
 *
 * <p>Usage:<br>
 * <code>&lt;?variable-resolver class="org.zkoss.spring.security.DelegatingVariableResolver"?&gt;</code>
 *
 * @author ashish
 * @since 3.0
 */
public class SecurityVariableResolver implements VariableResolver {
	
	
	/**
	 * Get the spring bean by the specified name.
	 */		
	public Object resolveVariable(String name) {
	
		if ("authentication".equals(name)) {
			return SecurityUtil.getAuthentication();
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
