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
		this.cyclomaticComplexity = 1;
	}

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
	
	public int getAdditions() {
		return this.additions;
	}
	
	public void setAdditions(int additions) {
		this.additions = additions;
	}

	public int getRemovals() {
		return this.removals;
	}

	public void setRemovals(int removals) {
		this.removals = removals;
	}

	public int getFilesChanged() {
		return this.filesChanged;
	}

	public void setFilesChanged(int filesChanged) {
		this.filesChanged = filesChanged;
	}
	
	public boolean isSolutionMethod() {
		return this.getDeclared() != null &&
				!this.getIdentifier().toLowerCase().contains("test");
	}
	
	public void setMetricsFromModifications(List<Modification> modifications) {
		this.setFilesChanged(modifications.size());
		modifications.stream().forEach(mod -> {
			this.setAdditions(this.getAdditions() + mod.getAdded());
			this.setRemovals(this.getRemovals() + mod.getRemoved());
		});
	}
}
