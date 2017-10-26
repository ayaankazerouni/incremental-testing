package models;

import java.util.List;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;

public class Method {

	private Commit declared;
	private Commit testInvoked;
	private String name;
	private String identifier;
	private int additions;
	private int removals;
	private int filesChanged;
	private int cyclomaticComplexity;
	
	public Method(String name, String identifier) {
		this.name = name;
		this.identifier = identifier;
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
		if (this.cyclomaticComplexity == 0) {
			this.cyclomaticComplexity += 2;
		} else {
			this.cyclomaticComplexity++;
		}
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
	
	/**
	 * Gets the number of additions between when this Method
	 * was declared and when this Method was invoked in a test
	 * @return additions
	 */
	public int getAdditions() {
		return this.additions;
	}
	
	private void setAdditions(int additions) {
		this.additions = additions;
	}

	/**
	 * Gets the number of removals between when this Method
	 * was declared and when this Method was invoked in a test
	 * @return removals
	 */
	public int getRemovals() {
		return this.removals;
	}

	private void setRemovals(int removals) {
		this.removals = removals;
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
			this.setAdditions(this.getAdditions() + mod.getAdded());
			this.setRemovals(this.getRemovals() + mod.getRemoved());
		});
	}
}
