package controllers;

import java.util.Arrays;

import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyModificationsWithFileTypes;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import visitors.SingleRepoVisitor;

public class SingleRepoStudy implements Study {
	
	private String infile;
	private String outfile;
	
	public SingleRepoStudy(String infile, String outfile) {
		this.infile = infile;
		this.outfile = outfile;
	}
	
	@Override
	public void execute() {
		SingleRepoVisitor visitor = new SingleRepoVisitor();
		
		new RepositoryMining()
			.in(GitRepository.singleProject(this.infile))
			.through(Commits.all())
			.filters(
				new OnlyModificationsWithFileTypes(Arrays.asList(".java")),
				new OnlyNoMerge()
			)
			.process(visitor, new CSVFile(this.outfile))
			.mine();
	}
}
