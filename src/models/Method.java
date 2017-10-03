package models;

import java.util.Calendar;

public class Method {

	private Calendar dateDeclared;
	private Calendar dateTestInvoked;
	private String name;
	private String identifier;
	private int cyclomaticComplexity;
	
	public Method(String name, String identifier) {
		this.name = name;
		this.identifier = identifier;
		this.cyclomaticComplexity = 1;
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
	
	public int getCyclomaticComplexity() {
		return this.cyclomaticComplexity;
	}
	
	public void incrementCyclomaticComplexity() {
		this.cyclomaticComplexity++;
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
}
