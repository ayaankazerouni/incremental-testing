#! /usr/bin/env python3

# %% import libs and data
import pandas as pd
import numpy as np
import argparse

def __aggregate_methods(group):
    tested = group[group.commitTestInvoked != 'null']
    percent_tested = len(tested) / len(group)
    tested = tested[tested.commitTestInvoked.ne(tested.commitDeclared)]

    # aggregate additions to solution code
    solutionAdditions = tested[tested.solutionAdditions != 'null']['solutionAdditions']
    solutionAdditions = pd.to_numeric(solutionAdditions)
    med_solutionAdditions = np.nanmedian(solutionAdditions)
    avg_solutionAdditions = np.nanmean(solutionAdditions)

    # aggregate additions to test code
    testAdditions = tested[tested.testAdditions != 'null']['testAdditions']
    testAdditions = pd.to_numeric(testAdditions)
    med_testAdditions = np.nanmedian(testAdditions)
    avg_testAdditions = np.nanmean(testAdditions)

    # solution + test additions
    additions = tested[tested.additions != 'null']['additions']
    additions = pd.to_numeric(additions)
    med_additions = np.nanmedian(additions)
    avg_additions = np.nanmean(additions)

    # aggregate removals from solution code
    solutionRemovals = tested[tested.solutionRemovals != 'null']['solutionRemovals']
    solutionRemovals = pd.to_numeric(solutionRemovals)
    med_solutionRemovals = np.nanmedian(solutionRemovals)
    avg_solutionRemovals = np.nanmean(solutionRemovals)

    # aggegate removals from test code
    testRemovals = tested[tested.testRemovals != 'null']['testRemovals']
    testRemovals = pd.to_numeric(testRemovals)
    med_testRemovals = np.nanmedian(testRemovals)
    avg_testRemovals = np.nanmean(testRemovals)

    # solution + test removals
    removals = tested[tested.removals != 'null']['removals']
    removals = pd.to_numeric(removals)
    med_removals = np.nanmedian(removals)
    avg_removals = np.nanmean(removals)

    filesChanged = tested[tested.filesChanged != 'null']['filesChanged']
    filesChanged = pd.to_numeric(filesChanged)
    med_filesChanged = np.nanmedian(filesChanged)
    avg_filesChanged = np.nanmean(filesChanged)

    complexity = pd.to_numeric(tested.cyclomaticComplexity)
    complexity = pd.to_numeric(complexity)
    med_complexity = np.nanmedian(complexity)
    avg_complexity = np.nanmean(complexity)

    to_write = {
        'avg_solutionAdditions': avg_solutionAdditions,
        'med_solutionAdditions': med_solutionAdditions,
        'avg_testAdditions': avg_testAdditions,
        'med_testAdditions': med_testAdditions,
        'avg_solutionRemovals': avg_solutionRemovals,
        'med_solutionRemovals': med_solutionRemovals,
        'avg_testRemovals': avg_testRemovals,
        'med_testRemovals': med_testRemovals,
        'avg_additions': avg_additions,
        'avg_removals': avg_removals,
        'avg_filesChanged': avg_filesChanged,
        'med_filesChanged': med_filesChanged,
        'avg_complexity': avg_complexity,
        'med_complexity': med_complexity,
        'percent_tested': percent_tested
    }

    return pd.Series(to_write)

def get_method_metrics(path):
    print('Loading method metrics data')

    dtypes = {
        'project': str,
        'method_id': str,
        'method': str,
        'date_declared': str,
        'date_test_invoked': str,
        'commitDeclared': str,
        'commitTestInvoked': str,
        'cyclomaticComplexity': int,
        'solutionAdditions': str,
        'solutionRemovals': str,
        'testAdditions': str,
        'testRemovals': str,
        'additions': str,
        'removals': str,
        'filesChanged': str
    }
    
    methods = pd.read_csv(path, dtype=dtypes)
    processed = methods.groupby(['userName', 'assignment']) \
                       .apply(__aggregate_methods) \
                       .dropna(axis=0, how='any') \
                       .reset_index() \
                       .set_index(['userName', 'assignment'])
    return processed

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--infile', '-i',
            help='path to output from repodriller mining')
    parser.add_argument('--outfile', '-o',
            help='path to desired output location (will be overwritten if already exists)')
    args = parser.parse_args()

    results = get_method_metrics(args.infile)
    results.to_csv(args.outfile)
