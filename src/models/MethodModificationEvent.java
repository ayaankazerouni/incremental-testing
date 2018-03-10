package models;

public class MethodModificationEvent {

	private String methodId;
	private long modTime;
	private String commit;
	private Type type;
	private int added;
	private int removed;
	private int modsToMethod;
	
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
	
	public int getModsToMethod() {
		return modsToMethod;
	}

	public void setModsToMethod(int modsToMethod) {
		this.modsToMethod = modsToMethod;
	}
	
	public int getRemoved() {
		return removed;
	}

	public void setRemoved(int removed) {
		this.removed = removed;
	}

	public int getAdded() {
		return added;
	}

	public void setAdded(int added) {
		this.added = added;
	}

	@Override
	public String toString() {
		return "(" + this.methodId + ") at " + this.modTime + ", " + this.type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		MethodModificationEvent other = (MethodModificationEvent) obj;
		
		if (!this.getMethodId().equals(other.getMethodId())) {
			return false;
		}
		
		if (!this.getType().equals(other.getType())) {
			return false;
		}
		
		if (!this.getCommit().equals(other.getCommit())) {
			return false;
		}
		
		return true;
	}
}
