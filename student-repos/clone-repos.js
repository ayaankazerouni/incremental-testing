#! /usr/bin/env node

const parse = require('csv-parse/lib/sync');
const fs = require('fs');
const { execSync, exec } = require('child_process');
const REMOTE_URL = 'web-cat.cs.vt.edu/Web-CAT/WebObjects/Web-CAT.woa/git/StudentProject';

const file = process.argv[2];

if (!fs.existsSync(file)) {
  console.error(`Error! Could not find file: ${file}`);
  process.exit(1);
}

const fileContents = fs.readFileSync(file, 'utf8');
const records = parse(fileContents, { columns: true });
records.forEach((r) => {
  const projectUuid = r['project.uuid'];
  const userUuid = r['user.uuid'];
	const userName = r['email'].split('@')[0];
	const assignment = r['assignment.name'].replace(/\s/g, ''); // assignment names somtimes have spaces
	// make the dirName so that we can match repos to other metrics by user
	const dirName = `${projectUuid}_${userName}_${assignment}`;
  const cmd = `git clone https://${userUuid}:${projectUuid}@${REMOTE_URL}/${projectUuid} repos/${dirName}`;
  try {
    execSync(cmd);
  } catch (err) {
    console.log(err.message, 'Clone unsuccessful.');
  }
});
