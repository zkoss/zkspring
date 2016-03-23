package zkspringissue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.zkoss.zats.mimic.Client;
import org.zkoss.zats.mimic.DefaultZatsEnvironment;
import org.zkoss.zats.mimic.DesktopAgent;
import org.zkoss.zats.mimic.impl.ClientCtrl;

public class ZatsBaseTest {

	private static DefaultZatsEnvironment env;
	private Client client;

	@BeforeClass
	public static void init() {
		env = new DefaultZatsEnvironment("./src/main/webapp/WEB-INF");
		env.init("./src/main/webapp");
	}

	@AfterClass
	public static void end() {
		env.destroy();
	}

	public ZatsBaseTest() {
		super();
	}

	@Before
	public void before() {
		client = env.newClient();
	}

	@After
	public void after() {
		env.cleanup();
		client = null;
	}

	protected  DesktopAgent open(String zulPath) {
		return client.connect(zulPath);
	}
	
	protected ClientCtrl getClientCtrl() {
		return (ClientCtrl) client;
	}
}