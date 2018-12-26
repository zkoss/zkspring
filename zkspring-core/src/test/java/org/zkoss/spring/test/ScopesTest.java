package org.zkoss.spring.test;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.zkoss.spring.test.config.TestBean;
import org.zkoss.zats.junit.AutoClient;
import org.zkoss.zats.junit.AutoEnvironment;
import org.zkoss.zats.mimic.DesktopAgent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import static org.zkoss.spring.test.ScopesTestComposer.DESKTOP_SCOPED_FROM_COMPOSER;
import static org.zkoss.spring.test.ScopesTestComposer.EXECUTION_SCOPED_FROM_COMPOSER;
import static org.zkoss.spring.test.ScopesTestComposer.WEBAPP_SCOPED_FROM_COMPOSER;

public class ScopesTest {
	@ClassRule
	public static AutoEnvironment autoEnv = new AutoEnvironment("src/test/webapp/WEB-INF", "src/test/webapp");

	@Rule
	public AutoClient autoClient = autoEnv.autoClient();

	@Test
	public void testScopes() {
		DesktopAgent dt = autoClient.connect("/scopesTest.zul");

		final TestBean webappTestBean = testBean(dt, WEBAPP_SCOPED_FROM_COMPOSER);
		final TestBean desktopTestBean = testBean(dt, DESKTOP_SCOPED_FROM_COMPOSER);
		final TestBean executionTestBean = testBean(dt, EXECUTION_SCOPED_FROM_COMPOSER);

		Assert.assertEquals("initial value should be equal", scopedLabelValue(dt, "#webappScoped"), webappTestBean.getValue());
		Assert.assertEquals("initial value should be equal", scopedLabelValue(dt, "#desktopScoped"), desktopTestBean.getValue());
		Assert.assertEquals("initial value should be equal", scopedLabelValue(dt, "#executionScoped"), executionTestBean.getValue());

		dt.query("#rewireBeans").click();

		final TestBean webappTestBean2 = testBean(dt, WEBAPP_SCOPED_FROM_COMPOSER);
		final TestBean desktopTestBean2 = testBean(dt, DESKTOP_SCOPED_FROM_COMPOSER);
		final TestBean executionTestBean2 = testBean(dt, EXECUTION_SCOPED_FROM_COMPOSER);

		Assert.assertSame("webapp scoped beans must be identical after an event", webappTestBean, webappTestBean2);
		Assert.assertSame("desktop scoped beans must be identical after an event", desktopTestBean, desktopTestBean2);
		Assert.assertNotSame("execution scoped beans must be different after an event", executionTestBean, executionTestBean2);

		DesktopAgent dt2 = autoClient.connect("/scopesTest.zul");

		final TestBean webappTestBean3 = testBean(dt2, WEBAPP_SCOPED_FROM_COMPOSER);
		final TestBean desktopTestBean3 = testBean(dt2, DESKTOP_SCOPED_FROM_COMPOSER);
		final TestBean executionTestBean3 = testBean(dt2, EXECUTION_SCOPED_FROM_COMPOSER);

		Assert.assertSame("webapp scoped beans must be identical after an event", webappTestBean, webappTestBean3);
		Assert.assertNotSame("desktop scoped beans must be different after an event", desktopTestBean, desktopTestBean3);
		Assert.assertNotSame("execution scoped beans must be different after an event", executionTestBean, executionTestBean3);
	}

	private TestBean testBean(DesktopAgent dt, String webappScopedFromComposer) {
		final Div testDivComp = dt.query("#testDiv").as(Div.class);
		return (TestBean) testDivComp.getAttribute(webappScopedFromComposer);
	}

	private String scopedLabelValue(DesktopAgent dt, String s) {
		return dt.query(s).as(Label.class).getValue();
	}
}
