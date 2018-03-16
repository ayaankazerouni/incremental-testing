#! /usr/bin/env python3

"""
Code to consolidate RepoDriller output with other metrics (like project score).
"""
# %% import libs and data
import pandas as pd
import numpy as np
import argparse

# %% define methods
def __extract_id_fields(project):
    # split = project.split('/')
    # project = split[len(split) - 1]
    fields = project.split('_')
    uuid = fields[0]
    user = fields[1]
    assignment = fields[2]
    assignment = assignment[0:len(assignment) - 1] + ' ' + assignment[len(assignment) - 1:]
    return { 'projectUuid': uuid, 'userName': user, 'assignment': assignment }

def __set_id_fields(row):
    fields = __extract_id_fields(row.project)
    row['userName'] = fields['userName']
    row['assignment'] = fields['assignment']
    row['projectUuid'] = fields['projectUuid']
    return row

def __aggregate_methods(group):
    tested = group[group.commit_test_invoked != 'null']
    percent_tested = len(tested) / len(group)
    tested = tested[tested.commit_test_invoked.ne(tested.commit_declared)]

    # aggregate additions to solution code
    solution_additions = tested[tested.solution_additions != 'null']['solution_additions']
    solution_additions = pd.to_numeric(solution_additions)
    med_solution_additions = np.nanmedian(solution_additions)
    avg_solution_additions = np.nanmean(solution_additions)

    # aggregate additions to test code
    test_additions = tested[tested.test_additions != 'null']['test_additions']
    test_additions = pd.to_numeric(test_additions)
    med_test_additions = np.nanmedian(test_additions)
    avg_test_additions = np.nanmean(test_additions)

    # solution + test additions
    additions = tested[tested.additions != 'null']['additions']
    additions = pd.to_numeric(additions)
    med_additions = np.nanmedian(additions)
    avg_additions = np.nanmean(additions)

    # aggregate removals from solution code
    solution_removals = tested[tested.solution_removals != 'null']['solution_removals']
    solution_removals = pd.to_numeric(solution_removals)
    med_solution_removals = np.nanmedian(solution_removals)
    avg_solution_removals = np.nanmean(solution_removals)

    # aggegate removals from test code
    test_removals = tested[tested.test_removals != 'null']['test_removals']
    test_removals = pd.to_numeric(test_removals)
    med_test_removals = np.nanmedian(test_removals)
    avg_test_removals = np.nanmean(test_removals)

    # solution + test removals
    removals = tested[tested.removals != 'null']['removals']
    removals = pd.to_numeric(removals)
    med_removals = np.nanmedian(removals)
    avg_removals = np.nanmean(removals)

    files_changed = tested[tested.files_changed != 'null']['files_changed']
    files_changed = pd.to_numeric(files_changed)
    med_files_changed = np.nanmedian(files_changed)
    avg_files_changed = np.nanmean(files_changed)

    complexity = pd.to_numeric(tested.cyclomatic_complexity)
    complexity = pd.to_numeric(complexity)
    med_complexity = np.nanmedian(complexity)
    avg_complexity = np.nanmean(complexity)

    to_write = {
        'avg_solution_additions': avg_solution_additions,
        'med_solution_additions': med_solution_additions,
        'avg_test_additions': avg_test_additions,
        'med_test_additions': med_test_additions,
        'avg_solution_removals': avg_solution_removals,
        'med_solution_removals': med_solution_removals,
        'avg_test_removals': avg_test_removals,
        'med_test_removals': med_test_removals,
        'avg_additions': avg_additions,
        'avg_removals': avg_removals,
        'avg_files_changed': avg_files_changed,
        'med_files_changed': med_files_changed,
        'avg_complexity': avg_complexity,
        'med_complexity': med_complexity,
        'percent_tested': percent_tested
    }

    return pd.Series(to_write)

def __aggregate_coevolution(group):
    group['ratio'] = group['testEditSizeStmt'] / group['editSizeStmt']
    group['percent'] = group['testEditSizeStmt'] / (group['editSizeStmt'] + group['testEditSizeStmt'])
    avg_ratio = np.mean(group['ratio'])
    avg_percent = np.mean(group['percent'])

    return pd.Series({ 'avg_ratio': avg_ratio, 'avg_percent': avg_percent })

def get_method_metrics(path):
    print('Loading method metrics data')

    dtypes = {
        'project': str,
        'method_id': str,
        'method': str,
        'date_declared': str,
        'date_test_invoked': str,
        'commit_declared': str,
        'commit_test_invoked': str,
        'cyclomatic_complexity': int,
        'solution_additions': str,
        'solution_removals': str,
        'test_additions': str,
        'test_removals': str,
        'additions': str,
        'removals': str,
        'files_changed': str
    }
    
    methods = pd.read_csv(path, dtype=dtypes)
    methods = methods.apply(__set_id_fields, axis=1)
    processed = methods.groupby(['userName', 'assignment']) \
                       .apply(__aggregate_methods) \
                       .dropna(axis=0, how='any') \
                       .reset_index() \
                       .set_index(['userName', 'assignment'])
    return processed

def get_coevolution_metrics(path):
    print('Loading coevolution data')

    coevolution = pd.read_csv(path)
    processed = coevolution.groupby(['userName', 'assignment']) \
                           .apply(__aggregate_coevolution) \
                           .dropna(axis=0, how='any') \
                           .reset_index() \
                           .set_index(['userName', 'assignment'])

    return processed


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('operation',
            help='[m|c]')
    parser.add_argument('--infile', '-i',
            help='path to output from repodriller mining')
    parser.add_argument('--outfile', '-o',
            help='path to desired output location (will be overwritten if already exists)')
    args = parser.parse_args()

    if args.operation == 'm':
        results = get_method_metrics(args.infile)
    elif args.operation == 'c':
        results = get_coevolution_metrics(args.infile)
    results.to_csv(args.outfile)
