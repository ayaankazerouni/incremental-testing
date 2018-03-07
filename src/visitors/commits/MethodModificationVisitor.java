package visitors.commits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import helpers.ASTHelper;
import models.HunkHeader;
import models.MethodModificationEvent;
import visitors.ast.MethodModificationVisitor;

public class MethodModificationCommitVisitor implements CommitVisitor {
	
	private List<MethodModificationEvent> methodModifications;
	
	@Override
	public void initialize(SCMRepository repo, PersistenceMechanism writer) {
		this.methodModifications = Collections.synchronizedList(new ArrayList<MethodModificationEvent>());
	}
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		try {
			repo.getScm().checkout(commit.getHash());
			for (Modification mod : commit.getModifications()) {
				if (mod.fileNameEndsWith(".java") && mod.getNewPath().contains("src/")) {
					List<HunkHeader> hunkHeaders = this.getHunkHeaders(mod.getDiff());
					ASTParser parser = ASTHelper.createAndSetupParser(mod.getFileName(), mod.getSourceCode(), repo.getPath() + "/src");
					CompilationUnit result = (CompilationUnit) parser.createAST(null);
					MethodModificationVisitor visitor = new MethodModificationVisitor(hunkHeaders, commit, this.methodModifications);
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
	
	private List<HunkHeader> getHunkHeaders(String diffText) {
		List<HunkHeader> headers = new ArrayList<HunkHeader>();
		for (String line : diffText.split("\n")) {
			if (line.startsWith("@@")) {
				line = line.replaceAll("\\D+", " ").trim();
				String[] lineInfo = line.split("\\s+");
				int oldStart = Integer.parseInt(lineInfo[0]);
				int oldLineCount = 0;
				int newStart = 0;
				int newLineCount = 0;
				if (lineInfo.length == 4) {
					oldLineCount = Integer.parseInt(lineInfo[1]);
					newStart = Integer.parseInt(lineInfo[2]);
					newLineCount = Integer.parseInt(lineInfo[3]);
				} else {
					newStart = Integer.parseInt(lineInfo[1]);
				}
				
				headers.add(new HunkHeader(oldStart, oldLineCount, newStart, newLineCount));
			}
		}
		return headers;
	}
}