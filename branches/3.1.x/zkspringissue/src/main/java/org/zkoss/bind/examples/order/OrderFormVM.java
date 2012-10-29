/* OrderVM.java

	Purpose:
		
	Description:
		
	History:
		2011/10/31 Created by Dennis Chen

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.bind.examples.order;

import java.util.Date;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.validator.AbstractValidator;


/**
 * @author Hawk
 * 
 */
public class OrderFormVM extends OrderVM3{

	@Override
	public Validator getShippingDateValidator() {
		return new AbstractValidator(){
			public void validate(ValidationContext ctx) {
				Date shipping = (Date)ctx.getProperties("shippingDate")[0].getValue();
				Date creation = (Date)ctx.getProperties("creationDate")[0].getValue();
				//do dependent validation, shipping date have to large than creation more than 3 days.
				if(!CaldnearUtil.isDayAfter(creation,shipping,3)){
					addInvalidMessage(ctx,"must large than creation date at least 3 days");
				}
			}
		};
	}
}
