/* ZkLoginOKFilter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 22, 2008 4:50:59 PM, Created by henrichen
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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.filter.GenericFilterBean;

/**
 * This filter check if this request an ZK Error 403 request caused by
 * {@link ZkAccessDeniedHandler}. If so, response with Error 403.  
 * 
 * @author henrichen
 * @see ZkAccessDeniedHandler
 * @since 1.0
 */
public class ZkError403Filter extends GenericFilterBean {
	private AccessDeniedHandler _accessDeniedHandler = new AccessDeniedHandlerImpl();
	
	public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
	throws IOException,	ServletException {
		
	}
	
	public void setAccessDeniedHandler(AccessDeniedHandler handler) {
		_accessDeniedHandler = handler;
	}


	@SuppressWarnings("deprecation")
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		final HttpSession sess = ((HttpServletRequest) request).getSession(false);
        if(sess == null) {
            chain.doFilter(request, response);
            return;
        }
        
		//process ZkAccessDeniedHandler iframe in errorTemplate
        final AccessDeniedException accessDeniedException = 
        	(AccessDeniedException)	sess.getAttribute(AccessDeniedHandlerImpl.SPRING_SECURITY_ACCESS_DENIED_EXCEPTION_KEY);
        
        if (accessDeniedException != null) {
            //FireFox will fire iframe src request twice. 
            //1st on Executions.createComponents, 2nd on doHighlighted (see ZkAccessDeniedHandler)
            //therefore, we won't remove the attribute here, we remove it when the errorTemplate
            //is closed.
            
           	//sess.removeAttribute(ZkAccessDeniedHandler.SPRING_SECURITY_ACCESS_DENIED_EXCEPTION_KEY);

            final String uri = ((HttpServletRequest) request).getRequestURI();
        	final String ctxpath = ((HttpServletRequest) request).getContextPath();
            final boolean isError403 = (ctxpath+ZkAccessDeniedHandler.ERROR_403_URL).equals(uri);
        	if (isError403) {
        		_accessDeniedHandler.handle((HttpServletRequest)request, (HttpServletResponse) response, accessDeniedException);
        		return;
	    	} else {
	    		request.setAttribute(AccessDeniedHandlerImpl.SPRING_SECURITY_ACCESS_DENIED_EXCEPTION_KEY, accessDeniedException);
	    	}
        }
        chain.doFilter(request, response);
	}

}
