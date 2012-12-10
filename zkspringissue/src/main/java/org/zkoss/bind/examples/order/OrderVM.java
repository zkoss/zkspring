/* OrderVM.java

	Purpose:
		
	Description:
		
	History:
		2011/10/31 Created by Dennis Chen

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.bind.examples.order;

import java.util.Calendar;

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zul.ListModelList;

/**
 * @author dennis
 * 
 */
public class OrderVM {

	//the order list
	ListModelList orders;
	
	//the selected order
	Order selected;
	
	private String message;

	public ListModelList getOrders() {
		if (orders == null) {
			//init the list
			orders = new ListModelList(getService().list());
		}
		return orders;
	}

	public Order getSelected() {
		return selected;
	}

	public void setSelected(Order selected) {
		this.selected = selected;
	}

	//action command
	
	@NotifyChange({"selected","orders"})
	@Command
	public void newOrder(){
		Order order = new Order();
		getOrders().add(order);
		selected = order;//select the new one
	}
	
	@NotifyChange("selected")
	@Command
	public void saveOrder(){
		getService().save(selected);
	}
	
	
	@NotifyChange({"selected","orders"})
	@Command
	public void deleteOrder(){
		getService().delete(selected);//delete selected
		getOrders().remove(selected);
		selected = null; //clean the selected
	}

	public OrderService getService() {
		return FakeOrderService.getInstance();
	}
	
	//validators for prompt
	public Validator getPriceValidator(){
		return new AbstractValidator(){
			public void validate(ValidationContext ctx) {
				Double price = (Double)ctx.getProperty().getValue();
				if(price==null || price<=0){
					addInvalidMessage(ctx, "must large than 0");
				}
			}
		};
	}
	
	public Validator getQuantityValidator(){
		return new AbstractValidator(){
			public void validate(ValidationContext ctx) {
				Integer quantity = (Integer)ctx.getProperty().getValue();
				if(quantity==null || quantity<=0){
					addInvalidMessage(ctx, "must large than 0");
				}
			}
		};
	}
	
	@Command @NotifyChange("message")
	public void allow(){
		message = "button clicked at "+Calendar.getInstance().getTime();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
