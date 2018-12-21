/* DelegatingVariableResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun  1 13:53:53     2006, Created by ashish
}}IS_NOTE

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.lang.Objects;
import org.zkoss.xel.VariableResolver;

/**
 * @deprecated As of release 4.0 obsolete.
 * Implemented identically super class {@link org.zkoss.zkplus.spring.DelegatingVariableResolver}
 *
 * <p>
 * DelegatingVariableResolver for resolving Spring beans,
 * Spring Security variables.
 * </p>
 * <p>
 * It delegates variable resolving to ZK Spring core, ZK Spring Security
 * Resolver if they are on application classpath.
 * <p>
 * Usage:<br>
 * <code>&lt;?variable-resolver class="org.zkoss.spring.DelegatingVariableResolver"?&gt;</code>
 * 
 * <p>Developers can specify a list of class names separated with comma in
 * a library property called <code>org.zkoss.spring.VariableResolver.class</code>,
 * such they are used as the default variable resolvers.
 *
 * @author henrichen
 * @since 3.0
 */
@Deprecated
public class DelegatingVariableResolver extends org.zkoss.zkplus.spring.DelegatingVariableResolver {
}
