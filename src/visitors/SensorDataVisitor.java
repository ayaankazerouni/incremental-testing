package visitors;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	protected Map<String, Method> getAndResetVisited() {
		Map<String, Method> toReturn = new HashMap<String, Method>(this.visitedMethods);
		this.visitedMethods = new HashMap<String, Method>();
		return toReturn;
	}
	
	protected void calculateComplexities(SCMRepository repo, Map<String, Method> visitedMethods) {
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
	
	protected void calculateEffort(SCMRepository repo, Map<String, Method> visitedMethods) {
		repo.getScm().reset();
		visitedMethods.values().stream()
			.filter(method -> method.getDeclared() != null && method.getTestInvoked() != null)
			.forEach(method -> {
				Commit declared = method.getDeclared();
				Commit testInvoked = method.getTestInvoked();
				List<Modification> modifications = null;
				if (declared.getDate().compareTo(testInvoked.getDate()) <= 0) {
					modifications = repo.getScm().getDiffBetweenCommits(declared.getHash(), testInvoked.getHash());
				} else {
					modifications = repo.getScm().getDiffBetweenCommits(testInvoked.getHash(), declared.getHash());
				}
				List<Modification> relevantMods = modifications.stream()
					.filter(mod -> mod.fileNameEndsWith(".java") && mod.getNewPath().contains("src/"))
					.collect(Collectors.toList());
				method.setMetricsFromModifications(relevantMods); 
			});
	}
	
	@Override
	public abstract void finalize(SCMRepository repo, PersistenceMechanism writer);
}