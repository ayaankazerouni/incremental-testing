package visitors.commits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import models.MethodModification;
import visitors.ast.MethodModificationVisitor;

public class MethodModificationCommitVisitor implements CommitVisitor {
	
	private List<MethodModification> methodModifications;
	
	@Override
	public void initialize(SCMRepository repo, PersistenceMechanism writer) {
		this.methodModifications = Collections.synchronizedList(new ArrayList<MethodModification>());
	}
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		try {
			repo.getScm().checkout(commit.getHash());
			for (Modification mod : commit.getModifications()) {
				if (mod.fileNameEndsWith(".java") && mod.getNewPath().contains("src/")) {
					ASTParser parser = ASTHelper.createAndSetupParser(mod.getFileName(), mod.getSourceCode(), repo.getPath() + "/src");
					CompilationUnit result = (CompilationUnit) parser.createAST(null);
					MethodModificationVisitor visitor = new MethodModificationVisitor(this.getHunkHeader(mod.getDiff()), commit);
					result.accept(visitor);
					
					this.methodModifications = visitor.getResults();
				}
			}
		} catch (Exception e) {
			repo.getScm().reset();
		}
	}

	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		repo.getScm().reset();
		synchronized (this.methodModifications) {
			this.methodModifications.stream()
				.forEach(method -> {
					String[] splitPath = repo.getPath().split("/");
					String dirName = splitPath[splitPath.length - 1];
					writer.write(
							dirName,
							method.getMethodId(),
							method.getModificationDate(),
							method.getCommit()
					);
				});
		}
	}
	
	private List<String> getHunkHeader(String diffText) {
		List<String> headers = Arrays.asList(diffText.split("\n")).stream()
			.filter(s -> s.startsWith("@@"))
			.collect(Collectors.toList());
		
		return headers;
	}
}