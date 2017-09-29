package visitors;

import java.util.Collections;
import java.util.Date;
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

public class SensorDataVisitor implements CommitVisitor {
	
	private Map<String, Method> visitedMethods = Collections.synchronizedMap(new HashMap<String, Method>());
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		Date commitTime = commit.getDate().getTime();
		for (Modification m : commit.getModifications()) {
			if (m.fileNameEndsWith(".java")) {
				MethodVisitor methodVisitor = null;
				if (m.getFileName().toLowerCase().contains("test")) {
					methodVisitor = new MethodVisitor(commitTime, this.visitedMethods, true);
				} else {
					methodVisitor = new MethodVisitor(commitTime, this.visitedMethods, false);
				}
				ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setSource(m.getSourceCode().toCharArray());
				CompilationUnit result = (CompilationUnit) parser.createAST(null);
				result.accept(methodVisitor);
				
				this.visitedMethods = methodVisitor.getResults();
			}
		}
	}
	
	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		this.visitedMethods.entrySet().stream()
			.filter(e -> this.filter(e))
			.sorted((e1, e2) -> e1.getValue().getDateDeclared().compareTo(e2.getValue().getDateDeclared()))
			.forEach(e -> {
				Method m = e.getValue();
				writer.write(
						e.getKey(),
						m.getDateDeclared(),
						m.getDateTestInvoked()
				);
			});
		CommitVisitor.super.finalize(repo, writer);
	}
	
	private boolean filter(Entry<String, Method> entry) {
		Method m = entry.getValue();
		return m.getDateDeclared() != null &&
				!m.getName().toLowerCase().contains("test");
	}
}
