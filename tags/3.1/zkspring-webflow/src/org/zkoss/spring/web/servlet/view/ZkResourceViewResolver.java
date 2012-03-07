/* ZkResourceViewResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 10, 2008 12:13:23 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.web.servlet.view;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Avoid prefix ZK those special url starts with "~.". 
 * @author henrichen
 * @since 1.1
 */
public class ZkResourceViewResolver extends InternalResourceViewResolver {
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		final AbstractUrlBasedView view = (AbstractUrlBasedView) super.buildView(viewName);
		final String prefix = getPrefix();
		if (StringUtils.hasText(prefix)) {
			final String exceptPath = prefix+"~./";
			if (view.getUrl().startsWith(exceptPath)) {
				final String newUrl = "/"+view.getUrl().substring(prefix.length());
				view.setUrl(newUrl);
			}
		}
		return view;
	}
}
