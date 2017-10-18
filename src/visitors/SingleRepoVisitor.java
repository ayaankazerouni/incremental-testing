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
		visitedMethods.values().stream()
			.filter(m -> m.isSolutionMethod())
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
				writer.write(
						m.getIdentifier(),
						m.getName(),
						declared,
						invoked,
						declaredHash,
						invokedHash,
						m.getCyclomaticComplexity(),
						filesChanged > 0 ? m.getAdditions() : null,
						filesChanged > 0 ? m.getRemovals() : null,
						filesChanged > 0 ? m.getFilesChanged() : null
				);
			});
	}
}
