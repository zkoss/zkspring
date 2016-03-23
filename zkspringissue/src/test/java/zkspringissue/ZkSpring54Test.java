package zkspringissue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.access.ConfigAttribute;
import org.zkoss.spring.security.intercept.zkevent.ZkEventProcessDefinitionSourceImpl;
import org.zkoss.spring.security.util.AntUrlPathMatcher;
import org.zkoss.spring.security.util.UrlMatcher;
import org.zkoss.zk.ui.event.Event;

public class ZkSpring54Test {

	@Test
	public void testDummyEvent() {
		UrlMatcher matcher = new AntUrlPathMatcher();
		ZkEventProcessDefinitionSourceImpl testee = new ZkEventProcessDefinitionSourceImpl(matcher);
		testee.addSecureEvent("/**", null, Collections.singletonList(configAttr("ANONYMUS")));
		testee.addSecureEvent("/some/other", null, Collections.singletonList(configAttr("OTHER")));
		
		//no NPE should occur
		Collection<ConfigAttribute> attributes = testee.getAttributes(new Event("dummy"));

		Set<String> roles =  new HashSet<String>();
		for (ConfigAttribute configAttribute : attributes) {
			roles.add(configAttribute.getAttribute());			
		}
		Assert.assertEquals("expect ANONYMUS", Collections.singleton("ANONYMUS"), roles);
	}

	private ConfigAttribute configAttr(String myval) {
		return new DummyConfigAttribute(myval);
	}
	
	class DummyConfigAttribute implements ConfigAttribute {
		private String attribute;
		public DummyConfigAttribute(String attribute) {
			this.attribute = attribute;
		}
		@Override
		public String getAttribute() {
			return attribute;
		}
		
	}
}
