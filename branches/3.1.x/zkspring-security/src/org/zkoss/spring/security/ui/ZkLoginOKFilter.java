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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.zkoss.spring.security.ui.webapp.ZkAuthenticationEntryPoint;

/**
 * This filter check if this request is a redirect request of 
 * successful authentication from {@link org.zkoss.spring.security.ui.webapp.ZkAuthenticationEntryPoint}. If so, forward
 * to the specified LOGIN_OK_URL in session.
 * 
 * @author henrichen
 * @since 1.0
 */
public class ZkLoginOKFilter extends GenericFilterBean {
	private AuthenticationTrustResolver _authResolver = new AuthenticationTrustResolverImpl();
	private String _defaultTargetUrl = "/";
	private String _authenticationFailureUrl;

	protected void doFilterHttp(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

	}

	private String getFullPath(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer(request.getRequestURI());
		if (sb != null && sb.indexOf("zkau") != -1) {
			sb.delete(sb.indexOf("zkau"), sb.indexOf("zkau") + 4);
		}
		final String query = request.getQueryString();
		return (query != null ? sb.append('?').append(query) : sb).toString();
	}
	
	public void setDefaultTargetUrl(String url) {
		_defaultTargetUrl = url;
	}
	
	public void setAuthenticationFailureUrl(String url) {
		_authenticationFailureUrl = url;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		final HttpSession sess = ((HttpServletRequest) request).getSession(false);
        if(sess == null) {
            chain.doFilter(request, response);
            return;
        }
        
        //check if an ZkAuthenticationEntry login OK. If yes, then forward
        //to loginOK page.
        final String loginOKUrl = (String) 
        	sess.getAttribute(ZkAuthenticationEntryPoint.LOGIN_OK_URL);
        
        //the login ok window is pop up and not authenticated yet, so check the authentication
        if (loginOKUrl != null) {
        	final Authentication auth = 
				SecurityContextHolder.getContext().getAuthentication();
        	final String uri = getFullPath((HttpServletRequest) request);
        	final String ctxpath = ((HttpServletRequest) request).getContextPath();
			if (auth != null && auth.isAuthenticated() && !_authResolver.isAnonymous(auth)) {
				//authenticated, remove the login ok url so no longer come here
		    	sess.removeAttribute(ZkAuthenticationEntryPoint.LOGIN_OK_URL);
		    	if ((ctxpath+_defaultTargetUrl).equals(uri)) {
		            String loginOKTemplate = (String) 
		    			sess.getAttribute(ZkAuthenticationEntryPoint.LOGIN_OK_TEMPLATE);
		            if (loginOKTemplate == null) {
		            	loginOKTemplate = ZkAuthenticationEntryPoint.LOGIN_OK_TEMPLATE;
		            }
	            	StringBuffer url = new StringBuffer(128);
	            	url.append("?loginOKUrl="+loginOKUrl);
	            	url.append("&loginOKDelay=").append(sess.getAttribute(ZkAuthenticationEntryPoint.LOGIN_OK_DELAY));
		            if (sess.getAttribute(ZkAuthenticationEntryPoint.FORCE_HTTPS)!=null) {
		            	//prepare form so we can "redirect" to _parent window 
		            	url.append("&savedUrl=").append(sess.getAttribute(ZkAuthenticationEntryPoint.SAVED_URL));
		            	url.append("&desktop=").append(sess.getAttribute(ZkAuthenticationEntryPoint.SAVED_DESKTOP));
		            	url.append("&loginWin=").append(sess.getAttribute(ZkAuthenticationEntryPoint.LOGIN_WIN));
		            }
		            //login OK so forward to the specified LOGIN_OK_TEMPLATE
			        HttpServletRequest httpRequest = (HttpServletRequest) request;
					RequestDispatcher dispatcher = 
						httpRequest.getRequestDispatcher(loginOKTemplate+url);
	                dispatcher.forward(request, response);
	                return;
				}
        	} else { //not authenticated, might be authentication failure
		    	if ((ctxpath+_authenticationFailureUrl).equals(uri)) {
		            //login FAIL so forward to the specified _authenticationFailureUrl
		            final String loginFailUrl = (String) 
		        		sess.getAttribute(ZkAuthenticationEntryPoint.LOGIN_FAIL_URL);
		            //use own login fail url instead of the <http> one
		            if (loginFailUrl != null) {
				        HttpServletRequest httpRequest = (HttpServletRequest) request;
						RequestDispatcher dispatcher = 
							httpRequest.getRequestDispatcher(loginFailUrl);
		                dispatcher.forward(request, response);
		                return;
		            }
				}
        	}
        }
        
        chain.doFilter(request, response);
		
	}

}
