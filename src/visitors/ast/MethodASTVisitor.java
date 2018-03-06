package visitors.ast;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.repodriller.domain.Commit;

import helpers.ASTHelper;
import models.MethodTimeToTestInvoke;

/**
 * Visits all method invocations and method declarations
 * in the project.
 */
public class MethodASTVisitor extends ASTVisitor {
	
	/**
	 * A map of methods. Each method has properties that are
	 * updated in this persistent structure as visitations take place
	 */
	private Map<String, MethodTimeToTestInvoke> results;
	
	/**
	 * The Commit being visited
	 */
	private Commit commit;
	
	/**
	 * Is this a test class?
	 */
	private boolean testClass;
	
	
	public MethodASTVisitor(Commit commit, Map<String, MethodTimeToTestInvoke> visitedMethods, String fileName) {
		this.results = visitedMethods;
		this.commit = commit;
		this.testClass = fileName.toLowerCase().contains("test");
	}
	
	public Map<String, MethodTimeToTestInvoke> getResults() {
		return Collections.synchronizedMap(this.results);
	}
	
	/**
	 * Uniquely identify this method in the entire project,
	 * then set its declaration commit hash if it wasn't already set.
	 * Adds the method to the data structure if it is absent.
	 */
	public boolean visit(MethodDeclaration node) {
		IMethodBinding binding = node.resolveBinding();
		if (binding != null && ASTHelper.methodIsNotPrivate(binding)) {
			String identifier = ASTHelper.getUniqueMethodIdentifier(binding);
			if (identifier != null) {
				synchronized (this.results) {
					if (this.results.containsKey(identifier)) {
						MethodTimeToTestInvoke method = this.results.get(identifier);
						Commit declared = method.getDeclared();
						if (declared == null || this.commit.getDate().before(declared.getDate())) {
							method.setDeclared(this.commit);
							this.results.put(identifier, method);
						}
					} else {
						String name = node.getName().getIdentifier();
						MethodTimeToTestInvoke method = new MethodTimeToTestInvoke(name, identifier);
						method.setDeclared(this.commit);
						this.results.put(identifier, method);
					}
				}
			}
		}
		return super.visit(node);
	}
	
	/**
	 * Uniquely identify this method in the entire project,
	 * then set its invocation commit if it wasn't already set,
	 * && this is a test class.
	 * Adds the method to the data structure if it is absent.
	 */
	public boolean visit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding != null && ASTHelper.methodIsNotPrivate(binding)) {
			String identifier = ASTHelper.getUniqueMethodIdentifier(binding);
			if (identifier != null) {
				synchronized (this.results) {
					if (this.results.containsKey(identifier)) {
						MethodTimeToTestInvoke method = this.results.get(identifier);
						if (this.testClass) {
							Commit testInvoked = method.getTestInvoked();
							if (testInvoked == null || this.commit.getDate().before(testInvoked.getDate())) {
								method.setTestInvoked(this.commit);
								this.results.put(identifier, method);
							}
						}
					} else {
						String name = node.getName().getIdentifier();
						MethodTimeToTestInvoke method = new MethodTimeToTestInvoke(name, identifier);
						if (this.testClass) {
							method.setTestInvoked(this.commit);
						}
						this.results.put(identifier, method);
					}
				}
			}
		}
		return super.visit(node);
	}
}
