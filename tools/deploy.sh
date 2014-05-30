#!/bin/sh
export SOURCES=/Volumes/diskE/Users/j5r/Documents/Personnel/dev/git/songbook
export DEPLOY=/Users/j5r/Documents/Personnel/dev/git/openshift/songbook

rm -fr $DEPLOY/songbook
cp -R $SOURCES/out/artifacts/songbook/ $DEPLOY/songbook/
cd $DEPLOY
git add --all; git commit -m "Update"; git push
