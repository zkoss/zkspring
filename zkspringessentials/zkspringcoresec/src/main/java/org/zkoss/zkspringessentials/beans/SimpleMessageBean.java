/**
 * 
 */
package org.zkoss.zkspringessentials.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author ashish
 *
 */
@Component("msgBean")
@Scope("desktop")
public class SimpleMessageBean {

	private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
