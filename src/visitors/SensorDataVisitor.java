package visitors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;

import helpers.ASTHelper;
import models.Method;

public abstract class SensorDataVisitor implements CommitVisitor {

private Map<String, Method> visitedMethods = Collections.synchronizedMap(new HashMap<String, Method>());
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		try {
			repo.getScm().checkout(commit.getHash());
			for (Modification m : commit.getModifications()) {
				if (m.fileNameEndsWith(".java") && m.getNewPath().contains("src/")) {
					MethodVisitor methodVisitor = new MethodVisitor(commit, this.visitedMethods, m.getFileName());
					ASTParser parser = ASTHelper.createAndSetupParser(m.getFileName(), m.getSourceCode(), repo.getPath() + "/src");
					CompilationUnit result = (CompilationUnit) parser.createAST(null);
					result.accept(methodVisitor);
					
					this.visitedMethods = methodVisitor.getResults();
				}
			}
		} catch (Exception e) {
			repo.getScm().reset();
		}
	}
	
	protected boolean methodFilter(Entry<String, Method> entry) {
		Method m = entry.getValue();
		return m.getDeclared() != null &&
				!m.getName().toLowerCase().contains("test");
	}
	
	protected Map<String, Method> getAndResetVisited() {
		Map<String, Method> toReturn = new HashMap<String, Method>(this.visitedMethods);
		this.visitedMethods = new HashMap<String, Method>();
		return toReturn;
	}
	
	protected void populateComplexities(SCMRepository repo, Map<String, Method> visitedMethods) {
		repo.getScm().reset();
		for (RepositoryFile file : repo.getScm().files()) {
			if (!file.fileNameEndsWith(".java") || !file.getFile().getParentFile().getName().equals("src")) {
				continue;
			}

			ComplexityVisitor visitor = new ComplexityVisitor(visitedMethods, file.getFile().getName());
			ASTParser parser = ASTHelper.createAndSetupParser(file.getFile().getName(), file.getSourceCode(), repo.getPath() + "/");
			CompilationUnit result = (CompilationUnit) parser.createAST(null);
			result.accept(visitor);
		}
	}
	
	@Override
	public abstract void finalize(SCMRepository repo, PersistenceMechanism writer);
}