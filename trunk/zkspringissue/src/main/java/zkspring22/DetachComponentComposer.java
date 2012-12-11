package zkspring22;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class DetachComponentComposer extends GenericForwardComposer {
	
	public void onClick$button(){
		Label label  =new Label();
		Events.sendEvent(label, new Event("onTest",label));
	}
	
	public void onClick$button2(){
		Window inner  =new Window();
		Window outer = new Window();
		outer.appendChild(inner);
		Events.sendEvent(inner, new Event("onTest",inner));
	}
}