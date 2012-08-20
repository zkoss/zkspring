/* ZkSecurityContextListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 8, 2008 4:25:40 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.config;

import java.util.List;

//import org.springframework.security.context.SecurityContext;
//import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventThreadCleanup;
import org.zkoss.zk.ui.event.EventThreadInit;
import org.zkoss.zk.ui.event.EventThreadResume;

/**
 * Synchronize the Spring Security SecurityContext between servlet thread and
 * event thread.
 * 
 * @author henrichen
 * @since 1.0
 */
public class ZkSecurityContextListener implements EventThreadInit, EventThreadCleanup, EventThreadResume {
	private SecurityContext _ctx;
	private final boolean _enabled; //whether event thread enabled

	public ZkSecurityContextListener() {
		final WebApp app = Executions.getCurrent().getDesktop().getWebApp();
		_enabled = app.getConfiguration().isEventThreadEnabled();
	}
	
	//-- EventThreadInit --//
	public void prepare(Component comp, Event event) throws Exception {
		if (_enabled) {
			_ctx = SecurityContextHolder.getContext(); //servlet thread
		}
	}

	public boolean init(Component comp, Event event) throws Exception {
		if (_enabled) {
			SecurityContextHolder.setContext(_ctx); //event thread
		}
		return true;
	}


	//-- EventThreadCleanup --//
	public void cleanup(Component comp, Event evt, List errs) throws Exception {
		if (_enabled) {
			_ctx = SecurityContextHolder.getContext(); //event thread
		}
	}

	public void complete(Component comp, Event evt) throws Exception {
		if (_enabled) {
			SecurityContextHolder.setContext(_ctx); //servlet thread
		}
	}

	//-- EventThreadResume --//
	public void beforeResume(Component comp, Event evt) throws Exception {
		if (_enabled) {
			_ctx = SecurityContextHolder.getContext(); //servlet thread
		}
	}

	public void afterResume(Component comp, Event evt) throws Exception {
		if (_enabled) {
			SecurityContextHolder.setContext(_ctx); //event thread
		}
	}

	public void abortResume(Component comp, Event evt) throws Exception {
		// do nothing //servlet thread
	}
}
