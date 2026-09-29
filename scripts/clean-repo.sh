#!/usr/bin/env bash
set -euo pipefail

# Removes only untracked/ignored build and editor residue. It never deletes tracked files.
git clean -ndX
if [ "${1:-}" = "--apply" ]; then
  git clean -fdX
else
  echo "Dry run only. Re-run with --apply after reviewing the list."
fi
