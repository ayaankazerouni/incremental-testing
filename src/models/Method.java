package models;

import java.util.Calendar;

public class Method {

	private Calendar dateDeclared;
	private Calendar dateTestInvoked;
	private String name;
	
	public Method(String name) {
		this.name = name;
	}

	public Calendar getDateTestInvoked() {
		return this.dateTestInvoked;
	}

	public void setDateTestInvoked(Calendar dateTestInvoked) {
		this.dateTestInvoked = dateTestInvoked;
	}

	public Calendar getDateDeclared() {
		return this.dateDeclared;
	}
	
	public void setDateDeclared(Calendar dateDeclared) {
		this.dateDeclared = dateDeclared;
	}

	public String getName() {
		return name;
	}
}
