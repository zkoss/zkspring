/* ZkConversionService.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 23, 2008 4:05:18 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.binding.convert.service;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.service.DefaultConversionService;
import org.zkoss.spring.binding.convert.converters.ListModelConverter;
import org.zkoss.zul.ListModel;

/**
 * Convenient {@link ConversionService} implementation for ZK
 * that composes ZK-specific converters with the standard
 * Web Flow converters.
 * 
 * <p>
 * In addition to the standard Web Flow conversion, this service provide 
 * conversion from an Object[], List, Set, Map into a {@link ListModel}
 * using a "listModel" alias for the type.
 * </p>
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkConversionService extends DefaultConversionService {
	public ZkConversionService() {
		super();
		addZkConverters();
	}

	protected void addZkConverters() {
		addConverter(new ListModelConverter());
		addAlias("listModel", ListModel.class);
	}
}
