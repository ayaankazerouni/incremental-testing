package visitors;

import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.SCMRepository;

import models.Method;

public class AggregateRepoVisitor extends SensorDataVisitor {
	
	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		Set<Method> processedMethods = super.getAndResetVisited().entrySet().stream()
				.filter(e -> super.methodFilter(e))
				.map(e -> e.getValue())
				.collect(Collectors.toSet());
		long methodsNotTested = processedMethods.stream()
				.filter(m -> m.getDateTestInvoked() == null)
				.count();
		OptionalDouble averageTimeToTest = (OptionalDouble) processedMethods.stream()
				.filter(m -> m.getDateDeclared() != null && m.getDateTestInvoked() != null)
				.mapToDouble(m -> {
					long declared = m.getDateDeclared().getTimeInMillis() / 1000;
					long invoked = m.getDateTestInvoked().getTimeInMillis() / 1000;
					long seconds = invoked - declared;
					double hours = (seconds / 3600);
					return hours;
				})
				.average();
		writer.write(
			repo.getPath(),
			processedMethods.size(),
			methodsNotTested,
			averageTimeToTest.getAsDouble()
		);
	}
}
