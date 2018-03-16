
package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		String repoPath = System.getProperty("user.home") + "/Developer/student-projects/testrepos/10d98918-7bee-4cb8-9316-7647a1947077_chrsrck_Project3";
		new RepoDriller().start(new SensorDataStudy(repoPath, "/tmp/test-creation.csv", true));
//		new RepoDriller().start(new CoEvolutionStudy(repoPath, "/tmp/coevolution.csv", true));
//		new RepoDriller().start(new MethodModificationStudy(repoPath, "/tmp/sample-event.csv", true));
	}
}
