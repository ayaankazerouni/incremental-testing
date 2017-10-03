package visitors;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import helpers.ASTHelper;
import models.Method;

public class ComplexityVisitor extends ASTVisitor {
	
	private Map<String, Method> results;
	
	public ComplexityVisitor(Map<String, Method> visitedMethods) {
		this.results = visitedMethods;
	}
	
	public boolean visit(Statement node) {
		if (this.isMcCabeComplex(node)) {
			this.visitMcCabeComplex(node);
		}
		return true;
	}
	
	public boolean visit(CatchClause node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}
	
	public boolean visit(BooleanLiteral node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}
	
	private void visitMcCabeComplex(ASTNode node) {
		MethodDeclaration enclosingMethod = this.getEnclosingMethod(node);
		String identifier = ASTHelper.getUniqueMethodIdentifier(enclosingMethod.resolveBinding());
		String name = enclosingMethod.getName().getIdentifier();
		if (this.results.containsKey(identifier)) {
			Method method = this.results.get(identifier);
			method.incrementCyclomaticComplexity();
			this.results.put(identifier, method);
		} else {
			Method method = new Method(name, identifier);
			method.incrementCyclomaticComplexity();
			this.results.put(identifier, method);
		}
	}
	
	private boolean isMcCabeComplex(Statement node) {
		int type = node.getNodeType();
		return type == ASTNode.IF_STATEMENT ||
				type == ASTNode.FOR_STATEMENT ||
				type == ASTNode.ENHANCED_FOR_STATEMENT ||
				type == ASTNode.WHILE_STATEMENT ||
				type == ASTNode.DO_STATEMENT ||
				type == ASTNode.SWITCH_CASE ||
				type == ASTNode.CATCH_CLAUSE ||
				type == ASTNode.BOOLEAN_LITERAL ||
				type == ASTNode.CONDITIONAL_EXPRESSION;
	}
	
	private MethodDeclaration getEnclosingMethod(ASTNode node) {
		ASTNode parentNode = node.getParent();
		while (parentNode.getNodeType() != ASTNode.METHOD_DECLARATION) {
			parentNode = parentNode.getParent();
		}
		
		return (MethodDeclaration) parentNode;
	}
}
