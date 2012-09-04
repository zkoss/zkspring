/* ZkEventProcessInterceptor.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 1, 2008 10:17:04 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.intercept.zkevent;

//import org.springframework.security.intercept.AbstractSecurityInterceptor;
//import org.springframework.security.intercept.InterceptorStatusToken;
//import org.springframework.security.intercept.ObjectDefinitionSource;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.zkoss.zk.ui.event.Event;

/**
 * <p>
 * Interceptor to secure event processing per the {@link org.zkoss.zk.ui.Component} 
 * path pattern and event name.
 * Determine if the request relates to a secured or public invocation by looking up the secure object request against the SecurityMetadataSource.
 * </p>
 * About AbstractSecurityInterceptor's basic concept, refer to Spring Security Reference 3.0.7 "Secure Objects and the AbstractSecurityInterceptor".
 * @author henrichen
 * @since 1.0
 */
public class ZkEventProcessInterceptor extends AbstractSecurityInterceptor {
	private InterceptorStatusToken _token;
	//{Event= {Component-Path = Role} } map, e.g. onClick={/mybtn_*=[ROLE_TELLER]}
    private SecurityMetadataSource _securityMetadataSource;
	
	public Class getSecureObjectClass() {
		return Event.class;
	}

//    public SecurityMetadataSource obtainObjectDefinitionSource() {
//        return _objectDefinitionSource;
//    }

    public void setObjectDefinitionSource(ZkEventProcessDefinitionSource newSource) {
        _securityMetadataSource = newSource;
    }

	public Event beforeInvocation(Event event) {
		_token = super.beforeInvocation(event);
		return event;
	}

	public void afterInvocation(Event event) {
		super.afterInvocation(_token, null);
	}

	@Override
	public SecurityMetadataSource obtainSecurityMetadataSource() {
		return _securityMetadataSource;
	}
}
