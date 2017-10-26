package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		String repoPath = System.getProperty("user.dir") + "/student-repos/repos/b02ceb15-b340-4130-b09b-96bd16dfb8a1_tonaid_Project3/";
		new RepoDriller().start(new SensorDataStudy(repoPath, "/tmp/repo-mining.csv", true));
	}
}
