/* ZKProxy.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 13, 2009 12:48:42 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.impl;

import java.lang.reflect.Method;

import javax.servlet.ServletRequest;

import org.zkoss.lang.Classes;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.ext.Scope;
import org.zkoss.zk.ui.ext.Scopes;
import org.zkoss.zk.ui.sys.DesktopCtrl;
import org.zkoss.zk.ui.sys.ExecutionCtrl;

/**
 * A proxy used to access ZK functions that depends on ZK versions.
 * 
 * @author henrichen
 * @since 1.2.0
 */
public class ZKProxy {
	private static Proxy _proxy;
	
	/** Reeturns the ZK Proxy used to access version-dependent features.
	 */
	public static Proxy getProxy() {
		if (_proxy == null) {//no need to synchronized
			try {
				Classes.forNameByThread("org.zkoss.zk.ui.sys.PageRenderer");
				_proxy = newProxy5();
			} catch (ClassNotFoundException ex) {
				_proxy = newProxy3();
			}
		}
		return _proxy;
	}
	
	/** Interface to access version-dependent features of ZK.
	 */
	public static interface Proxy {
		/** 
		 * Returns self of current context.
		 * @return self of current context.
		 */
		public Component getSelf(ExecutionCtrl exec);
		/** Sets an execution attribute.
		 */
		public void setAttribute(Execution exec, String name, Object value);
		/** Removes an execution attribute.
		 */
		public void removeAttribute(Execution exec, String name);
		
		/** Sets an session attribute.
		 */
		public void setAttribute(Session ses, String name, Object value);
		
		/** Removes an session attribute.
		 */
		public void removeAttribute(Session session, String name);
		
		/** Called when ZK Update Engine has sent a response to the client.
		 *
		 * <p>Note: the implementation has to maintain one last-sent
		 * response information for each channel, since a different channel
		 * has different set of request IDs and might resend in a different
		 * condition.
		 *
		 * @param channel the request channel.
		 * For example, "au" for AU requests and "cm" for Comet requests.
		 * @param reqId the request ID that the response is generated for.
		 * Ingore if null.
		 * @param resInfo the response infomation. Ignored if reqId is null.
		 * The real value depends on the caller.
		 */ 
		public void responseSent(DesktopCtrl desktopCtrl, String channel, String reqId, Object resInfo);
	}
	
	private static Proxy newProxy5() {
		return new Proxy() {
			public void removeAttribute(Execution exec, String name) {
				exec.removeAttribute(name);
			}

			public void responseSent(DesktopCtrl desktopCtrl, String channel,
					String reqId, Object resInfo) {
				desktopCtrl.responseSent(reqId, resInfo);
			}

			public void setAttribute(Execution exec, String name, Object value) {
				exec.setAttribute(name, value);
			}

			public void setAttribute(Session session, String name, Object value) {
				session.setAttribute(name, value);
			}
			
			public void removeAttribute(Session session, String name) {
				session.removeAttribute(name);
			}

			public Component getSelf(ExecutionCtrl exec) {
                final Page page = exec.getCurrentPage();
                final Scope scope = Scopes.getCurrent(page);
                if (scope != null) {
                        Object o = scope.getAttribute("self", false);
                        if(o instanceof Component) {
                                Component self = (Component) o;
                                if (self == null) {
                                        self = (Component) Scopes.getImplicit("self", null);
                                }
                                return self;
                        }
                }
                return null;

//				final Page page = exec.getCurrentPage();
//				Scope scope= ZkSpringIntegrationContext.getContextComponent();
//				if(scope == null) {
//					scope = Scopes.getCurrent(page);
//					if (scope != null)  {
//						Object o = null;
//						if(scope instanceof IdSpace) {
//							o = scope.getAttribute("self", false);
//						} else {
//							if(scope instanceof Component) {
//								o = scope.getAttribute("self", false);
//								Component self = (Component) o;
//								if (self == null) {
//									self = (Component) Scopes.getImplicit("self", null);
//								}
//								return self;
//							} 
//						}
//					}
//				} else {
//					return (Component) scope;
//				}
//				return null;
			}
		};
	}
	
	private static Proxy newProxy3() {
		return new Proxy() {

			public void removeAttribute(Execution exec, String name) {
				((ServletRequest)exec.getNativeRequest()).removeAttribute(name);
				//can't access removeAttribute directly, since signature of ZK 5 changed
			}

			public void responseSent(DesktopCtrl desktopCtrl, String channel,
					String reqId, Object resInfo) {
				try {
					final Method m = 
						DesktopCtrl.class.getDeclaredMethod("responseSent", new Class[] {String.class, String.class, Object.class});
					m.invoke(desktopCtrl, new Object[] {channel, reqId, resInfo});
				} catch (Exception e) {
					throw UiException.Aide.wrap(e);
				}
				//can't access responseSent directly, since signature of ZK 5 changed
			}

			public void setAttribute(Execution exec, String name, Object value) {
				((ServletRequest)exec.getNativeRequest()).setAttribute(name, value);
				//can't access setAttribute directly, since signature of ZK 5 changed
			}

			public Component getSelf(ExecutionCtrl exec) {
				final Page page = exec.getCurrentPage();
				final Scope scope = Scopes.getCurrent(page);
				if (scope != null) {
					Component self = (Component) scope.getAttribute("self", true);
					//since ZK 3.6.1, event handling, use getImplicit()
					if (self == null) {
						self = (Component) Scopes.getImplicit("self", null);
					}
					return self;
				}
				return null;
			}
			
			public void setAttribute(Session session, String name, Object value) {
				((javax.servlet.http.HttpSession)session.getNativeSession()).setAttribute(name, value);
			}

			public void removeAttribute(Session session, String name) {
				((javax.servlet.http.HttpSession)session.getNativeSession()).removeAttribute(name);
			}
		};
	}
}
