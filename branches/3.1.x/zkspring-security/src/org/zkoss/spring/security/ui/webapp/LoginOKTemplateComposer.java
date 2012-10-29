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

import java.util.Date;

import org.zkoss.mesg.Messages;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Timer;
import org.zkoss.zul.mesg.MZul;

/**
 * Controller codes for /web/zul/zkspring/security/loginTemplate.zul
 * @author henrichen
 * @since 1.1
 */
public class LoginOKTemplateComposer extends GenericForwardComposer {
	private Button closebtn;
	private Timer closetm;
	private String _desktopid;
	
/*	public void doAfterCompose(Component comp) throws Exception {
		//<timer id="closetm" repeats="true" delay="500" running="false" />
		closetm = new Timer(500);
		closetm.setId("closetm");
		closetm.setRepeats(true);
		closetm.setRunning(true);
		closetm.setParent(comp);

		super.doAfterCompose(comp);
	}
*/	
	public void onCreate() {
		//login OK, clear the popup login info from session
		sessionScope.remove(ZkAuthenticationEntryPoint.LOGIN_OK_URL);
		sessionScope.remove(ZkAuthenticationEntryPoint.LOGIN_OK_TEMPLATE);
		sessionScope.remove(ZkAuthenticationEntryPoint.LOGIN_FAIL_URL);
		sessionScope.remove(ZkAuthenticationEntryPoint.SAVED_URL);
		_desktopid = (String) sessionScope.remove(ZkAuthenticationEntryPoint.SAVED_DESKTOP);
		final Boolean forceHttps = (Boolean) sessionScope.remove(ZkAuthenticationEntryPoint.FORCE_HTTPS);
		if (forceHttps == null || !forceHttps.booleanValue()) {
			_desktopid = null;
		}
		sessionScope.remove(ZkAuthenticationEntryPoint.LOGIN_WIN);
		sessionScope.remove(ZkAuthenticationEntryPoint.LOGIN_OK_DELAY);

		//Start count OK button.
		final int delay = Integer.parseInt((String)self.getAttribute("loginOKDelay"));
		if (delay == 0) { //close the window immediately
		    Events.postEvent(new Event("onOK", self));
		} else {
			closebtn.setLabel(Messages.get(MZul.OK));
			closebtn.focus();
			if (delay > 0) {
				closetm.setAttribute("END_TIME", new Long(new Date().getTime() + delay * 1000)); 
				closetm.start();
			}
		}
	}
	
	public void onOK() {
		//A token to indicate reuse the desktop
		//to avoid any desktop is reused automatically
		if (_desktopid != null) {
			sessionScope.put(_desktopid, Boolean.TRUE);
		}
		Clients.submitForm("closePopupForm");
	}
	
	//closebtn
	public void onClick$closebtn() {
		onOK();
	}
	
	//closetm
	public void onTimer$closetm() { 
		final long endtime = ((Long)closetm.getAttribute("END_TIME")).longValue();
		final long curtime = new Date().getTime();
		final long left = (endtime - curtime + 500L) / 1000L;
		if (left <= 0) {
		    Events.sendEvent(new Event("onOK", (Component) self));
		    closetm.stop();
		} else {
			closebtn.setLabel(Messages.get(MZul.OK)+" ("+left+")");
		}
	}
}
