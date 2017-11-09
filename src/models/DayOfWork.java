package models;

import java.util.ArrayList;

public class DayOfWork {

	private int testFiles;
	private int productionFiles;
	private ArrayList<String> seenFiles;
	
	public DayOfWork() {
		this.seenFiles = new ArrayList<String>();
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
	
	public void addSeenFile(String fileName) {
		this.seenFiles.add(fileName);
	}
	
	public boolean hasSeenFile(String fileName) {
		return this.seenFiles.contains(fileName);
	}
}
