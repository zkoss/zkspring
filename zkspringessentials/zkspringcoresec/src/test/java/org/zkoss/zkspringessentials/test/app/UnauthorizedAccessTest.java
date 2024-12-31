package org.zkoss.zkspringessentials.test.app;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.zkoss.zats.ZatsException;
import org.zkoss.zats.junit.*;
import org.zkoss.zats.mimic.*;

/**
 * Access those pages that require an authentication
 */
public class UnauthorizedAccessTest {

    @ClassRule
    public static AutoEnvironment env = new AutoEnvironment("src/main/webapp/WEB-INF", "src/main/webapp");
    @Rule
    public AutoClient client = env.autoClient();


    @Test
    public void secure(){
        DesktopAgent desktop = client.connect("/secure/index.zul");
        // redirect to login page
        ComponentAgent loginWindow = desktop.query("#loginwin");
        Assert.assertNotNull(loginWindow);
    }

    @Test
    public void extremeSecure(){
        DesktopAgent desktop = client.connect("/secure/extreme/index.zul");
        // redirect to login page
        ComponentAgent loginWindow = desktop.query("#loginwin");
        Assert.assertNotNull(loginWindow);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /* because of <error-reload> in zk.xml, it's better to redirect to login page */
    @Test
    public void blockedOperation(){
        thrown.expect(ZatsException.class);
        thrown.expectMessage("403");

        DesktopAgent desktop = client.connect("/listAccounts.html");
        desktop.query("button[label='-$20']").click();
    }




}
