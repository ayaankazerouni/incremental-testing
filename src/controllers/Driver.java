package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		new RepoDriller().start(new SingleRepoStudy("/home/ayaan/Developer/repos/18779_P2", "/tmp/singleRepo.csv"));
	}

}
