/* AbstractSecurityVariableResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Mar  9 13:53:53     2010, Created by ashish
}}IS_NOTE

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.spring.security;

import org.zkoss.spring.SecurityVariableResolver;

/**
 * Default implementation of {@link SecurityVariableResolver}. Supports methods that are invoked by {@link DelegatingVariableResolver}
 * for resolving security variables in zscript or EL expressions.
 * @author ashish
 *
 */
public class AbstractSecurityVariableResolver implements
		SecurityVariableResolver {

	public Object getAuthentication() {
		// TODO Auto-generated method stub
		return SecurityUtil.getAuthentication();
	}

}
