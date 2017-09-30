package visitors;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import models.Method;

public abstract class SensorDataVisitor implements CommitVisitor {

private Map<String, Method> visitedMethods = Collections.synchronizedMap(new HashMap<String, Method>());
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		Calendar commitTime = commit.getDate();
		for (Modification m : commit.getModifications()) {
			if (m.fileNameEndsWith(".java")) {
				boolean isTest = m.getFileName().toLowerCase().contains("test");
				MethodVisitor methodVisitor = new MethodVisitor(commitTime, this.visitedMethods, isTest);
				ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setSource(m.getSourceCode().toCharArray());
				CompilationUnit result = (CompilationUnit) parser.createAST(null);
				result.accept(methodVisitor);
				
				this.visitedMethods = methodVisitor.getResults();
			}
		}
	}
	
	public boolean methodFilter(Entry<String, Method> entry) {
		Method m = entry.getValue();
		return m.getDateDeclared() != null &&
				!m.getName().toLowerCase().contains("test");
	}
	
	public Map<String, Method> getAndResetVisited() {
		Map<String, Method> toReturn = new HashMap<String, Method>(this.visitedMethods);
		this.visitedMethods = new HashMap<String, Method>();
		return toReturn;
	}
	
	@Override
	public abstract void finalize(SCMRepository repo, PersistenceMechanism writer);
}
