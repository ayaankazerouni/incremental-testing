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
import helpers.DistanceHelper;
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
				!m.getIdentifier().toLowerCase().contains("test");
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
		for (Method method : visitedMethods.values()) {
			if (method.getDeclared() == null || method.getTestInvoked() == null) {
				continue;
			}
			
			long levenshtein = this.getEffortForMethod(repo, method);
			method.setLevenshtein(levenshtein);
		}
	}
	
	private long getEffortForMethod(SCMRepository repo, Method method) {
		Commit declared = method.getDeclared();
		Commit invoked = method.getTestInvoked();
		HashMap<String, String> sourceDeclared = this.getSourceFromRevision(repo, declared);
		HashMap<String, String> sourceInvoked = this.getSourceFromRevision(repo, invoked);
	
		return this.getEffortBetweenRevisions(sourceDeclared, sourceInvoked);
	}
	
	/**
	 * Get a snapshot of the source at the specified commit.
	 * Checks out the commit for computation and then resets the
	 * repository to the default branch after finishing.
	 * 
	 * @param repo		The repository
	 * @param commit	The specified commit
	 * @return A HashMap where each key is a file name and each
	 * 			value is the source of that file at the specified
	 * 			commit.
	 */
	private HashMap<String, String> getSourceFromRevision(SCMRepository repo, Commit commit) {
		HashMap<String, String> source = new HashMap<String, String>();
		try {
			repo.getScm().checkout(commit.getHash());
			repo.getScm().files().stream()
				.filter(f -> f.fileNameEndsWith(".java") && 
							f.getFile().getParentFile().getName().contains("src"))
				.sorted((f1, f2) -> f1.getFile().getName().compareTo(f2.getFile().getName()))
				.forEach(f -> source.put(f.getFullName(), f.getSourceCode()));
			return source;
		} finally {
			repo.getScm().reset();
		}
	}
	
	private long getEffortBetweenRevisions(HashMap<String, String> rev1, HashMap<String, String> rev2) {
		long distance = 0;
		
		// First, put all missing keys into rev1 with null values
		rev2.keySet().stream()
			.filter(k -> !rev1.containsKey(k))
			.forEach(k -> rev1.put(k, null));
		
		// Then, sum up the distance between revisions' versions of each file
		for (Entry<String, String> entry : rev1.entrySet()) {
			String filename = entry.getKey();
			if (entry.getValue() == null) {
				distance += rev2.get(filename).length();
				continue;
			}
			
			String rev1Source = rev1.get(filename);
			String rev2Source = rev2.get(filename);
			distance += DistanceHelper.levenshtein(rev1Source, rev2Source);
		}
		return distance;
	}
	
	@Override
	public abstract void finalize(SCMRepository repo, PersistenceMechanism writer);
}