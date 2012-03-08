/* ZkEventListenerFactoryBean.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 13, 2009 11:39:22 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.bean;

import java.lang.reflect.Method;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Strings;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.ForwardEvent;

/**
 * @deprecated after release of zkspring 3.1, suggest to use SelectorComposer or GenericForwardComposer
 * 
 * ZKMethodEventListener initialize as late as when first event handled. 
 * @author henrichen
 *
 */
public class ZkMethodEventListener implements EventListener {
	private String _controllerid;
	private String _mtdname;
	private Object _controller;
	private Method _mtd;

	public ZkMethodEventListener(String controllerid, String methodname) {
		if (Strings.isBlank(controllerid)) {
			throw new UiException("'controllerid' is required.");
		}
		if (Strings.isBlank(methodname)) {
			throw new UiException("'methodname' is required.");
		}
		_controllerid = controllerid;
		_mtdname = methodname;
	}
	
	public String getControllerId() {
		return _controllerid;
	}
	
	private void init() {
		if (_controller == null) {
			_controller = SpringUtil.getBean(_controllerid);
			final Class cls = _controller.getClass();
			try {
				_mtd = Classes.getCloseMethodBySubclass(cls, _mtdname, new Class[] {Event.class});
			} catch (NoSuchMethodException e) {
				//try without argument case
				try {
					_mtd = Classes.getCloseMethodBySubclass(cls, _mtdname, null);
				} catch (NoSuchMethodException e1) {
					throw UiException.Aide.wrap(e1);
				}
			}
		}
	}
	
	public void onEvent(Event evt) throws Exception {
		init();
		if (_mtd.getParameterTypes().length == 0) {
			_mtd.invoke(_controller, (Object[])null);
		} else if (evt instanceof ForwardEvent) { //ForwardEvent
			final Class paramcls = (Class) _mtd.getParameterTypes()[0];
			//paramcls is ForwardEvent || Event
			if (ForwardEvent.class.isAssignableFrom(paramcls)
			|| Event.class.equals(paramcls)) { 
				_mtd.invoke(_controller, new Object[] {evt});
			} else {
				do {
					evt = ((ForwardEvent)evt).getOrigin();
				} while(evt instanceof ForwardEvent);
				_mtd.invoke(_controller, new Object[] {evt});
			}
		} else {
			_mtd.invoke(_controller, new Object[] {evt});
		}
	}
}
