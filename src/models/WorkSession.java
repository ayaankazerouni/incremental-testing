package models;

import java.util.Calendar;

public class WorkSession {

	private int testFiles;
	private int productionFiles;
	private Calendar startTime;
	private Calendar endTime;
	
	public WorkSession(Calendar startTime) {
		this.startTime = startTime;
		this.endTime = null;
	}
	
	public int getTestChanges() {
		return testFiles;
	}
	
	public void incrementTestChanges(int delta) {
		this.testFiles += delta;
	}
	
	public int getSolutionChanges() {
		return productionFiles;
	}
	
	public void incrementSolutionChanges(int delta) {
		this.productionFiles += delta;
	}
	
	public Calendar getStartTime() {
		return this.startTime;
	}
	
	public Calendar getEndTime() {
		return this.endTime;
	}
	
	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}
}
