#!/usr/bin/env sh
# Recrea los symlinks relativos .claude/skills/<skill> -> ../../.agents/skills/<skill>
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "$ROOT/.claude/skills"
for dir in "$ROOT"/.agents/skills/*/; do
  name="$(basename "$dir")"
  ln -sfn "../../.agents/skills/$name" "$ROOT/.claude/skills/$name"
  echo "linked $name"
done
