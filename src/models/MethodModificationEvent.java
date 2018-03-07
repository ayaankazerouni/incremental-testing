package models;

public class MethodModificationEvent {

	private String methodId;
	private long modTime;
	private String commit;
	private Type type;
	
	public enum Type {
		MODIFY_SELF,
		MODIFY_TESTING_METHOD
	}
	
	public MethodModificationEvent(String methodId, long modTime, String commit, Type type) {
		this.methodId = methodId;
		this.modTime = modTime;
		this.commit = commit;
		this.type = type;
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
