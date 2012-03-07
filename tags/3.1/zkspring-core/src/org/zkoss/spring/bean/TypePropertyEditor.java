/* TypePropertyEditor.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 20, 2008 7:11:58 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.bean;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.propertyeditors.ClassEditor;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.metainfo.ComponentDefinition;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;
import org.zkoss.zk.ui.sys.ExecutionCtrl;

/**
 * PropertyEditor for 'type' property of {@link ZkComponentFactoryBean}.
 * @author henrichen
 * @since 1.2
 */
public class TypePropertyEditor extends ClassEditor {
	public void setAsText(String text) {
		if (text.indexOf('.') < 0 && Character.isLowerCase(text.charAt(0))) {
			final Execution exec = Executions.getCurrent();
			if (exec != null) {
				final Page page = ((ExecutionCtrl)exec).getCurrentPage();
				final ComponentDefinition cd = page.getComponentDefinition(text, true);
				if (cd != null) {
					setAsText0(cd);
					return;
				}
			} else {
				final int j = text.indexOf(':');
				if (j >= 0) {
					final String ext = text.substring(0, j);
					final LanguageDefinition ld = LanguageDefinition.getByExtension(ext);
					final ComponentDefinition cd = ld.getComponentDefinitionIfAny(text.substring(j+1));
					if (cd != null) {
						setAsText0(cd);
						return;
					}
				} else {
					//TODO: search in user specified extention order?
					final List langds = LanguageDefinition.getAll();
					for (final Iterator it = langds.iterator(); it.hasNext();) {
						final LanguageDefinition ld = (LanguageDefinition) it.next();
						final ComponentDefinition cd = ld.getComponentDefinitionIfAny(text);
						if (cd != null) {
							setAsText0(cd);
							return;
						}
					}
				}
			}
		}
		super.setAsText(text);
	}
	
	private void setAsText0(ComponentDefinition cd) {
		final Object klass = cd.getImplementationClass();
		if (klass instanceof String) {
			super.setAsText((String)klass);
		} else if (klass instanceof Class) {
			setValue((Class) klass);
		} else {
			throw new UiException("Should be either String or a Class, but was: "+klass);
		}
	}
}