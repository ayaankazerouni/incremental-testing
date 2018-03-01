package visitors;

import java.util.Collections;
import java.util.Date;
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
import org.repodriller.scm.SCMRepository;

import helpers.ASTHelper;
import models.Method;

public class SensorDataVisitor implements CommitVisitor {
	
	private Map<String, Method> visitedMethods;
	
	@Override
	public void initialize(SCMRepository repo, PersistenceMechanism writer) {
		this.visitedMethods = Collections.synchronizedMap(new HashMap<String, Method>());
	}
	
	/**
	 * Checkout each commit and build an AST for the entire project,
	 * then calculate method metrics and put them in a persistent
	 * data structure.
	 */
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		try {
			repo.getScm().checkout(commit.getHash());
			for (Modification m : commit.getModifications()) {
				if (m.fileNameEndsWith(".java") && m.getNewPath().contains("src/")) {
					MethodASTVisitor methodVisitor = new MethodASTVisitor(commit, this.visitedMethods, m.getFileName());
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
	
	private void calculateComplexities(SCMRepository repo) {
		repo.getScm().reset();
		repo.getScm().files().stream()
			.filter(f -> f.fileNameEndsWith(".java") && f.getFullName().contains("src/"))
			.forEach(f -> {
				ComplexityASTVisitor visitor = new ComplexityASTVisitor(visitedMethods);
				ASTParser parser = ASTHelper.createAndSetupParser(f.getFile().getName(), f.getSourceCode(), repo.getPath() + "/");
				CompilationUnit result = (CompilationUnit) parser.createAST(null);
				result.accept(visitor);
			});
	}
	
	private void calculateEffort(SCMRepository repo) {
		repo.getScm().reset();
		synchronized (visitedMethods) {
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
					method.markPresent();
					method.setMetricsFromModifications(relevantMods); 
				});
		}
	}

	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		this.calculateComplexities(repo);
		this.calculateEffort(repo);
		synchronized (this.visitedMethods) {
			this.visitedMethods.values().stream()
				.filter(m -> m.isSolutionMethod() && m.isPresentInFinal())
				.sorted((m1, m2) -> m1.getDeclared().getDate().compareTo(m2.getDeclared().getDate()))
				.forEach(m -> {
					Date declared = m.getDeclared().getDate().getTime();
					String declaredHash = m.getDeclared().getHash();
					Date invoked = null;
					String invokedHash = null;
					if (m.getTestInvoked() != null) {
						invoked = m.getTestInvoked().getDate().getTime();
						invokedHash = m.getTestInvoked().getHash();
					}
					int filesChanged = m.getFilesChanged();
					String[] splitPath = repo.getPath().split("/");
					String dirName = splitPath[splitPath.length - 1];
					writer.write(
							dirName,
							m.getIdentifier(),
							m.getName(),
							declared,
							invoked,
							declaredHash,
							invokedHash,
							m.getCyclomaticComplexity(),
							filesChanged > 0 ? m.getSolutionAdditions() : null,
							filesChanged > 0 ? m.getSolutionRemovals() : null,
							filesChanged > 0 ? m.getTestAdditions() : null,
							filesChanged > 0 ? m.getTestRemovals() : null,
							filesChanged > 0 ? m.getSolutionAdditions() + m.getTestAdditions() : null,
							filesChanged > 0 ? m.getSolutionRemovals() + m.getTestRemovals() : null,
							filesChanged > 0 ? m.getFilesChanged() : null
					);
				});
		}
	}
}