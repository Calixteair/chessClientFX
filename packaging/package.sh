#!/usr/bin/env bash
# Fabrique une version autonome du client (Java + JavaFX embarqués) pour le système courant.
#   packaging/package.sh [version]     ex. packaging/package.sh 1.0.0
# Résultat dans target/release/ : Echecs-<version>-<os>-<arch>.(tar.gz|zip)
# Prérequis : JDK 17+ (jpackage), Maven. Utilisé tel quel par .github/workflows/release.yml.
set -euo pipefail

VERSION="${1:-1.0.0}"
NAME=Echecs
MODULE=com.chessclientfx/com.chessclientfx.ChessClientFX
cd "$(dirname "$0")/.."

mvn -q -B clean package javafx:jlink

case "$(uname -s)" in
  Linux*)  OS=linux;   ICON=packaging/icon.png ;;
  Darwin*) OS=macos;   ICON=target/icon.icns ;;
  MINGW*|MSYS*|CYGWIN*) OS=windows; ICON=packaging/icon.ico ;;
  *) echo "Système non géré : $(uname -s)" >&2; exit 1 ;;
esac
case "$(uname -m)" in
  x86_64|amd64) ARCH=x64 ;;
  arm64|aarch64) ARCH=arm64 ;;
  *) ARCH="$(uname -m)" ;;
esac

if [[ $OS == macos ]]; then
  # Icône macOS (.icns) générée depuis le PNG avec les outils du système.
  ICONSET=target/icon.iconset
  mkdir -p "$ICONSET"
  for size in 16 32 128 256 512; do
    sips -z $size $size packaging/icon.png --out "$ICONSET/icon_${size}x${size}.png" >/dev/null
    sips -z $((size * 2)) $((size * 2)) packaging/icon.png --out "$ICONSET/icon_${size}x${size}@2x.png" >/dev/null
  done
  iconutil -c icns "$ICONSET" -o "$ICON"
fi

# Par défaut, la version distribuée joue sur le serveur public (TLS via l'aiguillage SNI du VPS).
jpackage --type app-image --name "$NAME" --app-version "$VERSION" --vendor Calixteair \
  --description "Jeu d'échecs en ligne" --icon "$ICON" \
  --runtime-image target/runtime --module "$MODULE" \
  --arguments --host=chessfx.calixteair.fr --arguments --port=443 --arguments --tls=true \
  --dest target/app

ARCHIVE="$NAME-$VERSION-$OS-$ARCH"
mkdir -p target/release
case $OS in
  linux)   tar -C target/app -czf "target/release/$ARCHIVE.tar.gz" "$NAME" ;;
  macos)   ditto -c -k --keepParent "target/app/$NAME.app" "target/release/$ARCHIVE.zip" ;;
  windows) (cd target/app && powershell -NoProfile -Command "Compress-Archive -Path '$NAME' -DestinationPath '../release/$ARCHIVE.zip'") ;;
esac
ls -la target/release
