package org.zkoss.spring.test;

import org.zkoss.spring.test.config.TestBean;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Div;

@VariableResolver(DelegatingVariableResolver.class)
public class ScopesTestComposer extends SelectorComposer<Div> {
	public static final String WEBAPP_SCOPED_FROM_COMPOSER = "webappScopedFromComposer";
	public static final String DESKTOP_SCOPED_FROM_COMPOSER = "desktopScopedFromComposer";
	public static final String EXECUTION_SCOPED_FROM_COMPOSER = "executionScopedFromComposer";
	@WireVariable
	private TestBean webappScopedBean;

	@WireVariable
	private TestBean desktopScopedBean;

	@WireVariable
	private TestBean executionScopedBean;

	@Override
	public void doAfterCompose(Div container) throws Exception {
		super.doAfterCompose(container);
		storeBeansInComponentAttributes(container);
	}

	@Listen("onClick=#rewireBeans")
	public void rewireBeans() {
		Selectors.wireVariables(getSelf(), this, null);
		storeBeansInComponentAttributes(getSelf());
	}

	private void storeBeansInComponentAttributes(Div container) {
		container.setAttribute(WEBAPP_SCOPED_FROM_COMPOSER, webappScopedBean);
		container.setAttribute(DESKTOP_SCOPED_FROM_COMPOSER, desktopScopedBean);
		container.setAttribute(EXECUTION_SCOPED_FROM_COMPOSER, executionScopedBean);
	}

}
