package visitors;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.SCMRepository;

import models.Method;

public class AggregateRepoVisitor extends SensorDataVisitor {
	
	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		Map<String, Method> visitedMethods = super.getAndResetVisited();
		super.calculateComplexities(repo, visitedMethods);
		super.calculateEffort(repo, visitedMethods);
		Set<Method> processedMethods = visitedMethods.entrySet().stream()
				.filter(e -> super.methodFilter(e))
				.map(e -> e.getValue())
				.collect(Collectors.toSet());
		long methodsNotTested = processedMethods.stream()
				.filter(m -> m.getTestInvoked() == null)
				.count();
		OptionalDouble averageTimeToTest = (OptionalDouble) processedMethods.stream()
				.filter(m -> m.getTestInvoked() != null)
				.mapToDouble(m -> {
					long declared = m.getDeclared().getDate().getTimeInMillis() / 1000;
					long invoked = m.getTestInvoked().getDate().getTimeInMillis() / 1000;
					long seconds = invoked - declared;
					double hours = (seconds / 3600);
					return hours;
				})
				.average();
		int totalCyclomaticComplexity = processedMethods.stream()
				.mapToInt(m -> m.getCyclomaticComplexity())
				.sum();
		OptionalDouble averageLevenshtein = (OptionalDouble) processedMethods.stream()
				.filter(m -> m.getTestInvoked() != null)
				.mapToDouble(m -> m.getLevenshteinDistance())
				.average();
		writer.write(
			repo.getPath(),
			processedMethods.size(),
			methodsNotTested,
			totalCyclomaticComplexity,
			averageTimeToTest.getAsDouble(),
			averageLevenshtein.getAsDouble()
		);
	}
}
