package org.zkoss.zkspringessentials.beans;

import org.zkoss.bind.annotation.DependsOn;

public class Person {
	private int id;
	private String firstName = "";
	private String lastName = "";

	public Person(int id, String firstName, String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	@DependsOn({"firstName", "lastName"})
	public String getFullName() {
		return firstName + " " + lastName;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Person{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				'}';
	}
}