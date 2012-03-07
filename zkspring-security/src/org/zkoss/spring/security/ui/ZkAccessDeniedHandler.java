/* ZkAccessDeniedHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 23, 2008 4:39:06 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.zkoss.zk.ui.Executions;

/**
 * <p>This implementation pop up an error window and show the provided
 * error page or code 403</p>
 * 
 * @author henrichen
 * @since 1.0
 */
public class ZkAccessDeniedHandler implements AccessDeniedHandler {
	private Map _errorTemplateArgs = new HashMap();
	private String _errorTemplate; //an URL

	public static final String ERROR_403_URL = "/zk_error_403"; //see ZkExceptionTranslationFilter
	private static final String DEFAULT_ERROR_TEMPLATE = "~./zul/zkspring/security/errorTemplate.zul";

	

	@SuppressWarnings("deprecation")
	public void handle(HttpServletRequest request, HttpServletResponse response, 
	AccessDeniedException accessDeniedException)
	throws IOException, ServletException {
        final String template = getErrorTemplate();
		final Map args = new HashMap(getErrorTemplateArgs());
		
	    // Put exception into argument and session (perhaps of use to a view)
	    args.put(AccessDeniedHandlerImpl.SPRING_SECURITY_ACCESS_DENIED_EXCEPTION_KEY, accessDeniedException);
		args.put("errorUrl", ERROR_403_URL);
		
	    Executions.createComponents(
				template == null || template.trim().length() == 0 ? 
					DEFAULT_ERROR_TEMPLATE : template, null, args);
	}
	
	/** 
	 * <p>The argument to be passed into the error window template 
	 * (see {@link #getErrorTemplate}). If you did not specify own error
	 * window template, the system use the default one.</p>
	 * 
	 * <p>In error window template, you can customize it by passing arguments
	 * into it via this method. And you refer the parameter by  
	 * specifying in the template window in EL with ${arg.xxx} or 
	 * in &lt;zscript> with arg.xxx.</p>
	 * 
	 * <p>If you don't specify your own error window template, you can still
	 * customize the default login window template by assign values to following
	 * parameters.</p> 
	 * 
	 * <table>
	 * <th><td>param</td><td>type</td><td>memo</td></th>
	 * <tr><td>title</td><td>String</td><td>Login Window Title</td></tr>
	 * <tr><td>width</td><td>String</td><td>window width</td></tr>
	 * <tr><td>height</td><td>String</td><td>window height</td></tr>
	 * @param param the parameter key to be passed into login template.  
	 * @param value the parameter value to be passed into login template. 
	 */ 
	public void setErrorTemplateArg(String param, Object value) {
		_errorTemplateArgs.put(param, value);
	}
	
	/**
	 * Remove the specified error template parameter.
	 * @param param the error template parameter to be removed.
	 */
	public void removeErrorTemplateArg(String param) {
		_errorTemplateArgs.remove(param);
	}
	
	/**
	 * Sets the error template window arguments Map.
	 * @param map the error template window arguments Map.
	 */
	public void setErrorTemplateArgs(Map map) {
		_errorTemplateArgs = map;
	}
	
	/**
	 * Return the error template window arguments Map.
	 * @return the error template window arguments Map.
	 */
	public Map getErrorTemplateArgs() {
		return _errorTemplateArgs;
	}
	
	/**
	 * Set the error template URL. This must be a zul page. This implementation 
	 * will simply call Executions.createComponents(getErrorTemplate(), null, getErrorTemplateArgs())
	 * .If you did not specify a template URL, the default template URL is used.
	 * 
	 * @param templateURL the template URL
	 */
	public void setErrorTemplate(String templateURL) {
		_errorTemplate = templateURL;
	}
	
	/**
	 * Returns the error template URL.
	 * @return the error template URL.
	 */
	public String getErrorTemplate() {
		return _errorTemplate;
	}

}
