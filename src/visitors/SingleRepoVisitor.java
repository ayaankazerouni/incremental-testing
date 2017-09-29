package visitors;

import java.util.Map;

import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.SCMRepository;

import models.Method;

public class SingleRepoVisitor extends SensorDataVisitor {
	
	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		Map<String, Method> visitedMethods = super.getVisitedMethods();
		visitedMethods.entrySet().stream()
			.filter(e -> super.methodFilter(e))
			.sorted((e1, e2) -> e1.getValue().getDateDeclared().compareTo(e2.getValue().getDateDeclared()))
			.forEach(e -> {
				Method m = e.getValue();
				writer.write(
						e.getKey(),
						m.getDateDeclared(),
						m.getDateTestInvoked()
				);
			});
		super.finalize(repo, writer);
	}
}
