package visitors;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.repodriller.domain.Commit;

import helpers.ASTHelper;
import models.Method;

public class MethodVisitor extends ASTVisitor {
	
	private Map<String, Method> results;
	private Commit commit;
	private boolean testClass;
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
	
	public boolean visit(MethodInvocation node) {
		if (node.resolveMethodBinding() != null) {
			String identifier = ASTHelper.getUniqueMethodIdentifier(node.resolveMethodBinding(), this.fileName);
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
					this.results.put(name, method);
				}
			}
		}
		return super.visit(node);
	}
}
