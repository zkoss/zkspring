/* ZkFlowResourceListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 12, 2008 10:53:02 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.webflow.context.servlet;

import java.util.List;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.util.ExecutionCleanup;

/**
 * Store Web Flow related context when loading a zul page.  
 *
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowResourceListener implements ExecutionCleanup {

	public void cleanup(Execution exec, Execution parent, List errs)
	throws Exception {
		//Flow context is passed here from Web Flow framework
		//via request attributes Map when forwarding to this zul page (loading)
		if (parent == null && !exec.isAsyncUpdate(null)) {
			ZkFlowContextManager.storeFlowContext(exec);
		}
	}
}
