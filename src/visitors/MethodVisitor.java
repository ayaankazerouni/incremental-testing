package visitors;

import java.util.Calendar;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
		String name = node.getName().getIdentifier();
		Method method = new Method(name);
		method.setDateDeclared(this.date);
		this.results.putIfAbsent(method.getName(), method);
		return super.visit(node);
	}
	
	public boolean visit(MethodInvocation node) {
		String name = node.getName().getIdentifier();
		if (this.results.containsKey(name)) {
			Method method = this.results.get(name);
			if (this.testClass) {
				if (method.getDateTestInvoked() == null) {
					method.setDateTestInvoked(this.date);
					this.results.put(name, method);
				}
			}
		} else {
			Method method = new Method(name);
			if (this.testClass) {
				method.setDateTestInvoked(this.date);
			}
			this.results.put(name, method);
		}
		return super.visit(node);
	}
}
