package zkspring7;

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.WrongValuesException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Textbox;

@SuppressWarnings("serial")
public class WrongValueComposer extends GenericForwardComposer {
	
	private Textbox username;
	private Textbox password;
	
	public void onClick$wveButton(){
		username.clearErrorMessage();
		throw new WrongValueException(username, "Not a valid username.");
	}

	public void onClick$wvseButton(){
		username.clearErrorMessage();
		password.clearErrorMessage();
		WrongValueException[] wrongValues = {new WrongValueException(username, "Not a valid username."),
				new WrongValueException(password, "Not a valid password.")};
		throw new WrongValuesException(wrongValues);
	}
	
	public void onClick$myEx(){
		throw new RuntimeException(">>>Hawk runtime exception");
	}
}