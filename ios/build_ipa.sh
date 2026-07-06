#!/usr/bin/env bash
# Сборка IPA для Teleport iOS (только macOS + Xcode)
set -euo pipefail
cd "$(dirname "$0")"

SCHEME="Teleport"
PROJECT="Teleport.xcodeproj"
ARCHIVE="build/Teleport.xcarchive"
IPA_DIR="build/ipa"
TEAM_ID="${DEVELOPMENT_TEAM:-}"

if [[ "$(uname)" != "Darwin" ]]; then
  echo "Ошибка: IPA можно собрать только на macOS с Xcode."
  exit 1
fi

if ! command -v xcodegen &>/dev/null; then
  echo "Установите XcodeGen: brew install xcodegen"
  exit 1
fi

xcodegen generate

mkdir -p build

SIGN_ARGS=()
if [[ -n "$TEAM_ID" ]]; then
  SIGN_ARGS+=(DEVELOPMENT_TEAM="$TEAM_ID")
fi

xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration Release \
  -archivePath "$ARCHIVE" \
  archive \
  CODE_SIGN_STYLE=Automatic \
  "${SIGN_ARGS[@]}"

mkdir -p "$IPA_DIR"

xcodebuild \
  -exportArchive \
  -archivePath "$ARCHIVE" \
  -exportPath "$IPA_DIR" \
  -exportOptionsPlist exportOptions.plist \
  "${SIGN_ARGS[@]}"

echo ""
echo "Готово: $IPA_DIR/Teleport.ipa"
