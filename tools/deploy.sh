#!/bin/sh
export SOURCES=/Volumes/diskE/Users/j5r/Documents/Personnel/dev/kawane/songbook
export DEPLOY=/Volumes/diskE/Users/j5r/Documents/Personnel/dev/kawane/openshift/songbook

rm -fr $DEPLOY/songbook
cp -R $SOURCES/out/artifacts/songbook/ $DEPLOY/songbook/
cd $DEPLOY
git add --all; git commit -m "Update"; git push
