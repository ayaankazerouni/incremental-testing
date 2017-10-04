package visitors;

import java.util.Calendar;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import helpers.ASTHelper;
import models.Method;

public class MethodVisitor extends ASTVisitor {
	
	private Map<String, Method> results;
	private Calendar date;
	private boolean testClass;
	
	public MethodVisitor(Calendar date, Map<String, Method> visitedMethods, boolean testClass) {
		this.results = visitedMethods;
		this.date = date;
		this.testClass = testClass;
	}
	
	public Map<String, Method> getResults() {
		return this.results;
	}
	
	public boolean visit(MethodDeclaration node) {
		if (node.resolveBinding() != null) {
			String identifier = ASTHelper.getUniqueMethodIdentifier(node.resolveBinding());
			String name = node.getName().getIdentifier();
			if (this.results.containsKey(identifier)) {
				Method method = this.results.get(identifier);
				if (method.getDateDeclared() == null) {
					method.setDateDeclared(this.date);
					this.results.put(identifier, method);
				}
			} else {
				Method method = new Method(name, identifier);
				method.setDateDeclared(this.date);
				this.results.put(identifier, method);
			}
		}
		return super.visit(node);
	}
	
	public boolean visit(MethodInvocation node) {
		if (node.resolveMethodBinding() != null) {
			String identifier = ASTHelper.getUniqueMethodIdentifier(node.resolveMethodBinding());
			String name = node.getName().getIdentifier();
			if (this.results.containsKey(identifier)) {
				Method method = this.results.get(identifier);
				if (this.testClass) {
					if (method.getDateTestInvoked() == null) {
						method.setDateTestInvoked(this.date);
						this.results.put(identifier, method);
					}
				}
			} else {
				Method method = new Method(name, identifier);
				if (this.testClass) {
					method.setDateTestInvoked(this.date);
				}
				this.results.put(name, method);
			}
		}
		return super.visit(node);
	}
}
