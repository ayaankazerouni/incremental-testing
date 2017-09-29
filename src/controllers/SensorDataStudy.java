package controllers;

import java.util.Arrays;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyModificationsWithFileTypes;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import visitors.SensorDataVisitor;

public class SensorDataStudy implements Study {
	
	public static void main(String[] args) { 
		new RepoDriller().start(new SensorDataStudy());
	}
	
	@Override
	public void execute() {
		SensorDataVisitor visitor = new SensorDataVisitor();
		
		new RepositoryMining()
			.in(GitRepository.singleProject("/home/ayaan/Developer/repos/18779_P2"))
			.through(Commits.all())
			.filters(
				new OnlyModificationsWithFileTypes(Arrays.asList(".java")),
				new OnlyNoMerge()
			)
			.process(visitor, new CSVFile("/tmp/rd.csv"))
			.mine();
	}
}
