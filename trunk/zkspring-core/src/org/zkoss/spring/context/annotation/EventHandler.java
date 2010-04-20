/* EventHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 12, 2009 2:18:14 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate method as an event listener of the specified target ZK 
 * component and event name in the form of "abc.onXyz". 
 * 
 * @author henrichen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EventHandler {

	/**
	 * Specifies the target ZK component and event name of the annotated listener method.
	 *  
	 * @return the ZK component and event name in the form of "abc.onXyz" where
	 * "abc" is the component id and "onXyz" is the event name.
	 */
	String value();
}
