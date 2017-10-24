package visitors;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

import helpers.ASTHelper;
import models.Method;

/**
 * Visits all nodes in the AST that contribute to
 * McCabe's cyclomatic complexity: if, for, while,
 * do, case (but not the switch itself), catch,
 * and boolean literals && and ||
 */
public class ComplexityVisitor extends ASTVisitor {

	private Map<String, Method> results;
	private String fileName;

	public ComplexityVisitor(Map<String, Method> visitedMethods, String fileName) {
		this.results = visitedMethods;
		this.fileName = fileName;
	}

	@Override
	public boolean visit(IfStatement node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		this.visitMcCabeComplex(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		Operator operator = node.getOperator();
		if (operator.equals(InfixExpression.Operator.CONDITIONAL_AND)
				|| operator.equals(InfixExpression.Operator.CONDITIONAL_OR)) {
			this.visitMcCabeComplex(node);
		}
		return super.visit(node);
	}

	private void visitMcCabeComplex(ASTNode node) {
		MethodDeclaration enclosingMethod = this.getEnclosingMethod(node);
		IMethodBinding binding = enclosingMethod.resolveBinding();
		String identifier = ASTHelper.getUniqueMethodIdentifier(binding, this.fileName);
		String name = enclosingMethod.getName().getIdentifier();
		if (identifier != null) {
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
	}

	private MethodDeclaration getEnclosingMethod(ASTNode node) {
		ASTNode parentNode = node.getParent();
		while (parentNode.getNodeType() != ASTNode.METHOD_DECLARATION) {
			parentNode = parentNode.getParent();
		}

		return (MethodDeclaration) parentNode;
	}
}
