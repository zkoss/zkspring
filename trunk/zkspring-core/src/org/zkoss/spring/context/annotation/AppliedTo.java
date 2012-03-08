/* AppliedTo.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 26, 2008 6:12:06 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated after release of zkspring 3.1, suggest to use SelectorComposer or GenericForwardComposer
 * Used to specify the associated ZK component for the annotated controller class.
 * @author henrichen
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AppliedTo {

	/**
	 * Specifies the associate ZK component for the annotated controller class.
	 * @return the associate ZK component
	 */
	String value();
}
