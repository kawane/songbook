#!/bin/sh
# Vendors the pinned mesnos design system from components.mesnos.ovh: the
# stylesheet and the shared dialog (mesnos-dialog.js, standalone: no import).
# Bump VERSION, run from the repo root, check the site rendering and the
# delete confirmation, then commit.
set -eu

VERSION="0.4.0"
BASE="https://components.mesnos.ovh/v${VERSION}"
CSS_DEST="src/dist/web/css/vendor"
JS_DEST="src/dist/web/js/vendor"

mkdir -p "$CSS_DEST" "$JS_DEST"
curl -fsSL "$BASE/mesnos.style.css" -o "$CSS_DEST/mesnos.style.css"
curl -fsSL "$BASE/mesnos-dialog.js" -o "$JS_DEST/mesnos-dialog.js"
echo "$VERSION" > "$CSS_DEST/MESNOS_VERSION"
echo "mesnos.style.css and mesnos-dialog.js ${VERSION} vendored"
