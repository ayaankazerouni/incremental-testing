package models;

import java.util.List;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;

public class MethodTimeToTestInvoke {

	private Commit declared;
	private Commit testInvoked;
	private String name;
	private String identifier;
	private int solultionAdditions;
	private int solutionRemovals;
	private int testAdditions;
	private int testRemovals;
	private int filesChanged;
	private int cyclomaticComplexity;
	private boolean presentInFinal;
	
	public MethodTimeToTestInvoke(String name, String identifier) {
		this.name = name;
		this.identifier = identifier;
		this.cyclomaticComplexity = 1;
	}

	/**
	 * Get the commit in which this Method was first invoked
	 * in a test
	 * @return a Commit instance
	 */
	public Commit getTestInvoked() {
		return this.testInvoked;
	}

	public void setTestInvoked(Commit testInvoked) {
		this.testInvoked = testInvoked;
	}

	public Commit getDeclared() {
		return this.declared;
	}
	
	public void setDeclared(Commit declared) {
		this.declared = declared;
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
	
	/**
	 * Gets the number of additions to solution code between when this Method
	 * was declared and when this Method was invoked in a test
	 * @return additions
	 */
	public int getSolutionAdditions() {
		return this.solultionAdditions;
	}
	
	private void setSolutionAdditions(int additions) {
		this.solultionAdditions = additions;
	}
	
	/**
	 * Gets the number of additions to test code between when this Method
	 * was declared and when this Method was invoked in a test
	 * @return additions
	 */
	public int getTestAdditions() {
		return this.testAdditions;
	}
	
	private void setTestAdditions(int additions) {
		this.testAdditions = additions;
	}

	/**
	 * Gets the number of removals from solution code between when this Method
	 * was declared and when this Method was invoked in a test
	 * @return removals
	 */
	public int getSolutionRemovals() {
		return this.solutionRemovals;
	}

	private void setSolutionRemovals(int removals) {
		this.solutionRemovals = removals;
	}

	/**
	 * Gets the number of removals from test code between when this Method
	 * was declared and when this Method was invoked in a test
	 * @return removals
	 */
	public int getTestRemovals() {
		return this.testRemovals;
	}

	private void setTestRemovals(int removals) {
		this.testRemovals = removals;
	}
	
	public void markPresent() {
		this.presentInFinal = true;
	}
	
	public boolean isPresentInFinal() {
		return this.presentInFinal;
	}
	
	/**	
	 * Gets the number of files changed between when this Method
	 * was declared and when this Method was invoked in a test
	 * @return filesChanged
	 */
	public int getFilesChanged() {
		return this.filesChanged;
	}

	private void setFilesChanged(int filesChanged) {
		this.filesChanged = filesChanged;
	}
	
	/**
	 * Check if this method is a part of the software solution or not.
	 * A method is a solution method if it was declared in this project, and
	 * if it is not a test method.
	 * 
	 * @return true if solution method, false otherwise
	 */
	public boolean isSolutionMethod() {
		return this.getDeclared() != null &&
				!this.getIdentifier().toLowerCase().contains("test");
	}
	
	/**
	 * Given a list of modifications, set metrics for this Method
	 * @param modifications
	 */
	public void setMetricsFromModifications(List<Modification> modifications) {
		this.setFilesChanged(modifications.size());
		modifications.stream().forEach(mod -> {
			if (mod.getFileName().toLowerCase().contains("test")) {
				this.setTestAdditions(this.getTestAdditions() + mod.getAdded());
				this.setTestRemovals(this.getTestRemovals() + mod.getRemoved());
			} else {
				this.setSolutionAdditions(this.getSolutionAdditions() + mod.getAdded());
				this.setSolutionRemovals(this.getSolutionRemovals() + mod.getRemoved());
			}
		});
	}
}
