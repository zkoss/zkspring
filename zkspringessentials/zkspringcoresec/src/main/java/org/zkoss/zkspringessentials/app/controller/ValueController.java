package org.zkoss.zkspringessentials.app.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zkspringessentials.app.beans.SimpleMessageBean;
import org.zkoss.zul.*;

import java.util.Map;

/**
 * demonstrate several usages of {@link Value}
 */
@Component("valueController")
@Scope("desktop")
public class ValueController extends SelectorComposer {

	@Value("${value.from.file}")
	private String valueFromFile;

	@Value("${listOfValues}")
	private String[] valuesArray;

	@Value("#{${valuesMap}}")
	private Map<String, Integer> valuesMap;
	public String getValueFromFile() {
		return valueFromFile;
	}

	public String[] getValuesArray() {
		return valuesArray;
	}

	public Map<String, Integer> getValuesMap() {
		return valuesMap;
	}
}
