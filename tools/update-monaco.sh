#!/bin/sh
# Vendors a minimal subset of the Monaco editor (AMD min/vs build) into the web assets.
# Bump VERSION, run from the repo root, test /edit/{id}, then commit the result.
set -eu

VERSION="0.52.2"
DEST="src/dist/web/js/vendor/monaco"

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

echo "Downloading monaco-editor ${VERSION}..."
curl -fsSL "https://registry.npmjs.org/monaco-editor/-/monaco-editor-${VERSION}.tgz" -o "$TMP/monaco.tgz"
tar -xzf "$TMP/monaco.tgz" -C "$TMP"

SRC="$TMP/package/min/vs"
test -f "$SRC/loader.js" || { echo "Unexpected tarball layout: $SRC/loader.js not found" >&2; exit 1; }

rm -rf "$DEST"
mkdir -p "$DEST/vs/editor" "$DEST/vs/base/worker" "$DEST/vs/base/browser/ui/codicons/codicon"

cp "$SRC/loader.js"                 "$DEST/vs/"
cp "$SRC/editor/editor.main.js"     "$DEST/vs/editor/"
cp "$SRC/editor/editor.main.css"    "$DEST/vs/editor/"
# editor.main.nls.js disappeared in newer versions (merged into editor.main.js); copy if present
[ -f "$SRC/editor/editor.main.nls.js" ] && cp "$SRC/editor/editor.main.nls.js" "$DEST/vs/editor/"
cp "$SRC/base/worker/workerMain.js" "$DEST/vs/base/worker/"
cp "$SRC/base/browser/ui/codicons/codicon/codicon.ttf" "$DEST/vs/base/browser/ui/codicons/codicon/"

echo "$VERSION" > "$DEST/VERSION"
echo "Monaco ${VERSION} vendored into $DEST"
du -sh "$DEST"
