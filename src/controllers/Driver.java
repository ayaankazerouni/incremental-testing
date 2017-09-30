package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		new RepoDriller().start(new SingleRepoStudy("/Users/ayaankazerouni/Developer/repos/10116_P4", "/tmp/singleRepo.csv"));
//		new RepoDriller().start(new AggregateRepoStudy("/home/ayaan/Developer/repos/", "/tmp/aggregateRepo.csv"));
	}
}
