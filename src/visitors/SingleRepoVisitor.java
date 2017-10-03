package visitors;

import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;

import helpers.ASTHelper;
import models.Method;

public class SingleRepoVisitor extends SensorDataVisitor {
	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		Map<String, Method> visitedMethods = super.getAndResetVisited();
		this.populateComplexities(repo, visitedMethods);
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
						m.getIdentifier(),
						m.getName(),
						declared,
						invoked,
						m.getCyclomaticComplexity()
				);
			});
	}
	
	private void populateComplexities(SCMRepository repo, Map<String, Method> visitedMethods) {
		try {
			repo.getScm().checkout(repo.getHeadCommit());
			
			for (RepositoryFile file : repo.getScm().files()) {
				if (!file.fileNameEndsWith(".java")) {
					continue;
				}

				ComplexityVisitor visitor = new ComplexityVisitor(visitedMethods);
				ASTParser parser = ASTHelper.createAndSetupParser(file.getFile().getName(), file.getSourceCode(), repo.getPath() + "/");
				CompilationUnit result = (CompilationUnit) parser.createAST(null);
				result.accept(visitor);
			}
		} finally {
			repo.getScm().reset();
		}
	}
}
