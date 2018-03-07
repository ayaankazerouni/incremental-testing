package models;

public class HunkHeader {
	private int oldStart;
	private int oldLineCount;
	private int newStart;
	private int newLineCount;
	
	public HunkHeader(int oldStart, int oldEnd, int newStart, int newEnd) {
		this.oldStart = oldStart;
		this.oldLineCount = oldEnd;
		this.newStart = newStart;
		this.newLineCount = newEnd;
	}

	public int getOldStart() {
		return oldStart;
	}

	public void setOldStart(int oldStart) {
		this.oldStart = oldStart;
	}

	public int getOldLineCount() {
		return oldLineCount;
	}

	public void setOldLineCount(int oldEnd) {
		this.oldLineCount = oldEnd;
	}

	public int getNewStart() {
		return newStart;
	}

	public void setNewStart(int newStart) {
		this.newStart = newStart;
	}

	public int getNewLineCount() {
		return newLineCount;
	}

	public void setNewLineCount(int newEnd) {
		this.newLineCount = newEnd;
	}
}