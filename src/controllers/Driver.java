
package controllers;

import org.repodriller.RepoDriller;

public class Driver {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Requires 2 arguments!\n\n"
					+ "Usage:\n\tjava -jar {jarfile} {path_to_repo(s)} {single_repo?}");
			return;
		}
		String repoPath = args[0];
		boolean single = Boolean.parseBoolean(args[1]);
		new RepoDriller().start(new MethodModificationStudy(repoPath, "/tmp/method-modifications.csv", single));
	}
}
