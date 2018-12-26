/**
 * 
 */
package org.zkoss.zkspringessentials.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkspringessentials.app.beans.SimpleMessageBean;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * @author ashish
 *
 */
@Component("scopesCtrl1")
@Scope("desktop")
public class ScopesController extends SelectorComposer {

	@Autowired
	private SimpleMessageBean msgBean; /*desktop scoped bean, separate instance for each browser tab*/

	@Wire
	private Textbox name;

	@Listen("onClick=#setAppNameBtn")
	public void setAppNameBtn() {
		msgBean.setMsg(name.getValue());
	}

	@Listen("onClick=#showAppNameBtn")
	public void showAppName() {
		Messagebox.show(msgBean.getMsg());
	}

	@Listen("onClick=#showPageBtn")
	public void showPage() {
		Window win = (Window) Executions.createComponents("customScopesWindow.zul", null, null);
		win.doHighlighted();
	}
}
