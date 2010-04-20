/* ZkDesktopReuseFilter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 11, 2008 2:45:14 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.security.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.filter.GenericFilterBean;
import org.zkoss.spring.bean.ZkSpringUiFactory;
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.http.WebManager;
import org.zkoss.zk.ui.sys.DesktopCache;
import org.zkoss.zk.ui.sys.DesktopCtrl;
import org.zkoss.zk.ui.sys.WebAppCtrl;

/**
 * Reuse ZK desktop. Used when change from http -> https.
 * @author henrichen
 * @since 1.1
 */
public class ZkDesktopReuseFilter extends GenericFilterBean implements
		ServletContextAware {
	static final String ATTR_DESKTOP = "javax.zkoss.zk.ui.desktop"; //== WebManager.ATTR_DESKTOP
	static final String FILTER_APPLIED = "__zk_spring_desktop_reuse_filter_applied";
    protected final Log logger = LogFactory.getLog(getClass());

	private WebApp _webApp;
	private ServletContext _ctx;
	
//	public void setServletContext(ServletContext ctx) {
//		_ctx = ctx;
//	}

	protected void doFilterHttp(HttpServletRequest request,
		HttpServletResponse response, FilterChain chain)
		throws IOException, ServletException {

	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		// If ever applied, go directly to next
        if(((HttpServletRequest) request).getSession(false) == null || request.getAttribute(FILTER_APPLIED) != null) {
            chain.doFilter(request, response);
            return;
        }
        
        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
        
		if (_webApp == null) {
			final WebManager webman = WebManager.getWebManager(((HttpServletRequest) request).getSession(false).getServletContext());
			_webApp = webman.getWebApp();
		}
		
        final Desktop desktop = (Desktop) request.getAttribute(ATTR_DESKTOP);
        if (desktop == null) {
        	final String dtid = request.getParameter("dt");
        	if (dtid != null) {
        		final HttpSession hsess = ((HttpServletRequest)request).getSession(false);
        		if (hsess != null) {
        			if (hsess.getAttribute(dtid) != null) { //with the preset token
        				hsess.removeAttribute(dtid);
	        			final Session sess = ((WebAppCtrl)_webApp).getSessionCache().get(hsess);
	        			if (sess != null) {
	        				final DesktopCache cache = ((WebAppCtrl)_webApp).getDesktopCache(sess);
	        				if (cache != null) {
	        					final Desktop olddesktop = cache.getDesktopIfAny(dtid);
	        					if (olddesktop != null) {
	        						//set ATTR_DESKTOP, so will reuse the old desktop
	        						request.setAttribute(ATTR_DESKTOP, olddesktop);
	        						//cleanup the "au" channel to avoid get the 
	        						//    residue of the previous desktop operation
	        						ZKProxy.getProxy().responseSent((DesktopCtrl)olddesktop, "au", "", Boolean.TRUE);
	        						//setup the DESKTOP_REUSE to notify use the original pages in the desktop
	        						request.setAttribute(ZkSpringUiFactory.DESKTOP_REUSE, Boolean.TRUE);
	        					}
	        				}
	        			}
	        		}
	        	}
        	}
        }
        
		chain.doFilter(request, response);
		
	}
}
