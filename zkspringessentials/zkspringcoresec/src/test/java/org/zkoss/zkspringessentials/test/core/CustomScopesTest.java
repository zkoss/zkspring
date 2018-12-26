package org.zkoss.zkspringessentials.test.core;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.zkoss.zats.junit.AutoClient;
import org.zkoss.zats.junit.AutoEnvironment;
import org.zkoss.zats.mimic.ComponentAgent;
import org.zkoss.zats.mimic.DesktopAgent;
import org.zkoss.zul.Label;

public class CustomScopesTest {

	@ClassRule
	public static AutoEnvironment autoEnv = new AutoEnvironment("src/main/webapp/WEB-INF", "src/main/webapp");

	@Rule
	public AutoClient autoClient = autoEnv.autoClient();

	@Test
	public void testCustomScopesMain() {
		DesktopAgent dt = autoClient.connect("/core/customScopesMain.zul");
		verifyMessageboxLabel(dt, "");
		updateAppName(dt, "Test Message");
		verifyMessageboxLabel(dt, "Test Message");

		//reload to verify a new page has a new bean
		dt = autoClient.connect("/core/customScopesMain.zul");
		verifyMessageboxLabel(dt, "");
	}

	private void updateAppName(DesktopAgent dt, String appName) {
		appNameInput(dt).input(appName);
		dt.query("#setAppNameBtn").click();
	}

	private void verifyMessageboxLabel(DesktopAgent dt, String expectedValue) {
		dt.query("#showAppNameBtn").click();
		Assert.assertNotNull("Messagebox not open", messagebox(dt));
		Assert.assertEquals("Messagebox value wrong:", expectedValue, messageboxLabel(dt).as(Label.class).getValue());
		messagebox(dt).query("button[label='OK']").click();
		Assert.assertNull("Messagebox should be closed", messagebox(dt));
	}

	private ComponentAgent messageboxLabel(DesktopAgent dt) {
		return messagebox(dt).query("label");
	}

	private ComponentAgent messagebox(DesktopAgent dt) {
		return dt.query("window.z-messagebox-window");
	}

	private ComponentAgent appNameInput(DesktopAgent dt) {
		return dt.query("#name");
	}
}
