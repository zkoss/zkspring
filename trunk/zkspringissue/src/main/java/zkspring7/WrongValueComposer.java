package zkspring7;

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Textbox;

public class WrongValueComposer extends GenericForwardComposer {
	
	private Textbox username;
	
	public void onClick$button(){
		username.clearErrorMessage();
		throw new WrongValueException(username, "Not a valid username or password. Please retry.");
	}
}