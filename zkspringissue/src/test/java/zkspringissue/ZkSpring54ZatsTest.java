package zkspringissue;

import org.junit.Test;
import org.zkoss.zats.mimic.DesktopAgent;

public class ZkSpring54ZatsTest extends ZatsBaseTest {

	@Test
	public void testDummyRequest() {
		DesktopAgent agent = open("/zkspring-54-zats.zul");
		String desktopId = agent.getId();
		getClientCtrl().postUpdate(desktopId, null, "dummy", null, false);
		getClientCtrl().flush(desktopId);
	}
}