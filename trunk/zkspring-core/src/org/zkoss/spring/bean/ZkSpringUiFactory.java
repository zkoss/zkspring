/* ZkSpringUiFactory.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 11, 2008 3:11:20 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.bean;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;

import org.zkoss.idom.Document;
import org.zkoss.spring.security.config.ZkBeanIds;
import org.zkoss.web.servlet.http.Https;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Richlet;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.http.SimpleUiFactory;
import org.zkoss.zk.ui.impl.PageImpl;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.metainfo.PageDefinitions;
import org.zkoss.zk.ui.metainfo.VariableResolverInfo;
import org.zkoss.zk.ui.sys.PageConfig;
import org.zkoss.zk.ui.sys.RequestInfo;

/**
 * Handle new page creation issue when reuse desktop when changing page from 
 * http to https in Spring security. Since 1.2, also handle the PageDefinition 
 * to inject ZkSpringBeanBindingComposer on component so it will be applied to
 * bind the ZK component to Spring bean automatically. 
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkSpringUiFactory extends SimpleUiFactory {
	public static final String DESKTOP_REUSE = "zkoss.spring.DESKTOP_REUSE";
	public static final String DESKTOP_URL = "zkoss.spring.DESKTOP_URL";
	private static final String BINDING_COMPOSER = "${"+ZkBeanIds.ZK_BINDING_COMPOSER+"}";

	public PageDefinition getPageDefinition(RequestInfo ri, String path) {
		PageDefinition pd = super.getPageDefinition(ri, path);
		//bug 2684322: Nullpoint when accese to a nonexist page
		if (pd != null) {
			final ServletRequest request = (ServletRequest) ri.getNativeRequest();
			//bug 2885793: ServletRequest is null for <?import?> page
			if (request != null) {
				if (request.getAttribute(DESKTOP_REUSE) != null) {
					//when reuse, prepare an empty page definition
					return PageDefinitions.getPageDefinitionDirectly(ri.getWebApp(), ri.getLocator(), "<zk></zk>", "zul");
				} else {
					applyZkSpringDelegatingVariableResolver(pd);
					applyZkSpringBeanBindingComposer(pd);
				}
			}
		}
		return pd;
	}

	/**
	 * @since 1.2
	 */
	public PageDefinition getPageDefinitionDirectly(
	RequestInfo ri, String content, String extension) {
		PageDefinition pd = super.getPageDefinitionDirectly(ri, content, extension);
		//bug 2684322: Nullpoint when accese to a nonexist page
		if (pd != null) {
			applyZkSpringDelegatingVariableResolver(pd);
			applyZkSpringBeanBindingComposer(pd);
		}
		return pd;
	}

	/**
	 * @since 1.2
	 */
	public PageDefinition getPageDefinitionDirectly(
	RequestInfo ri, Document content, String extension) {
		PageDefinition pd = super.getPageDefinitionDirectly(ri, content, extension);
		//bug 2684322: Nullpoint when accese to a nonexist page
		if (pd != null) {
			applyZkSpringDelegatingVariableResolver(pd);
			applyZkSpringBeanBindingComposer(pd);
		}
		return pd;
	}
	
	/**
	 * @since 1.2
	 */
	public PageDefinition getPageDefinitionDirectly(
	RequestInfo ri, Reader reader, String extension) throws IOException {
		PageDefinition pd = super.getPageDefinitionDirectly(ri, reader, extension);
		//bug 2684322: Nullpoint when accese to a nonexist page
		if (pd != null) {
			applyZkSpringDelegatingVariableResolver(pd);
			applyZkSpringBeanBindingComposer(pd);
		}
		return pd;
	}
	
	public Desktop newDesktop(RequestInfo ri, String updateURI, String path) {
		final ServletRequest request = (ServletRequest)ri.getNativeRequest();
		final String url = Https.getOriginFullRequest(request);
		final Desktop desktop = super.newDesktop(ri, updateURI, path);
		desktop.setAttribute(DESKTOP_URL, url);
		return desktop;
	}

	public Page newPage(RequestInfo ri, PageDefinition pagedef, String path) {
		final ServletRequest request = (ServletRequest) ri.getNativeRequest();
		if (request.getAttribute(DESKTOP_REUSE) != null) {
			final Page page = (Page) ri.getDesktop().getPages().iterator().next();
			if (page instanceof ReusablePage) {
				((ReusablePage)page).setReuse(true);
				return page;
			} else {
				throw new IllegalStateException("Should be a ReuseablePage: "+page);
			}
		} else {
			return new ReusablePage(pagedef);
		}
	}

	public Page newPage(RequestInfo ri, Richlet richlet, String path) {
		final ServletRequest request = (ServletRequest) ri.getNativeRequest();
		if (request.getAttribute(DESKTOP_REUSE) != null) {
			final Page page = (Page) ri.getDesktop().getPages().iterator().next();
			if (page instanceof ReusablePage) {
				((ReusablePage)page).setReuse(true);
				return page;
			} else {
				throw new IllegalStateException("Should be a ReuseablePage: "+page);
			}
		} else {
			return new ReusablePage(richlet, path);
		}
	}

	//@since 1.2
	private void applyZkSpringDelegatingVariableResolver(PageDefinition pd) {
		final VariableResolverInfo varResolverInfo = new VariableResolverInfo(org.zkoss.spring.DelegatingVariableResolver.class); 
		pd.addVariableResolverInfo(varResolverInfo);
	}

	//since 1.2
	private void applyZkSpringBeanBindingComposer(PageDefinition pd) {
		applyZkSpringBeanBindingComposer(pd.getChildren());
	}
	
	//since 1.2
	private void applyZkSpringBeanBindingComposer(List nodes) {
		for (final Iterator it = nodes.iterator(); it.hasNext();) {
			final Object node = it.next();
			if (node instanceof ComponentInfo) {
				final ComponentInfo ci = (ComponentInfo) node;
				applyZkSpringBeanBindingComposer(ci.getChildren()); //recursive
				final String apply = ci.getApply();
				if (apply == null) { //apply is null
					ci.setApply(BINDING_COMPOSER);
				} else if (apply.indexOf(BINDING_COMPOSER) < 0) { //apply not null but not binding composer yet
					ci.setApply(BINDING_COMPOSER + ","+ apply);
				}
			}
		}
	}
	
	private static class ReusablePage extends PageImpl {
		private static final long serialVersionUID = 20090116165127L;
		private boolean _reuse;

		public ReusablePage(PageDefinition pgdef) {
			super(pgdef);
		}
		public ReusablePage(Richlet richlet, String path) {
			super(richlet, path);
		}
		
		public void setReuse(boolean b) {
			_reuse = b;
		}
		
		//-- PageCtrl --//
		public void preInit() {
			if (_reuse) { //reuse, so don't preInit again
				return; 
			} else {
				super.preInit();
			}
		}
		
		public void init(PageConfig config) {
			if (_reuse) { //reuse, so don't init again
				tryRemoveLoginTemplateWindow();
				return; 
			} else {
				super.init(config);
			}
		}

		//Try to remove the login template window when login OK and reuse the desktop
		private void tryRemoveLoginTemplateWindow() {
			//remove the original popup window
			final Execution exec = Executions.getCurrent();
			final String uuid = ((ServletRequest)exec.getNativeRequest()).getParameter("rm");
			if (uuid != null) {
				final Component popupWin = exec.getDesktop().getComponentByUuidIfAny(uuid);
				if (popupWin != null) {
					Events.postEvent(new Event("onRemoveLoginWin", popupWin));
				}
			}
		}
	}
}
