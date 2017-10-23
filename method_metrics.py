# %% import libs and data
import pandas as pd
import numpy as np
from datetime import datetime

# %% setup methods
def processrepo(group):
    untested_methods = group[group.commit_test_invoked == 'null'].size

    additions = pd.to_numeric(group.additions[group.additions != 'null'])
    avg_additions = np.nanmean(additions)
    med_additions = np.nanmedian(additions)

    removals = pd.to_numeric(group.removals[group.removals != 'null'])
    avg_removals = np.nanmean(removals)
    med_removals = np.nanmedian(removals)

    files_changed = pd.to_numeric(group.files_changed[group.files_changed != 'null'])
    avg_files_changed = np.nanmean(files_changed)
    med_files_changed = np.nanmedian(files_changed)

    complexity = pd.to_numeric(group.cyclomatic_complexity)
    avg_complexity = np.nanmean(complexity)
    med_complexity = np.nanmedian(complexity)

    towrite = {
        'untested_methods': untested_methods,
        'avg_additions': avg_additions,
        'avg_removals': avg_removals,
        'avg_files_changed': avg_files_changed,
        'med_additions': med_additions,
        'med_removals': med_removals,
        'med_files_changed': med_files_changed
    }

    return pd.Series(towrite)

df = pd.read_csv("/tmp/repo-mining.csv")

# %% play with data here
groups = df.groupby(['project'])
processed = groups.apply(processrepo)