/* ZkEventProcessDefinitionSource.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 1, 2008 4:21:36 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.intercept.zkevent;

import org.springframework.security.access.SecurityMetadataSource;

//import org.springframework.security.intercept.ObjectDefinitionSource;

/**
 * Interface for <code>ObjectDefinitionSource</code> implementations
 * that are designed to perform lookups keyed on ZK @{link Event}s.
 *
 * @author henrichen
 * @since 1.0
 */
public interface ZkEventProcessDefinitionSource extends SecurityMetadataSource {
}
