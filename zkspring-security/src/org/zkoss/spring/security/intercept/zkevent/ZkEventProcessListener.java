/* ZkEventProcessListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 1, 2008 6:17:08 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.intercept.zkevent;

import java.util.List;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.config.http.ZkEventSecurityBeanDefinitionParser;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.zkoss.spring.SpringUtil;
import org.zkoss.spring.security.config.ZkBeanIds;
import org.zkoss.spring.security.ui.ZkError403Filter;
import org.zkoss.spring.security.ui.ZkExceptionTranslationListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.EventInterceptor;

/**
 * <p>
 * Intercept ZK event processing and pass to {@link ZkEventProcessInterceptor} which inherits Spring Security's interceptor
 * in order to secure ZK event objects. 
 * </p>
 * <p>
 * If you configure custom filter ZK_DESKTOP_REUSE_FILTER in &lt;http&gt;, {@link org.zkoss.spring.init.SecurityWebAppInit} will add this listener 
 * to ZK configuration dynamically.
 * </p>
 * 
 * @author henrichen
 * @since 1.0
 */
public class ZkEventProcessListener implements EventInterceptor {
	private static final String DEFAULT_EVENT_PROCESS_INTERCEPTOR_NAME = "zkEventProcessInterceptor";
	private final Log logger = LogFactory.getLog(getClass());
	
	private ZkEventProcessInterceptor _interceptor;
	private boolean skip = false;
	
	public void afterProcessEvent(Event event) {
		if (!skip){
			_interceptor.afterInvocation(event);
		}
	}

	public Event beforeProcessEvent(Event event) {
		
		if (skipInterceptionCheck()) {
			return event;
		}
		
		//if in ZK event exception translation, then ignore any further events
		if (Executions.getCurrent().getAttribute(ZkExceptionTranslationListener.ZK_EXCEPTION_TRANSLATION) != null) {
			return event;
		}
		
		if (_interceptor == null) {
			_interceptor = (ZkEventProcessInterceptor)SpringUtil.getBean(DEFAULT_EVENT_PROCESS_INTERCEPTOR_NAME, ZkEventProcessInterceptor.class);
			if (_interceptor == null) {
				_interceptor = new ZkEventProcessInterceptor(); 
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("intercept before "+event);
		}
		return _interceptor.beforeInvocation(event);
	}

	/**
	 * <p>
	 * All intercepted events will invoke this listener but some of them are not secure objects, we should skip them to avoid Spring Security throwing exceptions.
	 * </p>
	 * Since Spring Security 3.1.0, configuration file can have multiple &lt;http&gt; tags. Each &lt;http&gt; creates a DefaultSecurityFilterChain 
	 * with a request path matcher. we should skip events sent from zuls and do not pass it to {@link ZkEventProcessInterceptor} under the following conditions:<br/>
	 * <li>zuls inside &lt;http security="none"&gt;. If no skip, Spring Security will throw AuthenticationCredentialsNotFoundException.</li> 
	 * <li>zuls inside &lt;http&gt; tag without ZK custom filters.</li>
	 * 
	 * @return true - skip current event.
	 */
	private boolean skipInterceptionCheck(){
		skip = false;
		if (SecurityContextHolder.getContext().getAuthentication() == null) { //pages without access control
			skip = true;
		}else{
			List<SecurityFilterChain> filterChains = (List<SecurityFilterChain>) SpringUtil.getBean(ZkEventSecurityBeanDefinitionParser.SPRING_SECURITY_31_FILTER_CHAIN);
			if (filterChains == null){
				skip = false;
			}else{
				HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();

				for (int i = 0; i < filterChains.size(); i++) {
					if (filterChains.get(i).matches(request)) {
						List<Filter> matchingFilters = filterChains.get(i).getFilters();
						// check if there is a ZK filter installed in the matching chain
						ZkError403Filter zkError403Filter = (ZkError403Filter) SpringUtil.getBean(ZkBeanIds.ZK_ERROR_403_FILTER);
						if (!matchingFilters.contains(zkError403Filter)) {
							skip = true;
							break;
						}
					}
				}
			}
		}
		return skip;
	}

	public Event beforePostEvent(Event event) {
		//do nothing
		return event;
	}

	public Event beforeSendEvent(Event event) {
		//do nothing
		return event;
	}
}
