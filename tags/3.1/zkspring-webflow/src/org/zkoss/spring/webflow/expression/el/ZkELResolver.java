/* ZkELResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 7, 2008 1:09:38 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.webflow.expression.el;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import org.springframework.webflow.execution.RequestContext;
import org.zkoss.lang.reflect.Fields;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;

/**
 * Custom EL resolver that resolves the ZK variables and components. 
 * @author henrichen
 * @since 1.1
 */
public class ZkELResolver extends ELResolver {

	public Class getCommonPropertyType(ELContext elContext, Object base) {
		if (base == null) {
			return RequestContext.class;
		}
		return null;
	}

	public Iterator getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class getType(ELContext elContext, Object base, Object property) {
		return null;
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		if (base == null) {
			final Execution exec = Executions.getCurrent();
			Object value = null;
			if (exec != null) {
				final Event event = (Event) exec.getAttribute("actionEvent");
				if (event != null && property instanceof String) {
					//resolve implicit object
					value = resolveZKImplicit(event, exec, (String)property);
						
					//resolve other ZK object
					if (value == null) {
						final Component comp = (Component) event.getTarget();
						value = comp.getAttribute((String)property, false);
					}
					if (value != null) {
						elContext.setPropertyResolved(true);
					}
				}
			}
			return value;
		} else if (base instanceof Component || base instanceof Page || base instanceof Desktop){
			try {
				final Object value = Fields.get(base, (String) property);
				elContext.setPropertyResolved(true);
				return value;
			} catch (NoSuchMethodException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private Object resolveZKImplicit(Event event, Execution exec, String fdname) {
		if ("event".equals(fdname))
			return event;
		final Component comp = (Component) event.getTarget();
		if ("self".equals(fdname))
			return comp;
		if ("spaceOwner".equals(fdname))
			return comp.getSpaceOwner();
		if ("page".equals(fdname))
			return comp.getPage();
		if ("desktop".equals(fdname))
			return comp.getDesktop();
		if ("session".equals(fdname))
			return comp.getDesktop().getSession();
		if ("application".equals(fdname))
			return comp.getDesktop().getWebApp();
		if ("componentScope".equals(fdname))
			return comp.getAttributes();
		if ("spaceScope".equals(fdname)) {
			final IdSpace spaceOwner = comp.getSpaceOwner();
			return (spaceOwner instanceof Page) ? 
					comp.getPage().getAttributes() : ((Component)spaceOwner).getAttributes();
		}
		if ("pageScope".equals(fdname))
			return comp.getPage().getAttributes();
		if ("desktopScope".equals(fdname))
			return comp.getDesktop().getAttributes();
		if ("sessionScope".equals(fdname))
			return comp.getDesktop().getSession().getAttributes();
		if ("applicationScope".equals(fdname))
			return comp.getDesktop().getWebApp().getAttributes();
		if ("requestScope".equals(fdname))
			return exec.getAttributes();
		if ("execution".equals(fdname))
			return exec;
		if ("arg".equals(fdname))
			return exec.getArg();
		if ("param".equals(fdname))
			return exec.getParameterMap();
		return null;
	}

	public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2)
	throws NullPointerException, PropertyNotFoundException, ELException {
		return false;
	}

	public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3)
	throws NullPointerException, PropertyNotFoundException,
		PropertyNotWritableException, ELException {
	}

}
