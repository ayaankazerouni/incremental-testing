package controllers;

import java.util.Arrays;
import java.util.Calendar;

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
		Calendar from = Calendar.getInstance();
		from.set(Calendar.DAY_OF_MONTH, 9);
		from.set(Calendar.MONTH, 9);
		from.set(Calendar.YEAR, 2016);
		Calendar to = Calendar.getInstance();
		to.set(Calendar.DAY_OF_MONTH, 11);
		to.set(Calendar.MONTH, 9);
		to.set(Calendar.YEAR, 2016);
		SensorDataVisitor visitor = new SensorDataVisitor();
		
		new RepositoryMining()
			.in(GitRepository.singleProject("/home/ayaan/Developer/repos/18779_P2"))
			.through(Commits.betweenDates(from, to))
			.filters(
				new OnlyModificationsWithFileTypes(Arrays.asList(".java")),
				new OnlyNoMerge()
			)
			.process(visitor, new CSVFile("/tmp/rd.csv"))
			.mine();
	}
}
