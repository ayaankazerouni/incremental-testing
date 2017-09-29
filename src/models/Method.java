package models;

import java.util.Date;

public class Method {

	private Date dateDeclared;
	private Date dateTestInvoked;
	private String name;
	private int testCallCount;
	private int callCount;
	
	public Method(String name) {
		this.name = name;
	}

	public Date getDateTestInvoked() {
		return this.dateTestInvoked;
	}

	public void setDateTestInvoked(Date dateTestInvoked) {
		this.dateTestInvoked = dateTestInvoked;
	}

	public Date getDateDeclared() {
		return this.dateDeclared;
	}
	
	public void setDateDeclared(Date dateDeclared) {
		this.dateDeclared = dateDeclared;
	}

	public String getName() {
		return name;
	}

	public int getTestCallCount() {
		return testCallCount;
	}

	public void incrementTestCallCount() {
		this.testCallCount++;
	}
	
	public int getCallCount() {
		return callCount;
	}
	
	public void incrementCallCount() {
		this.callCount++;
	}
	
	// TODO: Reliable hashing function for methods
}
