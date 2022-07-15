#!/usr/bin/env bash
set -e

if [ $# != 2 ]; then
  echo "Usage: <d4j project name> <d4j bug id>"
  exit 1
fi

### Config
JACOCO_AGENT_DIR="" # Path to root directory of teamscale jacoco agent
WORKDIR="" # Working directory for testwise coverage generation
PROJECT=$1 # Defects4j name of project
BUG_ID=$2 # Defects4j Bug Id
###

if [ -z "$JACOCO_AGENT_DIR" ] || [ -z "$WORKDIR" ] || [ -z "$PROJECT" ]|| [ -z "$BUG_ID" ]; then
  echo "Error: Config is not set!"
  exit 1
fi

basedir="$( cd "$(dirname "$0")" || exit 1 ; pwd -P )"
util_dir="$basedir/seminar-util"
fixed_dir="$WORKDIR/${PROJECT}_${BUG_ID}_f"
coverage_dir="$WORKDIR/${PROJECT}_${BUG_ID}_coverage"

### Exports
export SWQ_AGENT_JARPATH="$JACOCO_AGENT_DIR/lib/teamscale-jacoco-agent.jar"

if [ -d "$coverage_dir" ]; then
    printf "Coverage dir already exists, removing (%s)\n" "$coverage_dir"
    rm -rf "$coverage_dir"
fi
mkdir "$coverage_dir" -p

# Checkout fixed version
defects4j checkout -p "$PROJECT" -v "${BUG_ID}f" -w "$fixed_dir"
cd "$fixed_dir" || exit 1
git checkout -B default

# Run tests for test execution report
export SWQ_EXECUTIONREPORTLISTENER_ENABLE=true
cd "$fixed_dir" || exit 1
defects4j test
export SWQ_EXECUTIONREPORTLISTENER_ENABLE=false
# Convert test execution report to ts jacoco agent format
cd "$util_dir" || exit 1
./gradlew run --args="convertExecutionReport ${fixed_dir}/testExecutionReport.csv $coverage_dir $PROJECT $BUG_ID" || exit 1

# Run tests for test wise coverage
## Generating agent config
cd "$util_dir" || exit 1
agent_config_path="${coverage_dir}/agentconfig.txt"
./gradlew run --args="generateAgentConfig $agent_config_path $coverage_dir $PROJECT $BUG_ID" || exit 1
## Run tests
export SWQ_AGENT_ENABLE=true
export SWQ_AGENT_CONFIGFILEPATH="$agent_config_path"
export SWQ_TESTWISELISTENER_ENABLE=true
cd "$fixed_dir" || exit 1
defects4j test
export SWQ_AGENT_ENABLE=false
export SWQ_TESTWISELISTENER_ENABLE=false
## Convert using ts jacoco agent
cd "$fixed_dir" || exit 1
classes="$fixed_dir/$(defects4j export -p dir.bin.classes),$fixed_dir/$(defects4j export -p dir.bin.tests)"
$JACOCO_AGENT_DIR/bin/convert -t -c "$classes" -o "$coverage_dir/testwise-coverage" -i "$coverage_dir"