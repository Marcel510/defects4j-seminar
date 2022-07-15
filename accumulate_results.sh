#!/usr/bin/env bash
set -e

### Config
WORKDIR="" # Working directory for testwise coverage generation
###

if [ -z "$WORKDIR" ]; then
  echo "Error: WORKDIR is not set!"
  exit 1
fi

cd $WORKDIR

info_complete_file="$WORKDIR/info.csv"
result_complete_file="$WORKDIR/result.csv"

rm -f "$info_complete_file" "$result_complete_file" || true


# shellcheck disable=SC2035
find */info.csv | while read -r i
do
    cat "$i" >> "$info_complete_file"
done

# shellcheck disable=SC2035
find */evaluation/result.csv | while read -r i
do
    cat "$i" >> "$result_complete_file"
done

echo "DONE"
