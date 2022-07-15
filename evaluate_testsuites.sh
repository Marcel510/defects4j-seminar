#!/usr/bin/env bash
set -e

if [ $# != 2 ]; then
  echo "Usage: <d4j project name> <d4j bug id>"
  exit 1
fi

### Config
WORKDIR="" # Working directory for testwise coverage generation
TESTSUITES=("greedy" "hgs")
PROJECT=$1 # Defects4j name of project
BUG_ID=$2 # Defects4j Bug Id
###

if [ -z "$WORKDIR" ] || [ -z "$PROJECT" ]|| [ -z "$BUG_ID" ]; then
  echo "Error: Config is not set!"
  exit 1
fi

fixed_dir="$WORKDIR/${PROJECT}_${BUG_ID}_f"
coverage_dir="$WORKDIR/${PROJECT}_${BUG_ID}_coverage"
evaluation_dir="$coverage_dir/evaluation"
buggy_dir="$WORKDIR/${PROJECT}_b"

# Check if testsuite files exist
for testsuite in "${TESTSUITES[@]}"; do
  if [ ! -f "$coverage_dir/testsuite-$testsuite.txt" ]; then
    echo "Minimized testsuite $testsuite does not exist!"
    exit 1
  fi
done

echo "EVALUATING $PROJECT $BUG_ID"

if [ -d "$evaluation_dir" ]; then
    printf "Evaluation dir already exists, removing (%s)\n" "$evaluation_dir"
    rm -rf "$evaluation_dir"
fi
mkdir -p "$evaluation_dir"

# Checkout buggy version
defects4j checkout -p "$PROJECT" -v "${BUG_ID}b" -w "$buggy_dir"

cd "$buggy_dir" || exit 1

## Actual evaluation
result_file="$evaluation_dir/result.csv"

set +e
for testsuite in "${TESTSUITES[@]}"; do
  ts_file="$coverage_dir/testsuite-$testsuite.txt"

  echo "RUNNING $testsuite"
  defects4j monitor.test -t "$ts_file" >> "$evaluation_dir/$testsuite.log" 2>>"$evaluation_dir/$testsuite.log"

  exit_code=$?
  if [ $exit_code == 0 ]; then
    echo "$PROJECT;$BUG_ID;$testsuite;0" >> "$result_file"
    echo "BUG was NOT detected"
  elif [ $exit_code == 2 ]; then
    echo "$PROJECT;$BUG_ID;$testsuite;1" >> "$result_file"
    echo "BUG was detected"
  else
    echo "ERROR WHEN RUNNING TESTSUITE $testsuite for $PROJECT $BUG_ID (return code d4j: $exit_code)"
    exit 1
  fi

  # save testsuitetestrunner_log
  cp "$buggy_dir/testsuitetestrunner_log" "$evaluation_dir/${testsuite}-all-tests.txt"
  grep "FAILED" "$buggy_dir/testsuitetestrunner_log" > "$evaluation_dir/${testsuite}-failed-tests.txt"
done

