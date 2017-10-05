package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		String singleRepoPath = "~/Developer/repos/10116_P4".replaceFirst("^~", System.getProperty("user.home"));
		String aggregateRepoPath = "~/Developer/repos/".replaceFirst("^~", "user.home");
		new RepoDriller().start(new SingleRepoStudy(singleRepoPath, "/tmp/singleRepo.csv"));
//		new RepoDriller().start(new AggregateRepoStudy(aggregateRepoPath, "/tmp/aggregateRepo.csv"));
	}
}
