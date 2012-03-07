/* ZkEnableSessionInvalidateFilter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 19, 2008 9:14:45 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.ui;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.filter.GenericFilterBean;
import org.zkoss.zk.ui.impl.Attributes;

/**
 * This filter is used to remove the do-not-clean-ZK-session 
 * (RENEW_NATIVE_SESSION) notification made by 
 * {@link ZkDisableSessionInvalidateFilter}.
 *
 * @author henrichen
 * @see ZkDisableSessionInvalidateFilter
 * @since 1.0
 *
 */
public class ZkEnableSessionInvalidateFilter extends GenericFilterBean {

	protected void doFilterHttp(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
        final HttpSession sess = ((HttpServletRequest) request).getSession(false);
        if(sess == null) {
            chain.doFilter(request, response);
            return;
        }
        
        //remove the notification made by ZkDisableSessionInvalidateFilter.
        //See ZK feature 2117539
        sess.removeAttribute(Attributes.RENEW_NATIVE_SESSION);
        
        chain.doFilter(request, response);
	}
}
