#!/usr/bin/env bash
script_dir=$(dirname ${BASH_SOURCE[0]})
if ! git diff --quiet; then
    echo "git is dirty, any key to continue anyway"; read dummy
fi
if [ $# -lt 1 ]; then
    echo "Usage $0 <branchname> (without origin/ prefix) e.g."
    echo "$0 dependabot/npm_and_yarn/ui/karma-6.1.0"
    exit 1
fi
git fetch origin
# git checkout $1
git checkout -b $1 origin/$1
git merge master
if echo $1|grep -q npm_and_yarn; then
    echo "npm / yarn fix, switching to /ui"
    cd ${script_dir}/../ui
    yarn install
    yarn test
    echo "test finished, any to continue, ctrl-c to exit"
    read dummy
    git checkout master
    git merge --no-ff $1
    echo "Merged, don't forget to push"
fi