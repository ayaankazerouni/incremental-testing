package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
//		String singleRepoPath = "~/Developer/repos/b3e2ce51-6de5-4770-94c9-27ca8eb6eb78".replaceFirst("^~", System.getProperty("user.home"));
		String aggregateRepoPath = "~/Developer/repos/".replaceFirst("^~", System.getProperty("user.home"));
//		new RepoDriller().start(new SingleRepoStudy(singleRepoPath, "/tmp/singleRepo.csv"));
		new RepoDriller().start(new AggregateRepoStudy(aggregateRepoPath, "/tmp/aggregateRepo.csv"));
	}
}
