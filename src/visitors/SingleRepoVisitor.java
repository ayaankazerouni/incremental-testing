package visitors;

import java.util.Date;
import java.util.Map;

import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.SCMRepository;

import models.Method;

public class SingleRepoVisitor extends SensorDataVisitor {
	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		Map<String, Method> visitedMethods = super.getAndResetVisited();
		super.calculateComplexities(repo, visitedMethods);
		super.calculateEffort(repo, visitedMethods);
		visitedMethods.entrySet().stream()
			.filter(e -> super.methodFilter(e))
			.sorted((e1, e2) -> e1.getValue().getDeclared().getDate().compareTo(e2.getValue().getDeclared().getDate()))
			.forEach(e -> {
				Method m = e.getValue();
				Date declared = m.getDeclared().getDate().getTime();
				String declaredHash = m.getDeclared().getHash();
				Date invoked = null;
				String invokedHash = null;
				if (m.getTestInvoked() != null) {
					invoked = m.getTestInvoked().getDate().getTime();
					invokedHash = m.getTestInvoked().getHash();
				}
				long levenshtein = m.getLevenshteinDistance();
				writer.write(
						m.getIdentifier(),
						m.getName(),
						declared,
						invoked,
						declaredHash,
						invokedHash,
						m.getCyclomaticComplexity(),
						levenshtein == -1 ? null : levenshtein
				);
			});
	}
}
