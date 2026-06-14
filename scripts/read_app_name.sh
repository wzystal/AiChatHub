#!/usr/bin/env bash
# 从工程资源读取应用显示名（钉钉卡片用）。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

read_from_strings() {
  local file="$1"
  [[ -f "$file" ]] || return 1
  local name
  name=$(sed -n 's/.*<string name="app_name">\([^<]*\)<\/string>.*/\1/p' "$file" | head -1)
  [[ -n "$name" ]] || return 1
  printf '%s' "$name"
}

for candidate in \
  "$ROOT/app/src/main/res/values/strings.xml" \
  "$ROOT/android/app/src/main/res/values/strings.xml"; do
  if read_from_strings "$candidate"; then
    exit 0
  fi
done

if [[ -f "$ROOT/app.json" ]] && command -v jq >/dev/null 2>&1; then
  name=$(jq -r '.expo.name // .name // empty' "$ROOT/app.json")
  if [[ -n "$name" && "$name" != "null" ]]; then
    printf '%s' "$name"
    exit 0
  fi
fi

basename "$ROOT"
