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
records.forEach((r, index) => {
  const projectUuid = r['project.uuid'];
  const userUuid = r['user.uuid'];
  const cmd = `git clone https://${userUuid}:${projectUuid}@${REMOTE_URL}/${projectUuid} student-repos/repos/${projectUuid}`;
  try {
    execSync(cmd);
  } catch (err) {
    console.log(err.message, 'Clone unsuccessful.');
  }
});
