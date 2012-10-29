/* ListModelConverter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 23, 2008 7:07:06 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.binding.convert.converters;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.binding.convert.converters.Converter;
import org.springframework.util.ClassUtils;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.ListModelMap;
import org.zkoss.zul.ListModelSet;

/**
 * A {@link Converter} implementation that converts an Object array, List, Set,
 * or Map into a ZK {@link ListModel}
 * 
 * @author henrichen
 * @since 1.1
 */
public class ListModelConverter implements Converter {

	public Object convertSourceToTargetClass(Object source, Class targetClass)
	throws Exception {
		if (targetClass.equals(ListModel.class)) {
			if (source instanceof ListModel) {
				return source;
			} else if (source instanceof Set) {
				return new ListModelSet((Set)source, true);
			} else if (source instanceof List) {
				return new ListModelList((List)source, true);
			} else if (source instanceof Map) {
				return new ListModelMap((Map)source, true);
			} else if (source instanceof Object[]) {
				return new ListModelArray((Object[]) source, true);
			} else if ((source instanceof Class) && Enum.class.isAssignableFrom((Class)source)) {
				return new ListModelArray((Object[]) ((Class)source).getEnumConstants(), true);
			} else {
				throw new UiException("Expects java.util.Set, java.util.List, java.util.Map, Object[], Enum Class, or ListModel only. "+source.getClass());
			}
		} else {
			Constructor emptyConstructor = ClassUtils.getConstructorIfAvailable(targetClass, new Class[] {});
			ListModel model = (ListModel) emptyConstructor.newInstance(new Object[] {source, Boolean.TRUE});
			return model;
		}
	}

	public Class getSourceClass() {
		return Object.class;
	}

	public Class getTargetClass() {
		return ListModel.class;
	}

}
