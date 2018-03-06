package visitors.ast;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.repodriller.domain.Commit;

import helpers.ASTHelper;
import models.MethodModification;

public class MethodModificationVisitor extends ASTVisitor {

	private Commit commit;
	
	private List<MethodModification> results;
	
	private List<String> hunkHeaders;
	
	public MethodModificationVisitor(List<String> hunkHeaders, Commit commit) {
		this.commit = commit;
		this.hunkHeaders = hunkHeaders;
	}
	
	public List<MethodModification> getResults() {
		return Collections.synchronizedList(this.results);
	}
	
	/**
	 * Uniquely identify this method in the entire project,
	 * then set its declaration commit hash if it wasn't already set.
	 * Adds the method to the data structure if it is absent.
	 */
	public boolean visit(MethodDeclaration node) {
		String methodId = ASTHelper.getUniqueMethodIdentifier(node.resolveBinding());
		int startLine = ASTHelper.getStartLine(node);
		int endLine = ASTHelper.getEndLine(node);
		
		this.hunkHeaders.stream()
			.forEach(h -> {
				h = h.replaceAll("\\D+", " ").trim();
				String[] lines = h.split("\\s+");
				int oldStart = Integer.parseInt(lines[0]);
				int oldEnd = Integer.parseInt(lines[1]);
				int newStart = Integer.parseInt(lines[2]);
				int newEnd = newStart + Integer.parseInt(lines[3]);
				
				if (Math.max(startLine, newStart) <= Math.min(endLine, newEnd) || (Math.max(startLine, oldStart) <= Math.min(newEnd, oldEnd))) {
					MethodModification mod = new MethodModification(methodId, commit.getDate().getTimeInMillis(), commit.getHash());
					this.results.add(mod);
				}
			});
		
		return super.visit(node);
	}
}
