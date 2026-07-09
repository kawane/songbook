#!/bin/sh
# Vendors the pinned mesnos design system stylesheet from components.mesnos.ovh.
# Bump VERSION, run from the repo root, check the site rendering, then commit.
set -eu

VERSION="0.1.44"
DEST="src/dist/web/css/vendor"

mkdir -p "$DEST"
curl -fsSL "https://components.mesnos.ovh/v${VERSION}/mesnos.style.css" -o "$DEST/mesnos.style.css"
echo "$VERSION" > "$DEST/MESNOS_VERSION"
echo "mesnos.style.css ${VERSION} vendored into $DEST"
