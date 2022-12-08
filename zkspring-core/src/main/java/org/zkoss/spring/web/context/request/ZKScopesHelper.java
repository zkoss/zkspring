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

package org.zkoss.spring.web.context.request;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.ext.*;
import org.zkoss.zk.ui.sys.ExecutionCtrl;

/**
 * An internal helper used by custom zk scopes
 *
 * @author Robert
 * @since 4.0.0
 *
 * @deprecated obsolete since 4.0.0 kept for backwards compatibility, to be removed in a future version
 */
@Deprecated
class ZKScopesHelper {
	/**
	 * returns the 'self' component associated with the given execution
	 *
	 * @param exec the current ZK execution
	 * @return the 'self' component or null if none found
	 */
	public static Component getSelf(ExecutionCtrl exec) {
		final Page page = exec.getCurrentPage();
		final Scope scope = Scopes.getCurrent(page);
		if (scope != null) {
			Object o = scope.getAttribute("self", false);
			if (o instanceof Component) {
				Component self = (Component) o;
				if (self == null) {
					self = (Component) Scopes.getImplicit("self", null);
				}
				return self;
			}
		}
		return null;
	}
}
