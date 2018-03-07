package visitors.ast;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.repodriller.domain.Commit;

import helpers.ASTHelper;
import models.HunkHeader;
import models.MethodModificationEvent;
import models.MethodModificationEvent.Type;

public class MethodModificationASTVisitor extends ASTVisitor {

	/**
	 * The current commit being visited.
	 */
	private Commit commit;
	
	/**
	 * A list of method modification events that will be emitted from this study.
	 */
	private Set<MethodModificationEvent> results;
	
	/**
	 * The list of Git hunk headers from this commit
	 */
	private List<HunkHeader> hunkHeaders;
	
	/**
	 * The file being examined
	 */
	private String fileName;
	
	public MethodModificationASTVisitor(List<HunkHeader> hunkHeaders, Commit commit, Set<MethodModificationEvent> results, String fileName) {
		this.commit = commit;
		this.hunkHeaders = hunkHeaders;
		this.results = results;
		this.fileName = fileName;
	}
	
	public Set<MethodModificationEvent> getResults() {
		return Collections.synchronizedSet(this.results);
	}
	
	/**
	 * If this method was modified in this commit's Diff, emit a MODIFY_SELF event
	 * for the method.
	 */
	public boolean visit(MethodDeclaration node) {
		IMethodBinding binding = node.resolveBinding();
		if (!fileName.toLowerCase().contains("test") && binding != null) {
			String methodId = ASTHelper.getUniqueMethodIdentifier(binding);
			this.hunkHeaders.stream()
				.filter(h -> this.hunkTouchedMethod(h, node))
				.forEach(h -> {
					long time  = commit.getDate().getTimeInMillis();
					String hash = commit.getHash();
					Type type = Type.MODIFY_SELF;
					MethodModificationEvent mod = new MethodModificationEvent(methodId, time, hash, type);
					this.results.add(mod);
				});
		}
		
		return super.visit(node);
	}
	
	/**
	 * If this method was invoked in a test, and the test was modified in this commit's
	 * Diff, emit a MODIFY_TESTING_METHOD event for this method.
	 */
	public boolean visit(MethodInvocation node) {
		if (this.fileName.toLowerCase().contains("test")) {
			String declaringClassName = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
			if (!(declaringClassName.startsWith("java.") || declaringClassName.startsWith("javax."))) { // naively check that it's not a Java core method
				String methodId = ASTHelper.getUniqueMethodIdentifier(node.resolveMethodBinding());
				MethodDeclaration enclosingMethod = ASTHelper.getEnclosingMethod(node);
				if (enclosingMethod.getName().getIdentifier().startsWith("test")) {
					this.hunkHeaders.stream()
						.filter(h -> this.hunkTouchedMethod(h, enclosingMethod)) // the enclosing (test) method was modified in this commit
						.forEach(h -> {
							long time = commit.getDate().getTimeInMillis();
							String hash = commit.getHash();
							Type type = Type.MODIFY_TESTING_METHOD;
							MethodModificationEvent mod = new MethodModificationEvent(methodId, time, hash, type);
							this.results.add(mod);
						});
				}
			}
		}
		
		return super.visit(node);
	}
	
	private boolean hunkTouchedMethod(HunkHeader header, MethodDeclaration method) {
		int startLine = ASTHelper.getStartLine(method);
		int endLine = ASTHelper.getEndLine(method);
		int oldStart = header.getOldStart();
		int oldEnd = oldStart + header.getOldLineCount();
		int newStart = header.getNewStart();
		int newEnd = newStart + header.getNewLineCount();
		
		return Math.max(startLine, newStart) <= Math.min(endLine, newEnd) || Math.max(startLine, oldStart) <= Math.min(newEnd, oldEnd);
	}
}
