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
	
	public int getTestFiles() {
		return testFiles;
	}
	
	public void incrementalTestFiles(int delta) {
		this.testFiles += delta;
	}
	
	public int getProductionFiles() {
		return productionFiles;
	}
	
	public void incrementProductionFiles(int delta) {
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
