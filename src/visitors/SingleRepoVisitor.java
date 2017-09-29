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
		writer.write(visitedMethods.size());
		visitedMethods.entrySet().stream()
			.filter(e -> super.methodFilter(e))
			.sorted((e1, e2) -> e1.getValue().getDateDeclared().compareTo(e2.getValue().getDateDeclared()))
			.forEach(e -> {
				Method m = e.getValue();
				Date declared = m.getDateDeclared().getTime();
				Date invoked = null;
				if (m.getDateTestInvoked() != null) {
					invoked = m.getDateTestInvoked().getTime();
				}
				writer.write(
						m.getName(),
						declared,
						invoked
				);
			});
	}
}
