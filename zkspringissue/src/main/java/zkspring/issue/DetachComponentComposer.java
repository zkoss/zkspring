package zkspring.issue;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;

public class DetachComponentComposer extends GenericForwardComposer {
	
	public void onClick$button(){
		Label label  =new Label();
		Events.sendEvent(label, new Event("onTest",label));
	}
}