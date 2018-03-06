package models;

public class MethodModification {

	private String methodId;
	private long modTime;
	private String commit;
	
	public MethodModification(String methodId, long modTime, String commit) {
		this.methodId = methodId;
		this.modTime = modTime;
		this.setCommit(commit);
	}

	public String getMethodId() {
		return methodId;
	}
	
	public void setMethodId(String methodId) {
		this.methodId = methodId;
	}

	public long getModificationDate() {
		return modTime;
	}

	public void setModificationDate(long modificationDate) {
		this.modTime = modificationDate;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}
	
	
}
