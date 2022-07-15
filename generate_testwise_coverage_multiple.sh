#!/usr/bin/env bash
set -e

if [ $# != 3 ]; then
  echo "Usage: <d4j project name> <d4j bug id start> <d4j bug id end>"
  exit 1
fi

project_name=$1
start=$(($2))
end=$(($3))

for ((i=start; i <= end; i++ )) do
  ./generate_testwise_coverage.sh "$project_name" "$i"
done