package visitors.ast;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.repodriller.domain.Commit;

import helpers.ASTHelper;
import models.HunkHeader;
import models.MethodModificationEvent;
import models.MethodModificationEvent.Type;

public class MethodModificationVisitor extends ASTVisitor {

	private Commit commit;
	
	private List<MethodModificationEvent> results;
	
	private List<HunkHeader> hunkHeaders;
	
	public MethodModificationVisitor(List<HunkHeader> hunkHeaders, Commit commit, List<MethodModificationEvent> results) {
		this.commit = commit;
		this.hunkHeaders = hunkHeaders;
		this.results = results;
	}
	
	public List<MethodModificationEvent> getResults() {
		return Collections.synchronizedList(this.results);
	}
	
	/**
	 * Uniquely identify this method in the entire project,
	 * then set its declaration commit hash if it wasn't already set.
	 * Adds the method to the data structure if it is absent.
	 */
	public boolean visit(MethodDeclaration node) {
		IMethodBinding binding = node.resolveBinding();
		if (binding != null) {
			String methodId = ASTHelper.getUniqueMethodIdentifier(binding);
			int startLine = ASTHelper.getStartLine(node);
			int endLine = ASTHelper.getEndLine(node);
			
			this.hunkHeaders.stream()
				.forEach(h -> {
					int oldStart = h.getOldStart();
					int oldEnd = oldStart + h.getOldLineCount();
					int newStart = h.getNewStart();
					int newEnd = newStart + h.getNewLineCount();
					
					if (Math.max(startLine, newStart) <= Math.min(endLine, newEnd) || (Math.max(startLine, oldStart) <= Math.min(newEnd, oldEnd))) {
						long time  = commit.getDate().getTimeInMillis();
						String hash = commit.getHash();
						Type type = Type.MODIFY_SELF;
						MethodModificationEvent mod = new MethodModificationEvent(methodId, time, hash, type);
						this.results.add(mod);
					}
				});
		}
		
		return super.visit(node);
	}
	
	public boolean visit(MethodInvocation node) {
		String declaringClassName = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
		if (!(declaringClassName.startsWith("java.") || declaringClassName.startsWith("javax."))) {
			// TODO: Get parent (which should be a test method declaration) and check if modifications took place
			// in the method. If they did, add an event of type MODIFY_TESTING_METHOD for this method
		}
		return super.visit(node);
	}
}
