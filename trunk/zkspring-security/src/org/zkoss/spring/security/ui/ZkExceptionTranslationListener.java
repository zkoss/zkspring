/* ZkExceptionTranslationListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 26, 2008 2:52:59 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.security.web.util.ThrowableCauseExtractor;
//import org.springframework.security.ui.ExceptionTranslationFilter;
import org.zkoss.lang.Exceptions;
import org.zkoss.spring.SpringUtil;
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.spring.security.intercept.zkevent.ZkEventProcessInterceptor;
import org.zkoss.spring.security.ui.webapp.ZkAuthenticationEntryPoint;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventThreadCleanup;
import org.zkoss.zk.ui.sys.ExecutionCtrl;

/**
 * <p>Bridge AuthenticationException thrown inside ZK event processing phase to 
 * Spring Security system
 * and make AuthenticationEntryPoint and AccessDeniedHandler Ajaxified.</p>
 *
 * <p>This is generally used with one-page application that users need
 * to login half way. Basically, this implementation pops up a highlighted 
 * login window and embeds the original Spring Security login form inside 
 * the window so the login process still go through the original 
 * Spring Security filters; make it virtually the same as login on another 
 * browser window (but with the same session).</p>
 * 
 * <p>Note this listener handles AuthenticationException thrown inside ZK event
 * processing phase only; so it would mainly handle the protected Method 
 * invocation. ZK also supports protecting event as well. You can configure to
 * protect specific events and components. Please see
 * {@link ZkEventProcessInterceptor} for details.</p>
 *
 * <p>As said, this listener will use a customized zkExceptionTranslationFilter 
 * to do authentication or access denied handling. You can specify the 
 * zkExceptionTranslationFilter in either ways:</p>
 * <ol>
 * <li>Define a Spring bean extends from 
 * org.springframework.security.ui.ExceptionTranslationFilter or use the default
 * implemented {@link ZkExceptionTranslationFilter} and configure it to your
 * needs</li> 
 * <li>Or, if the Spring bean is not defined, this implementation will new a 
 * {@link ZkExceptionTranslationFilter} automatically and use default configuration.</li>
 * </ol>
 * 
 * <p>To enable this listener, just specify the following in WEB-INF/zk.xml</p>
 * <pre><code>
 *	&lt;listener>
 *		&lt;description>ZK Exception Translation Listener&lt;/description>
 *		&lt;listener-class>org.zkoss.spring.ZkExceptionTranslationListener&lt;/listener-class>
 *	&lt;/listener>
 * </code></pre>
 * @see ZkExceptionTranslationFilter
 * @see ZkAuthenticationEntryPoint
 * @see ZkAccessDeniedHandler 
 * @author henrichen
 * @since 1.0
 */
public class ZkExceptionTranslationListener implements EventThreadCleanup {
	private static final String DEFAULT_EXCEPTION_TRANSLATION_FILTER_NAME = "zkExceptionTranslationFilter";
	public static final String ZK_EXCEPTION_TRANSLATION = "zkspring.ZK_EXCEPTION_TRANSLATION";
	
	public void cleanup(Component comp, Event evt, List errs) throws Exception {
		if (errs != null && !errs.isEmpty() && errs.size() == 1) {
			Throwable ex = (Throwable) errs.get(0);
			if (ex instanceof AuthenticationException) {
				ex = Exceptions.findCause(ex, AuthenticationException.class);
			} else if(ex instanceof AccessDeniedException) {
				ex = Exceptions.findCause(ex, AccessDeniedException.class);
			}
			if (ex == null) {
				
			}
			if (ex != null) {
				errs.clear(); //to avoid ZK handle it
				
				//pop all left events away, so they will not be processed
				ExecutionCtrl exectrl = (ExecutionCtrl) Executions.getCurrent();
				List evts = new ArrayList();
				do {
					evts.add(evt);
					evt = exectrl.getNextEvent();
				} while(evt != null);
				ZKProxy.getProxy().setAttribute((Execution)exectrl, ZkAuthenticationEntryPoint.EVENTS, evts);
				doExceptionTranslationFiltering(ex);
			}
		}
	}

	public void complete(Component comp, Event evt) throws Exception {
		// Do nothing
	}

	public void doExceptionTranslationFiltering(final Throwable ex) throws Exception {
		final Execution exec = Executions.getCurrent();
		final String name = DEFAULT_EXCEPTION_TRANSLATION_FILTER_NAME;
		ExceptionTranslationFilter filter = (ExceptionTranslationFilter) 
			SpringUtil.getBean(name, ExceptionTranslationFilter.class);
		if (filter == null) {
			filter = new ZkExceptionTranslationFilter(); 
		}
		ZKProxy.getProxy().setAttribute(exec, ZK_EXCEPTION_TRANSLATION, Boolean.TRUE);
		filter.doFilter(
			(ServletRequest)exec.getNativeRequest(),
			(ServletResponse) exec.getNativeResponse(), 
			new FilterChain() {
				public void doFilter(ServletRequest arg0,
					ServletResponse arg1) throws IOException,
					ServletException {
					//throw exception to trigger login
					if (ex instanceof AuthenticationException) {
						throw (AuthenticationException) ex;
					} else if (ex instanceof AccessDeniedException) {
						throw (AccessDeniedException) ex;
					} else {
						throw new RuntimeException(ex);
					}
				}
			}
		);
	}
}
