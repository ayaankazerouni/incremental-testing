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
		super.populateComplexities(repo, visitedMethods);
		visitedMethods.entrySet().stream()
			.filter(e -> super.methodFilter(e))
			.sorted((e1, e2) -> e1.getValue().getDeclared().getDate().compareTo(e2.getValue().getDeclared().getDate()))
			.forEach(e -> {
				Method m = e.getValue();
				Date declared = m.getDeclared().getDate().getTime();
				Date invoked = null;
				if (m.getTestInvoked() != null) {
					invoked = m.getTestInvoked().getDate().getTime();
				}
				writer.write(
						m.getIdentifier(),
						m.getName(),
						declared,
						invoked,
						m.getCyclomaticComplexity()
				);
			});
	}
}
