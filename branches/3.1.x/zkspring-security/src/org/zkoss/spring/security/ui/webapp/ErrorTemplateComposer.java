/* ErrorTemplateComposer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 7, 2008 5:53:46 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.ui.webapp;

//import org.springframework.security.ui.AccessDeniedHandlerImpl;
import org.springframework.security.web.WebAttributes;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller codes for /web/zul/zkspring/security/errorTemplate.zul
 * @author henrichen
 * @since 1.0
 */
public class ErrorTemplateComposer extends GenericForwardComposer {
	//override
	@SuppressWarnings("deprecation")
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		session.setAttribute(WebAttributes.ACCESS_DENIED_403, 
				arg.get(WebAttributes.ACCESS_DENIED_403));
	}
	@SuppressWarnings("deprecation")
	public void onClose() {
		session.removeAttribute(WebAttributes.ACCESS_DENIED_403);
	}
}

