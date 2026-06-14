#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
STAMP_FILE=".build-and-install.stamp"
GRADLE="./gradlew"
ADB="${ADB:-adb}"
APP_PACKAGE="${APP_PACKAGE:-com.example.aichatdemo}"
LAUNCHER_ACTIVITY="${LAUNCHER_ACTIVITY:-.ui.chat.ChatActivity}"

log() { printf '[build-and-install] %s\n' "$*"; }
die() { log "ERROR: $*"; exit 1; }

compute_source_hash() {
  find app \
    \( -path '*/build/*' -o -path '*/.gradle/*' \) -prune -o \
    -type f \( -name '*.kt' -o -name '*.java' -o -name '*.xml' -o -name '*.kts' -o -name '*.gradle' -o -name '*.properties' \) -print \
    | LC_ALL=C sort | while IFS= read -r file; do shasum -a 256 "$file"; done | shasum -a 256 | awk '{print $1}'
}

pick_usb_device() {
  local devices
  devices="$("$ADB" devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')"
  [[ -n "$devices" ]] || die "未检测到 USB 设备"
  printf '%s\n' "$devices" | head -n1
}

main() {
  [[ -x "$GRADLE" ]] || die "未找到 gradlew"
  command -v "$ADB" >/dev/null || die "未找到 adb"
  local hash saved=""
  hash="$(compute_source_hash)"
  [[ -f "$STAMP_FILE" ]] && saved="$(cat "$STAMP_FILE")"
  if [[ "$hash" != "$saved" || ! -f "$APK_PATH" ]]; then
    log "编译 debug APK ..."
    "$GRADLE" :app:assembleDebug --no-daemon
    printf '%s' "$hash" > "$STAMP_FILE"
  else
    log "无变更，跳过编译"
  fi
  local device
  device="$(pick_usb_device)"
  "$ADB" -s "$device" install -r "$APK_PATH"
  "$ADB" -s "$device" shell am start -n "${APP_PACKAGE}/${LAUNCHER_ACTIVITY}" >/dev/null 2>&1 || true
  log "安装完成"
}

main "$@"
