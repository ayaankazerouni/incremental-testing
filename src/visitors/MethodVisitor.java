package visitors;

import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import models.Method;

public class MethodVisitor extends ASTVisitor {
	
	private Map<String, Method> results;
	private Date date;
	private boolean testClass;
	
	public MethodVisitor(Date date, Map<String, Method> visitedMethods, boolean testClass) {
		this.results = visitedMethods;
		this.date = date;
		this.testClass = testClass;
	}
	
	public Map<String, Method> getResults() {
		return this.results;
	}
	
	public boolean visit(MethodDeclaration node) {
		if (!this.testClass) {
			String name = node.getName().getIdentifier();
			Method method = new Method(name);
			method.setDateDeclared(this.date);
			this.results.putIfAbsent(method.getName(), method);
		}
		System.out.println(node.getName() + " " + this.date);
		return super.visit(node);
	}
	
	public boolean visit(MethodInvocation node) {
		String name = node.getName().getIdentifier();
		if (this.results.containsKey(name)) {
			Method method = this.results.get(name);
			method.incrementCallCount();
			if (this.testClass && !name.toLowerCase().contains("assert")) {
				method.incrementTestCallCount();
				if (method.getDateTestInvoked() == null) {
					method.setDateTestInvoked(this.date);
				}
			}
		} else {
			Method method = new Method(name);
			method.incrementCallCount();
			if (this.testClass && !name.toLowerCase().contains("assert")) {
				method.setDateTestInvoked(this.date);
			}
			this.results.put(name, method);
		}
		return super.visit(node);
	}
}
