
package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		String repoPath = args[0];
		boolean single = Boolean.parseBoolean(args[1]);
		new RepoDriller().start(new MethodModificationStudy(repoPath, "/tmp/method-modifications.csv", single));
	}
}
