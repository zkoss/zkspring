/* ZkView.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 12, 2008 3:10:13 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.web.servlet.view;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageCriteria;
import org.springframework.binding.message.Severity;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.expression.el.RequestContextELResolver;
import org.zkoss.spring.impl.ZKProxy;
import org.zkoss.spring.js.ajax.ZkAjaxHandler;
import org.zkoss.spring.webflow.context.servlet.ZkFlowContextManager;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.metainfo.Annotation;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.util.Composition;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Window;

/**
 * ZK Spring MVC View. 
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkView extends InternalResourceView {
	private final static String POPUP_TEMPLATE = "~./zul/zkspring/webflow/popupTemplate.zul";
	/**
	 * Render the internal resource given the specified model.
	 * This includes setting the model as request attributes.
	 */
	protected void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		logger.debug("ZK View renderMergedOutputModel: "+model);
		
		final Execution exec = Executions.getCurrent(); 
		if (exec == null) { //!Ajax request
			super.renderMergedOutputModel(model, request, response);
		} else { 
			final Component popTarget = (Component) exec.getAttribute(ZkAjaxHandler.POPUP);
			//remember flow model into request attributes
			//so DelegatingVariableResolver can see it!

			//Expose the model object as request attributes.
			exposeModelAsRequestAttributes(model, request);

			//Expose helpers as request attributes, if any.
			exposeHelpers(request);

			if (popTarget != null) {
				renderPopup(model, request, response, exec, popTarget);
			} else {
				renderFragmentOrRedirect(model, exec);
			}
		}
	}
	
	private void renderFragmentOrRedirect(Map model, Execution exec) {
		String[] fragments = (String[]) model.get(View.RENDER_FRAGMENTS_ATTRIBUTE);
		if (fragments != null && fragments.length > 0) {
			//store new flow context into page
			ZkFlowContextManager.storeFlowContext(exec);
			
			//handle the fragment
			final Map fragmentMap = prepareDesktopFragmentMap();
			final Map pageMap = new HashMap();
			for(int j = 0; j < fragments.length; ++j) {
				replaceFragment(fragments[j], fragmentMap, pageMap);
			}
		} else { //has error message
			final RequestContext requestContext = (RequestContext) model.get(RequestContextELResolver.REQUEST_CONTEXT_VARIABLE_NAME);
			if (requestContext != null) {
				final MessageContext msgctx = requestContext.getMessageContext();
				if (msgctx != null) {
					final Message[] errors =
					    msgctx.getMessagesByCriteria(new ErrorMessageCriteria());
					if (errors.length > 0) {
						//store new flow context into page
						ZkFlowContextManager.storeFlowContext(exec);

						final Map fragmentMap = prepareDesktopFragmentMap();
						//check the special fragment "messages"
						if (fragmentMap.containsKey("messages")) {
							requestContext.getFlashScope().put("errors", errors);
							replaceFragment("messages", fragmentMap, new HashMap());
						} else {
							//TODO ZK 3.6 use "throw new WrongValuesException(WrongValueException[])" 
							final Message msg = errors[0];
							Component comp = null;
							final Object source = msg.getSource();
							if (source instanceof Component) {
								comp = (Component) source;
							} else if (source instanceof String) {
								comp  = (Component) ZkFlowContextManager.getSelf(exec).getAttribute((String)source, false);
							}
							if (comp != null) {
								//cannot throw out exception here, or the flow state will out of sync
								//save in executution and throw it later in ZkFlowHandlerAdapter.handl()
								ZkFlowContextManager.setWrongValueException(exec, new WrongValueException(comp, msg.getText()));
							} else {
								requestContext.getViewScope().put("errors", errors);
								requestContext.getExternalContext().requestFlowExecutionRedirect();
							}
						}
					}
				}
			}
		}
	}
		
	private static class ErrorMessageCriteria implements MessageCriteria {
		public boolean test(Message message) {
			return message != null && (message.getSeverity() == Severity.ERROR || message.getSeverity() == Severity.FATAL);
		}
	}
	
	private void renderPopup(Map model, HttpServletRequest request, 
	HttpServletResponse response, Execution exec, Component popTarget) throws Exception {
		// Determine the path for the request dispatcher.
		String dispatcherPath = prepareForRendering(request, response);
		final Window popupWin = (Window) exec.createComponents(POPUP_TEMPLATE, null, null);
		
		//store flow context information into popup window
		ZKProxy.getProxy().setAttribute(exec, ZkFlowContextManager.FLOW_POPUP_WINDOW, popupWin);
		ZkFlowContextManager.storeFlowContext(exec);
		
		//Some page might be composed with Composition, so always prepare it

		//Bug: If the dispatcherPath page contains zhtml <h:html> + <h:body>, 
		//  the new created <h:body> will replace the page's _defparent. Then 
		//  in fragment cleanup, the <h:html> is detached so the <h:body> does 
		//  not attached to the page any more (because its parent h:html is 
		//  detached) but the _defparent is still the <h:body>. The state of
		//  the page screwed up and things going crazy.
		//exec.setAttribute(Composition.PARENT, popupWin);
		//exec.createComponents(dispatcherPath, popupWin, null);
		
	
		//keep only the fragment part
		String[] fragments = (String[]) model.get(View.RENDER_FRAGMENTS_ATTRIBUTE);
		if (fragments != null && fragments.length > 0) {
			Component[] comps = exec.createComponents(dispatcherPath, null);
			
			//remember flow model stored in request attributes to current Desktop
			//so DelegatingVariableResolver can see it!
			final Map fragmentMap = prepareFragmentMap(new HashMap(), Arrays.asList(comps));
			
			//remove all
			//popupWin.getChildren().clear();

			//attach fragment only
			for (int j = 0; j < fragments.length; ++j) {
				final String fragment = fragments[j];
				final Component fragmentComp = (Component) fragmentMap.get(fragment);
				if (fragmentComp != null) {
					popupWin.appendChild(fragmentComp);
				}
			}
			
			//prepare a data binder for the popupWin
			final AnnotateDataBinder binder = new AnnotateDataBinder(popupWin, true);
			popupWin.setAttribute("binder", binder, true);

			//redraw
			popupWin.invalidate();
		}
	}
	
	private void replaceFragment(String fragment, Map fragmentMap, Map pageMap) {
		logger.debug("ZK View render fragment: "+fragment);
		
		//find old fragment component
		final Component comp = (Component) fragmentMap.get(fragment);
		if (comp == null) { //don't know what to do
			return; 
		}

		//find new fragment component
		final Page page = comp.getPage();
		final String pageUrl = page.getRequestPath();
		Map newMap = (Map) pageMap.get(pageUrl);
		if (newMap == null) {
			newMap = new HashMap();
			pageMap.put(pageUrl, newMap);
			
			final Execution exec = Executions.getCurrent();
			Component[] comps = exec.createComponents(pageUrl, null);

			newMap = prepareFragmentMap(newMap, Arrays.asList(comps));
			pageMap.put(pageUrl, newMap);
		}
		final Component newComp = (Component) newMap.get(fragment);
		
		//remove old fragment
		comp.getChildren().clear();
		
		//attach new fragment
		Component kid = newComp.getFirstChild();
		while (kid != null) {
			final Component nextkid = kid.getNextSibling(); 
			comp.appendChild(kid);
			kid = nextkid;
		}
		
		//redraw in client side: better performance and avoid NativeComponent residue
		comp.invalidate();
	}
	
	//return a Map of (fragment id, associated component) on desktop
	private Map prepareDesktopFragmentMap() {
		final Map fragmentMap = new HashMap();
		final Desktop desktop = Executions.getCurrent().getDesktop();
		final Collection pages = desktop.getPages();
		for (final Iterator it=pages.iterator(); it.hasNext();) {
			final Page page = (Page) it.next();
			prepareFragmentMap(fragmentMap, page.getRoots());
		}
		return fragmentMap;
	}
	
	@SuppressWarnings("deprecation")
	private static Map prepareFragmentMap(Map fragmentMap, Collection comps) {
		for (final Iterator it = comps.iterator(); it.hasNext();) {
			final ComponentCtrl comp = (ComponentCtrl) it.next();
			final Annotation annt = comp.getAnnotation("fragment");
			if (annt != null) {
				final String fragment = annt.getAttribute("value");
				if (fragment != null) {
					if (fragmentMap.containsKey(fragment)) {
						throw new UiException("fragment id must be unique in a desktop. Components:"+comp+", "+fragmentMap.get(fragment)+", Fragment:"+fragment);
					}
					fragmentMap.put(fragment, comp); 
				}
			}
			prepareFragmentMap(fragmentMap, ((Component)comp).getChildren()); //recusive
		}
		return fragmentMap;
	}
}
