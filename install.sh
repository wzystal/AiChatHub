#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

APP_ID="com.example.aichatdemo"
LAUNCHER_ACTIVITY="${APP_ID}/.ui.chat.ChatActivity"

log() {
  printf '==> %s\n' "$*"
}

fail() {
  printf '错误: %s\n' "$*" >&2
  exit 1
}

if [[ ! -x "./gradlew" ]]; then
  fail "未找到可执行的 ./gradlew，请在工程根目录运行此脚本"
fi

if ! command -v adb >/dev/null 2>&1; then
  fail "未找到 adb，请安装 Android SDK Platform-Tools 并加入 PATH"
fi

device_list="$(adb devices | awk 'NR>1 && $2 == "device" { print $1 }')"
if [[ -z "$device_list" ]]; then
  fail "未检测到已连接且可用的 adb 设备，请先连接设备或启动模拟器"
fi

device_count="$(printf '%s\n' "$device_list" | sed '/^$/d' | wc -l | tr -d ' ')"
if [[ "$device_count" -gt 1 ]]; then
  selected_device="$(printf '%s\n' "$device_list" | head -n 1)"
  export ANDROID_SERIAL="$selected_device"
  log "检测到 ${device_count} 台设备，使用: ${selected_device}"
fi

log "编译并安装 Debug 包到设备..."
./gradlew :app:installDebug

log "启动应用..."
adb shell am start -n "$LAUNCHER_ACTIVITY"

log "安装完成"
