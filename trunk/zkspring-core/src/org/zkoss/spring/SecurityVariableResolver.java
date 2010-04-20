/* SecurityVariableResolver.java

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
package org.zkoss.spring;

/**
 * Used to interface with ZK Spring Security library for resolving security related variables by {@link DelegatingVariableResolver}.
 * @author ashish
 * 
 */
public interface SecurityVariableResolver {

	/**
	* Returns currently authenticated {@link Principal} object. If no user is logged in returns null.
	*/
	public Object getAuthentication();
}
