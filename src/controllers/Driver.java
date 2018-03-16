
package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		String repoPath = System.getProperty("user.home") + "/Developer/student-projects/testrepos/c07a5411-88d4-4e86-a99a-7f74f5b1a110_shuvesta_Project4";
//		new RepoDriller().start(new SensorDataStudy(repoPath, "/tmp/repo-mining.csv", false));
//		new RepoDriller().start(new CoEvolutionStudy(repoPath, "/tmp/coevolution.csv", false));
		new RepoDriller().start(new MethodModificationStudy(repoPath, "/tmp/sample-event.csv", true));
	}
}
