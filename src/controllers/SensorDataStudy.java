package controllers;

import java.util.Arrays;

import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyInBranches;
import org.repodriller.filter.commit.OnlyModificationsWithFileTypes;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import visitors.SensorDataVisitor;

public class SensorDataStudy implements Study {
	
	private String infile;
	private String outfile;
	/**
	 * Is this study on a single repo or many?
	 */
	private boolean single;
	
	public SensorDataStudy(String infile, String outfile, boolean single) {
		this.infile = infile;
		this.outfile = outfile;
		this.single = single;
	}
	
	/**
	 * Initialise the miner with a single repository or a directory
	 * containing many repositories, then begin mining using
	 * a SensorDataVisitor object.
	 */
	@Override
	public void execute() {
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
			.process(visitor, new CSVFile(this.outfile))
			.mine();
	}
}
