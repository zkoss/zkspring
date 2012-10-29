/* ZkFlowUrlHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 5, 2008 11:03:30 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.webflow.context.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;
import org.zkoss.spring.js.ajax.ZkAjaxHandler;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;


/**
 * ZK event flow URL manipulation.
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowUrlHandler extends DefaultFlowUrlHandler {
	private static final String DEFAULT_URL_ENCODING_SCHEME = "UTF-8";

	private String urlEncodingScheme = DEFAULT_URL_ENCODING_SCHEME;

	//override
	public String getFlowExecutionKey(HttpServletRequest request) {
		final Execution exec = Executions.getCurrent();
		String flowExecutionKey = super.getFlowExecutionKey(request);
		if (flowExecutionKey == null && exec != null) { //ZK Ajax request
			flowExecutionKey = ZkFlowContextManager.getFlowExecutionKey(exec);
		}
		return flowExecutionKey; 
	}
	
	//override
	public String getFlowId(HttpServletRequest request) {
		final Execution exec = Executions.getCurrent();
		return exec != null ? ZkFlowContextManager.getFlowId(exec) : super.getFlowId(request);
	}
	
	//override
	public String createFlowExecutionUrl(String flowId, String flowExecutionKey, HttpServletRequest request) {
		final StringBuffer url = new StringBuffer();
		url.append(getRequestURI(request));
		url.append('?');
		appendQueryParameter(url, "execution", flowExecutionKey);
		
		//store in the request, 
		//then ZkFlowResourceListener will store it as variable
//		if (Executions.getCurrent() == null) { //!ZK Ajax request
			request.setAttribute(ZkFlowContextManager.FLOW_REQUEST_URI, getRequestURI(request));
//		}
		request.setAttribute(ZkFlowContextManager.FLOW_ID, flowId);
		request.setAttribute(ZkFlowContextManager.FLOW_EXECUTION_KEY, flowExecutionKey);
		return url.toString();
	}
	
	//override
	public String createFlowDefinitionUrl(String flowId, AttributeMap input, HttpServletRequest request) {
		final StringBuffer url = new StringBuffer();
		url.append(getFlowHandlerUri(request));
		url.append('/');
		url.append(encode(flowId));
		if (input != null && !input.isEmpty()) {
			url.append('?');
			appendQueryParameters(url, input.asMap());
		}
		return url.toString();
	}
	
	private void appendQueryParameters(StringBuffer url, Map parameters) {
		final Iterator entries = parameters.entrySet().iterator();
		while (entries.hasNext()) {
			final Map.Entry entry = (Map.Entry) entries.next();
			appendQueryParameter(url, entry.getKey(), entry.getValue());
			if (entries.hasNext()) {
				url.append('&');
			}
		}
	}

	private void appendQueryParameter(StringBuffer url, Object key, Object value) {
		final String encodedKey = encode(key);
		final String encodedValue = encode(value);
		url.append(encodedKey).append('=').append(encodedValue);
	}

	private String encode(Object value) {
		return value != null ? urlEncode(String.valueOf(value)) : "";
	}

	private String urlEncode(String value) {
		try {
			return URLEncoder.encode(String.valueOf(value), urlEncodingScheme);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Cannot url encode " + value);
		}
	}
	
	private String getFlowHandlerUri(HttpServletRequest request) {
		final String flowRequestUri = getRequestURI(request);
		final int lastSlash = flowRequestUri.lastIndexOf('/');
		return flowRequestUri.substring(0, lastSlash);
	}
	
	private String getRequestURI(HttpServletRequest request) {
		final Execution exec = Executions.getCurrent(); 
		return  exec == null || exec.getAttribute(ZkAjaxHandler.POPUP) != null ? request.getRequestURI() :
			(String) ZkFlowContextManager.getFlowRequestURI(exec); 
	}
}
