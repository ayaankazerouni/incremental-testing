package controllers;

import java.util.Arrays;

import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyModificationsWithFileTypes;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import visitors.AggregateRepoVisitor;

public class AggregateRepoStudy implements Study {
	private String infile;
	private String outfile;
	
	public AggregateRepoStudy(String infile, String outfile) {
		this.infile = infile;
		this.outfile = outfile;
	}
	
	@Override
	public void execute() {
		AggregateRepoVisitor visitor = new AggregateRepoVisitor();
		
		new RepositoryMining()
			.in(GitRepository.allProjectsIn(this.infile))
			.through(Commits.all())
			.withThreads(3)
			.filters(
				new OnlyModificationsWithFileTypes(Arrays.asList(".java")),
				new OnlyNoMerge()
			)
			.process(visitor, new CSVFile(this.outfile))
			.mine();
	}
}
