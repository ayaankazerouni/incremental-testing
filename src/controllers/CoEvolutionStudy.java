package controllers;

import java.nio.file.Paths;
import java.util.Arrays;

import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyInBranches;
import org.repodriller.filter.commit.OnlyModificationsWithFileTypes;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import visitors.commits.CoEvolutionVisitor;

public class CoEvolutionStudy implements Study {

	private String infile;
	private String outfile;
	private boolean single;
	
	public CoEvolutionStudy(String infile, String outfile, boolean single) {
		this.infile = infile;
		this.outfile = outfile;
		this.single = single;
	}
	
	@Override
	public void execute() {
		String[] header = new String[] { "project", "userName", "assignment", "workSessionId", "testEditSizeStmt", "editSizeStmt", "startTime", "endTime" };
		CoEvolutionVisitor visitor = new CoEvolutionVisitor();
		RepositoryMining miner = new RepositoryMining();
		miner = single ?
				miner.in(GitRepository.singleProject(this.infile)) :
				miner.in(GitRepository.allProjectsIn(this.infile));
		miner.through(Commits.all())
			.filters(
				new OnlyModificationsWithFileTypes(Arrays.asList(".java")),
				new OnlyNoMerge(),
				new OnlyInBranches(Arrays.asList("master"))
			)
			.setRepoTmpDir(Paths.get("/tmp/"))
			.process(visitor, new CSVFile(this.outfile, header))
			.mine();
	}

}
