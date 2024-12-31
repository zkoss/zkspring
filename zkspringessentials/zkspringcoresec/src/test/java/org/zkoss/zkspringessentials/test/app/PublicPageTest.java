package org.zkoss.zkspringessentials.test.app;

import org.junit.*;
import org.zkoss.zats.junit.*;
import org.zkoss.zats.mimic.*;
import org.zkoss.zul.Button;

import java.util.List;

/**
 * those pages are accessible without authentication
 */
public class PublicPageTest {

    @ClassRule
    public static AutoEnvironment env = new AutoEnvironment("src/main/webapp/WEB-INF", "src/main/webapp");
    @Rule
    public AutoClient client = env.autoClient();

    @Test
    public void home(){
        DesktopAgent desktop = client.connect("/");
        List<ComponentAgent> buttonList = desktop.queryAll("button");
        Assert.assertEquals(4, buttonList.size());
    }

    @Test
    public void index(){
        DesktopAgent desktop = client.connect("/");
        List<ComponentAgent> buttonList = desktop.queryAll("button");
        Assert.assertEquals(4, buttonList.size());
    }

    @Test
    public void login(){
        DesktopAgent desktop = client.connect("/login.zul");
        ComponentAgent submitButton = desktop.query("button");
        Assert.assertEquals("Submit Query", submitButton.as(Button.class).getLabel());
    }

    /**
     * clicking a button with a href causes 403 error, since ZATS tries to send a zkau request before being authenticated.
     * So here we have to connect listAccounts.html directly instead of clicking a button to navigate to that page.
     */
    @Test
    public void listAccount(){
        DesktopAgent desktop = client.connect("/listAccounts.html");
        List<ComponentAgent> buttonList = desktop.queryAll("button");
        Assert.assertEquals(18, buttonList.size());
    }



}
