package visitors.ast;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.core.util.EnclosingMethodAttribute;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;

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
	 * The modification being assessed
	 */
	private Modification modification;
	
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
	
	public MethodModificationASTVisitor(List<HunkHeader> hunkHeaders, Commit commit, Modification modification, Set<MethodModificationEvent> results) {
		this.commit = commit;
		this.hunkHeaders = hunkHeaders;
		this.results = results;
		this.modification = modification;
		this.fileName = modification.getFileName();
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
				.forEach(h -> {
					this.recordMethodModificationEvent(methodId, h, node, Type.MODIFY_SELF);
				});
		}
		
		return super.visit(node);
	}
	
	/**
	 * If this method was invoked in a test, and the test was modified in this commit's
	 * Diff, emit a MODIFY_TESTING_METHOD event for this method.
	 */
	public boolean visit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (this.fileName.toLowerCase().contains("test") && binding != null) {
			String declaringClassName = binding.getDeclaringClass().getQualifiedName();
			if (!(declaringClassName.startsWith("java.") || declaringClassName.startsWith("javax."))) { // naively check that it's not a Java core method
				String methodId = ASTHelper.getUniqueMethodIdentifier(node.resolveMethodBinding());
				MethodDeclaration enclosingMethod = ASTHelper.getEnclosingMethod(node);
				if (enclosingMethod.getName().getIdentifier().startsWith("test")) {
					this.hunkHeaders.stream()
						.forEach(h -> {
							this.recordMethodModificationEvent(methodId, h, enclosingMethod, Type.MODIFY_TESTING_METHOD);
						});
				}
			}
		}
		
		return super.visit(node);
	}
	
	private void recordMethodModificationEvent(String methodId, HunkHeader header, MethodDeclaration method, Type type) {
		int modSize = this.getMethodMods(header, method);
		if (modSize > 0) {
			long time = commit.getDate().getTimeInMillis();
			String hash = commit.getHash();
			MethodModificationEvent mod = new MethodModificationEvent(methodId, time, hash, type);
			mod.setModsToMethod(modSize);
			mod.setAdded(this.modification.getAdded());
			mod.setRemoved(this.modification.getRemoved());
			
			if (type == Type.MODIFY_TESTING_METHOD) {
				String testMethodId = ASTHelper.getUniqueMethodIdentifier(method.resolveBinding());
				mod.setTestMethodId(testMethodId);
			}
			
			this.results.add(mod);
		}
	}
	
	private int getMethodMods(HunkHeader header, MethodDeclaration method) {
		int startLine = ASTHelper.getStartLine(method);
		int endLine = ASTHelper.getEndLine(method);
		int hunkStart = header.getNewStart();
		int hunkEnd = hunkStart + header.getNewLineCount();
		
		int start = Math.max(startLine, hunkStart);
		int end = Math.min(endLine, hunkEnd);

		return end - start;
	}
}
