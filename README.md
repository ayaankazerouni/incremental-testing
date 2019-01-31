# incremental-testing

Mining software repositories from student code to assess incremental development and testing practices.

This work will soon be published:
* Ayaan M. Kazerouni, Clifford A. Shaffer, Stephen H. Edwards, Francisco Servant. [*Assessing Incremental Testing Practices and Their Impact on Project Outcomes*](http://people.cs.vt.edu/~ayaan/assets/publications/Assessing_Incremental_Testing_Practices_and_Their_Impact_on_Project_Outcomes.pdf). SIGCSE Technical Symposium. 2019.

## Description

### Repository mining
There are two parts in this repository. The first part, under the [src/](src) directory, is a Java package that does repository mining on given repositories. Essentially, the steps are, for each commit:
* Checkout the commit
* Bucket it into a work session based on gaps of inactivity between commits
* For each modification in the commit,
  - Find out how much test code and how much production code was modified
  - Figure out which production methods were modified, and how much
  - Figure out which tests were modified, and how much, and which production methods are invoked in those tests
* Emit events containing the information gleaned from each commit

We therefore *expand* the raw Git history (commits) into a method-modification stream of events.

Repository mining was carried out using [RepoDriller](https://github.com/mauricioaniche/repodriller). This work depends on a particular branch from my fork of RepoDriller on GitHub: [ayaankazerouni/repodriller:sigcse-2019](https://github.com/ayaankazerouni/repodriller/tree/sigcse-2019) (see [mauricioaniche/repodriller#128](https://github.com/mauricioaniche/repodriller/issues/128)).

### Analysis
The events described above were further broken down using R, for further analysis. The code is in the [analysis/](analysis) directory.

The bulk of the analysis is rooted around measuring the *percentage of changes that were changes to test code*. We do this at multiple levels of granularity:
* At the *entire project / overall time* level
  - Figure out how many changes were made to test vs. production code
* At the *entire project / work session* level:
  - Figure out the same thing as above per work session
* At the *individual method / overall time* level:
  - Bucket the changes based on which production methods they were related to
  - Figure out the same thing as above
* At the *individual method / work session* (most granular) level:
  - For changes relating to each method, in each work session, figure out how many changes were made to test code vs. production code
 
We also measure how much test code was written before finishing the corresponding production code.
