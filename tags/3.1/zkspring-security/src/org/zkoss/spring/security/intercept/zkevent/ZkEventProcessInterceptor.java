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
 * Interceptor to secure event processing per the {@link org.zkoss.zk.ui.Component} 
 * path pattern and event name.
 * @author henrichen
 * @since 1.0
 */
public class ZkEventProcessInterceptor extends AbstractSecurityInterceptor {
	private InterceptorStatusToken _token;
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
		// TODO Auto-generated method stub
		return _securityMetadataSource;
	}
}
