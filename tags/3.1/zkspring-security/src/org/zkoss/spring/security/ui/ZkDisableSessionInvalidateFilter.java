/* ZkDisableSessionInvalidateFilter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 19, 2008 8:50:27 AM, Created by henrichen
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
 * This filter is used to notify ZK's {@link org.zkoss.zk.ui.http.HttpSessionListener}
 * not to clean ZK's session when Spring filters such as 
 * AuthenticationProcessingFilter and SessionFixationProtectionFilter 
 * invalidate the native session. 
 *
 * @author henrichen
 * @see ZkEnableSessionInvalidateFilter
 * @since 1.0
 *
 */
public class ZkDisableSessionInvalidateFilter extends GenericFilterBean {

	protected void doFilterHttp(HttpServletRequest request,
		HttpServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		
	}

//	public int getOrder() {
//		return FilterChainOrder.AUTHENTICATION_PROCESSING_FILTER - 20;
//	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
        final HttpSession sess = ((HttpServletRequest) request).getSession(false);
        if(sess == null) {
            chain.doFilter(request, response);
            return;
        }
        //notify ZK's HttpSessionListener not to clean up ZK Session when
        //Spring filters such as AuthenticationProcessingFilter or 
        //SessionFixationProtectionFilter invalidate the native session.
        //See ZK feature 2117539
        sess.setAttribute(Attributes.RENEW_NATIVE_SESSION, Boolean.TRUE);
        
        try {
	        chain.doFilter(request, response);
        } finally {
	        //force removing in case ZkEnableSessionInvalidateFilter
        	//is not called. session might have changed in doFilter()...
	        final HttpSession sess2 = ((HttpServletRequest) request).getSession(false);
	        if (sess2 != null) {
	        	sess2.removeAttribute(Attributes.RENEW_NATIVE_SESSION);
	        }
        }
	}
}
