/* ZkExceptionTranslationFilter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 19, 2008 5:03:45 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.ui;

//import org.springframework.security.ui.ExceptionTranslationFilter;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.zkoss.spring.security.ui.webapp.ZkAuthenticationEntryPoint;

/**
 * Default ZkExceptionTranslationFilter used with ZK event exception handling.
 * @author henrichen
 * @since 1.0
 * @see ZkAuthenticationEntryPoint
 * @see ZkExceptionTranslationListener
 */
public class ZkExceptionTranslationFilter extends ExceptionTranslationFilter {
	public ZkExceptionTranslationFilter() {
		super();
		setAuthenticationEntryPoint(new ZkAuthenticationEntryPoint());
		setAccessDeniedHandler(new ZkAccessDeniedHandler());
		//This is for secure ZK Ajax request. 
		//Have to set to false to disallow redirect to saved Ajax request.
//		setCreateSessionAllowed(false); 
	}
}
