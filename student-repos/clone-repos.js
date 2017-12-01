#! /usr/bin/env node

const parse = require('csv-parse/lib/sync');
const fs = require('fs');
const { execSync, exec } = require('child_process');
const REMOTE_URL = 'web-cat.cs.vt.edu/Web-CAT/WebObjects/Web-CAT.woa/git/StudentProject';

if (process.argv.length < 4) {
	console.error('Specify an input file and an output directory.');
}

const file = process.argv[2];
const outputdir = process.argv[3];

if (!fs.existsSync(file)) {
  console.error(`Error! Could not find file: ${file}`);
  process.exit(1);
}

const set = new Set();
const fileContents = fs.readFileSync(file, 'utf8');
const records = parse(fileContents, { columns: true });
records.forEach((r) => {
  const projectUuid = r['project.uuid'];
	if (!set.has(projectUuid)) {
		const userUuid = r['user.uuid'];
		const userName = r['email'].split('@')[0];
		const assignment = r['assignment.name'].replace(/\s/g, ''); // assignment names sometimes have spaces
		// make the dirName so that we can match repos to other metrics by user
		const dirName = `${projectUuid}_${userName}_${assignment}`;
		const cmd = `git clone https://${userUuid}:${projectUuid}@${REMOTE_URL}/${projectUuid} ${outputdir}/${dirName}`;
		try {
			execSync(cmd);
		} catch (err) {
			console.log(err.message, 'Clone unsuccessful.');
		} finally {
			set.add(projectUuid);
		}	
	} else {
		console.log(`Already cloned ${projectUuid}`);
	}
});
