package visitors;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.repodriller.domain.Commit;

import helpers.ASTHelper;
import models.Method;

/**
 * Visits all method invocations and method declarations
 * in the project.
 */
public class MethodVisitor extends ASTVisitor {
	
	/**
	 * A map of methods. Each method has properties that are
	 * updated in this persistent structure as visitations take place
	 */
	private Map<String, Method> results;
	
	/**
	 * The Commit being visited
	 */
	private Commit commit;
	
	/**
	 * Is this a test class?
	 */
	private boolean testClass;
	
	/**
	 * The name of the file in the current Modification
	 */
	private String fileName;
	
	public MethodVisitor(Commit commit, Map<String, Method> visitedMethods, String fileName) {
		this.results = visitedMethods;
		this.commit = commit;
		this.fileName = fileName;
		this.testClass = fileName.toLowerCase().contains("test");
	}
	
	public Map<String, Method> getResults() {
		return this.results;
	}
	
	/**
	 * Uniquely identify this method in the entire project,
	 * then set its declaration commit hash if it wasn't already set.
	 * Adds the method to the data structure if it is absent.
	 */
	public boolean visit(MethodDeclaration node) {
		if (node.resolveBinding() != null) {
			String identifier = ASTHelper.getUniqueMethodIdentifier(node.resolveBinding(), this.fileName);
			if (identifier != null) {
				String name = node.getName().getIdentifier();
				if (this.results.containsKey(identifier)) {
					Method method = this.results.get(identifier);
					if (method.getDeclared() == null) {
						method.setDeclared(this.commit);
						this.results.put(identifier, method);
					}
				} else {
					Method method = new Method(name, identifier);
					method.setDeclared(this.commit);
					this.results.put(identifier, method);
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
		if (node.resolveMethodBinding() != null) {
			String identifier = ASTHelper.getUniqueMethodIdentifier(node.resolveMethodBinding(), null);
			if (identifier != null) {
				String name = node.getName().getIdentifier();
				if (this.results.containsKey(identifier)) {
					Method method = this.results.get(identifier);
					if (this.testClass) {
						if (method.getTestInvoked() == null) {
							method.setTestInvoked(this.commit);
							this.results.put(identifier, method);
						}
					}
				} else {
					Method method = new Method(name, identifier);
					if (this.testClass) {
						method.setTestInvoked(this.commit);
					}
					this.results.put(identifier, method);
				}
			}
		}
		return super.visit(node);
	}
}
