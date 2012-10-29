/* ZkAuthenticationEntryPoint.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 17, 2008 3:32:48 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.ui.webapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.util.StringUtils;
import org.zkoss.spring.security.ui.ZkExceptionTranslationListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;

/**
 * <p>Used by {@link ZkExceptionTranslationListener} to commence an authentication
 * scheme.</p> 
 * <p>This implementation pops up a login Window and show the login form page
 * (see {@link #setLoginFormUrl}) 
 * you specified(or the default one if you did not specify) as the content of the
 * window. When the user login OK,
 * it then show the login OK page you specified(see {@link #setLoginOKUrl}) or 
 * the default one if you did not specified) and close the pop up window 
 * automatically.</p>
 * <p>If you want to customize your pop up login window, you can choose one of 
 * following options per your requirements:
 * <ol>
 * <li>Use the default login window template and change the template arguments 
 * to the default login window template (see {@link #setLoginTemplateArg})</li>
 * <li>Create and provide own login window template(see {@link #setLoginTemplate})
 *  and template arguments(see {@link #setLoginTemplateArg}).</li>
 * </ol>
 * 
 * @author henrichen
 * @since 1.0
 */
public class ZkAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
	private Map _loginTemplateArgs = new HashMap(); //argument to be passed  into login window template URL
	private String _loginTemplate; //login window template URL
	private String _loginOKTemplate; //login window template URL
	private String _loginOKUrl; //login OK URL
	private String _loginFailUrl; //login Fail URL
	private int _delay = 0; //the delay time to close the pop up login window, default to close immediatly.
	
    public static final String SPRING_SECURITY_SAVED_REQUEST_KEY = "SPRING_SECURITY_SAVED_REQUEST_KEY";
	/** Provided login ok url */
	public static final String LOGIN_OK_URL = "ZKSPRING_SECURITY_LOGIN_OK_URL";
	/** Provided login ok template url */
	public static final String LOGIN_OK_TEMPLATE = "ZKSPRING_SECURITY_LOGIN_OK_TEMPLATE";
	/** Provided login fail url (provided for ajax login) */
	public static final String LOGIN_FAIL_URL = "ZKSPRING_SECURITY_LOGIN_FAIL_URL";
	/** The URL which enter the login */
	public static final String SAVED_URL = "ZKSPRING_SECURITY_SAVED_URL";
	/** The desktop which enter the login */
	public static final String SAVED_DESKTOP = "ZKSPRING_SECURITY_SAVED_DESKTOP";
	/** The popup template window uuid, need this info to remove it when login ok */
	public static final String LOGIN_WIN = "ZKSPRING_SECURITY_LOGIN_WIN";
	/** Force desktop to use https after successful authentication */
	public static final String FORCE_HTTPS = "ZKSPRING_SECURITY_FORCE_HTTPS";
	/** Delay seconds to close the popup login window */
	public static final String LOGIN_OK_DELAY = "ZKSPRING_SECURITY_LOGIN_OK_DELAY";

	/** ZK Events that have not been processed because of the Authentication exception */
	public static final String EVENTS = "ZKSPRING_SECURITY_EVENTS";
	/** Spring Authentication object */
	public static final String AUTH = "ZKSPRING_SECURITY_AUTH"; 

	private static final String DEFAULT_LOGIN_TEMPLATE = "~./zul/zkspring/security/loginTemplate.zul";
	private static final String DEFAULT_LOGIN_OK = "~./zul/zkspring/security/loginOK.zul";
	private static final String DEFAULT_LOGIN_OK_TEMPLATE = "~./zul/zkspring/security/loginOKTemplate.zul";
	
	public ZkAuthenticationEntryPoint() {
		setLoginFormUrl(DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL);
		//setForceHttps(true);
	}
	
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException,
			ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        final String template = getLoginTemplate();
		final String loginUrl = buildRedirectUrlToLoginPage(httpRequest, httpResponse, authException);
        final String loginOKUrl = getLoginOKUrl() == null ? 
        		DEFAULT_LOGIN_OK : getLoginOKUrl();
        final String loginOKTemplate = getLoginOKTemplate() == null ?
        		DEFAULT_LOGIN_OK_TEMPLATE : getLoginOKTemplate();
        final String loginFailUrl = getLoginFailUrl();
        
		final Map args = new HashMap(getLoginTemplateArgs());
		args.put("loginUrl", loginUrl);
        args.put("loginOKUrl", loginOKUrl);
        if (loginFailUrl != null) {
        	args.put("loginFailUrl", loginFailUrl);
        }
        args.put("loginOKTemplate", loginOKTemplate);
		args.put("loginOKDelay", new Integer(_delay));
		
		//Check if violates "Same origin policy for JavaScript" (iframe script 
		//cannot access main page script and vice versa if not same origin)
		//See http://developer.mozilla.org/En/Same_origin_policy_for_JavaScript
        final String scheme = request.getScheme().toLowerCase();
		if (isForceHttps() && "http".equals(scheme)) {
			args.put("forceHttps", Boolean.TRUE);
		}
		// Need to flush previously saved request - Bug fix for secure page showing up in login popup
//		httpRequest.getSession().setAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY, null);
		
		Executions.createComponents(
				template == null || template.trim().length() == 0 ? 
					DEFAULT_LOGIN_TEMPLATE : template, null, args);
	}

	/** 
	 * <p>The argument to be passed into the login window template 
	 * (see {@link #getLoginTemplate}). If you did not specify own login
	 * window template, the system use the default one.</p>
	 * 
	 * <p>In login window template, you can customize it by passing arguments
	 * into it via this method. And you refer the parameter by  
	 * specifying in the template window in EL with ${arg.xxx} or 
	 * in &lt;zscript> with arg.xxx.</p>
	 * 
	 * <p>If you don't specify your own login window template, you can still
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
	public void setLoginTemplateArg(String param, Object value) {
		_loginTemplateArgs.put(param, value);
	}
	
	/**
	 * Remove the specified login template parameter.
	 * @param param the login template parameter to be removed.
	 */
	public void removeLoginTemplateArg(String param) {
		_loginTemplateArgs.remove(param);
	}
	
	/**
	 * Sets the login template window arguments Map.
	 * @param map the login template window arguments Map.
	 */
	public void setLoginTemplateArgs(Map map) {
		_loginTemplateArgs = map;
	}

	/**
	 * Return the login template window arguments Map.
	 * @return the login template window arguments Map.
	 */
	public Map getLoginTemplateArgs() {
		return _loginTemplateArgs;
	}
	
	/**
	 * <p>Set the login template URL. You don't generally need to call this
	 * method. If you don't use own login window template, the system will
	 * use the default login window template.</p>
	 * 
	 * <p>The default login window template will embed the login form and pop 
	 * up as a highlighted window. You can use {@link #setLoginTemplateArg(String, Object)}
	 * to customize the arguments in a limit way. Also you can define your own 
	 * login form (see {@link #setLoginFormUrl(String)}) and login OK page 
	 * (see {@link #setLoginOKUrl(String)}) if you like. Note that, the default 
	 * login window template will post "onLogin" event to all root components 
	 * of the page that triggers this login when it pop up for user to login. 
	 * And then it will post "onLoginOK" event to all root components of the 
	 * page that triggers this login if user is authenticated successfully. 
	 * You can get the {@link org.springframework.security.Authentication} by 
	 * calling {@link Event#getData()} of the "onLoginOK" event</p>
	 * 
	 * <p>If you want to define your own login window template, note that this 
	 * class simply call Executions.createComponents(getLoginTemplate(), null, getLoginTemplateArgs())
	 * when it needs an authentication.</p> 
	 * 
	 * @param templateURL the login window template URL
	 * @see #setLoginTemplateArg(String, Object)
	 * @see #setLoginFormUrl(String)
	 * @see #setLoginOKUrl(String)
	 * @see #setLoginOKDelay(int)
	 */
	public void setLoginTemplate(String templateURL) {
		_loginTemplate = templateURL;
	}
	
	/**
	 * Returns the login template URL.
	 * @return the login template URL.
	 */
	public String getLoginTemplate() {
		return _loginTemplate;
	}

	/**Returns the login OK Template URL.
	 * @return the login OK template
	 */
	public String getLoginOKTemplate() {
		return _loginOKTemplate;
	}

	/**<p> Set the login OK URL. You don't generally need to call this
	 * method. If you don't use own login ok template, the system will
	 * use the default login ok template. Note that this page will be 
	 * shown in the pop up login window when login OK.</p>
	 * @param loginOKTemplate the login OK template URL to set
	 */
	public void setLoginOKTemplate(String loginOKTemplate) {
        if (!StringUtils.hasText(loginOKTemplate))
        	loginOKTemplate = null;
		this._loginOKTemplate = loginOKTemplate;
	}

	/**Returns the login OK URL.
	 * @return the loginOK
	 */
	public String getLoginOKUrl() {
		return _loginOKUrl;
	}

	/**Set the login OK URL. This page will be shown in the login ok template
	 * when login OK.
	 * @param loginOKUrl the loginOK URL to set
	 */
	public void setLoginOKUrl(String loginOKUrl) {
        if (!StringUtils.hasText(loginOKUrl))
        	loginOKUrl = null;
		this._loginOKUrl = loginOKUrl;
	}
	
	/**Returns the login Fail URL.
	 * @return the loginFail url.
	 */
	public String getLoginFailUrl() {
		return _loginFailUrl;
	}
	
	/**Sets the login Fail URL. This page will be shown in the login ok template
	 * when login fail.
	 * @param loginFailUrl the login Fail URL to set
	 */
	public void setLoginFailUrl(String loginFailUrl) {
		if (!StringUtils.hasText(loginFailUrl))
			loginFailUrl = null;
		this._loginFailUrl = loginFailUrl;
	}
	
	/**
	 * Returns the delay seconds to close the login window after login 
	 * OK(successfully). Value that equal to 0 means close immediately; seconds
	 * that less than 0 means do not close automatically. Default to 0.
	 * @return the delay seconds after login successfully
	 */
	public int getLoginOKDelay() {
		return _delay;
	}
	
	/**
	 * Sets the delay seconds to close the login window after login 
	 * OK(successfully). Seconds that equal to 0 means close immediately; seconds
	 * that less than 0 means do not close automatically. Default to 0.
	 * @param seconds the delay seconds after login successufuly.
	 */
	public void setLoginOKDelay(int seconds) {
		_delay = seconds;
	}
	
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {
    	final String loginForm = getLoginFormUrl(); 
        return loginForm == null || loginForm.trim().length() == 0 ?
        	DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL : loginForm;
    }
}
