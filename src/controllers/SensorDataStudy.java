package controllers;

import java.nio.file.Paths;
import java.util.Arrays;

import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyInBranches;
import org.repodriller.filter.commit.OnlyModificationsWithFileTypes;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.diff.OnlyDiffsWithFileTypes;
import org.repodriller.filter.range.Commits;

import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.CollectConfiguration;
import org.repodriller.scm.GitRepository;

import visitors.commits.SensorDataVisitor;

public class SensorDataStudy implements Study {
	
	private String infile;
	private String outfile;
	/**
	 * Is this study on a single repo or many?
	 */
	private boolean single;
	
	/**
	 * Initialise the Study on the specified repository or directory
	 * of repositories.
	 * 
	 * @param infile	The input (a repo or directory containing many repos)
	 * @param outfile	The CSV output (will get overwritten if it already exists)
	 * @param single	Is this study happening on one repo or many repos?
	 */
	public SensorDataStudy(String infile, String outfile, boolean single) {
		this.infile = infile;
		this.outfile = outfile;
		this.single = single;
	}
	
	/**
	 * Initialise and execute the miner with a single repository or a directory
	 * containing many repositories, then begin mining using
	 * a SensorDataVisitor object.
	 */
	@Override
	public void execute() {
		String[] header = new String[] {
				"project",
				"userName",
				"assignment",
				"methodId",
				"methodName",
				"dateDeclared",
				"dateTestInvoked",
				"commitDeclared",
				"commitTestInvoked",
				"cyclomaticComplexity",
				"solutionAdditions",
				"solutionRemovals",
				"testAdditions",
				"testRemovals",
				"additions",
				"removals",
				"filesChanged"
		};
		
		SensorDataVisitor visitor = new SensorDataVisitor();
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
			.collect(new CollectConfiguration().branches().sourceCode().diffs(new OnlyDiffsWithFileTypes(Arrays.asList(".java"))))
			.setRepoTmpDir(Paths.get("/tmp/"))
			.visitorsAreThreadSafe(true)
			.visitorsChangeRepoState(true)
			.withThreads()
			.process(visitor, new CSVFile(this.outfile, header))
			.mine();
	}
}
