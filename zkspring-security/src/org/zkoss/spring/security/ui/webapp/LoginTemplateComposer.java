/* LoginTemplateComposer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 7, 2008 6:00:52 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.ui.webapp;

import java.util.Collection;
import java.util.Iterator;

//import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.spring.bean.ZkSpringUiFactory;
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.URIEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller codes for /web/zul/zkspring/security/loginTemplate.zul
 * @author henrichen
 * @since 1.0
 */
public class LoginTemplateComposer extends GenericForwardComposer {
	private boolean _forceHttps;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		sessionScope.put(ZkAuthenticationEntryPoint.LOGIN_OK_URL, arg.get("loginOKUrl"));
		sessionScope.put(ZkAuthenticationEntryPoint.LOGIN_FAIL_URL, arg.get("loginFailUrl"));
		sessionScope.put(ZkAuthenticationEntryPoint.LOGIN_OK_TEMPLATE, arg.get("loginOKTemplate"));
		sessionScope.put(ZkAuthenticationEntryPoint.SAVED_URL, desktop.getAttribute(ZkSpringUiFactory.DESKTOP_URL));
		sessionScope.put(ZkAuthenticationEntryPoint.SAVED_DESKTOP, desktop.getId());
		sessionScope.put(ZkAuthenticationEntryPoint.LOGIN_WIN, comp.getUuid());
		sessionScope.put(ZkAuthenticationEntryPoint.LOGIN_OK_DELAY, arg.get("loginOKDelay"));
			//these session login info is cleaned in LoginOKTemplateComposer
		
		final Boolean b = (Boolean) arg.get("forceHttps"); 
		_forceHttps = (b != null) && b.booleanValue();
		if (_forceHttps) {
			sessionScope.put(ZkAuthenticationEntryPoint.FORCE_HTTPS, Boolean.TRUE);
		}
	}
	
	public void onCreate() {
		//When popup login window, post onLogin Event to root components.
		Authentication auth = 
						SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			ZKProxy.getProxy().setAttribute(execution, ZkAuthenticationEntryPoint.AUTH, auth);
		}
		//keep the saved desktop for http -> https case
		if (_forceHttps) {
			Clients.evalJavaScript("zk.keepDesktop=true");
		}
		final Collection roots = page.getRoots();
		for (final Iterator it = roots.iterator(); it.hasNext();) {
			final Component root = (Component) it.next();
			if (root != spaceOwner) {
				Events.postEvent(new Event("onLogin", root, auth));
			}
		}
	}

	//when forceHttps, http -> https, will reuse the desktop
	//must request to remove this login window
	//event post by ZkDesktopReuseUiFactory.tryRemoveLoginTemplateWindow() when reuse desktop(loginOK)
	public void onRemoveLoginWin(Event event) {
		//reuse and refresh the desktop, send the onLoginOK to root windows
		postLoginOK();

		//then remove this popup window
		event.getTarget().detach();
	}
	
	private void postLoginOK() {
		//successful authentication, post onLoginOK Event to root
		//components. Event.getData() is the Authentication
		final Authentication auth = 
				SecurityContextHolder.getContext().getAuthentication();
		ZKProxy.getProxy().setAttribute(execution, ZkAuthenticationEntryPoint.AUTH, auth);
		final Collection roots = page.getRoots();
		for (final Iterator it = roots.iterator(); it.hasNext();) {
			final Component root = (Component) it.next();
			if (root != spaceOwner) {
				Events.postEvent(new Event("onLoginOK", root, auth));
			}
		}
	}
	
	//when login ok, iframe uri will change. Close only when uri is the close page
	public void onURIChange$loginfrm(URIEvent event) {
		//when login ok, loginOKUrl will be removed (see ZkLoginOKFilter)
		final Object loginOKUrl = Sessions.getCurrent().getAttribute(ZkAuthenticationEntryPoint.LOGIN_OK_URL);
		
		final String closeUri = event.getURI();
		
		//already login OK and loginOKTemplate request the closePopup.zul page
		if (loginOKUrl == null 
			&& (closeUri != null && closeUri.endsWith("/~./zul/zkspring/security/closePopup.zul"))) { 
			postLoginOK();
			self.detach();
		}
	}
}
