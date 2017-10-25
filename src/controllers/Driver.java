package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		String repoPath = System.getProperty("user.dir") + "/student-repos/repos/";
		new RepoDriller().start(new SensorDataStudy(repoPath, "/tmp/repo-mining.csv", false));
	}
}
