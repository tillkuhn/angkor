#!/usr/bin/env bash
script_dir=$(dirname ${BASH_SOURCE[0]})
if ! git diff --quiet; then
    echo "git diff is dirty, ctrl-c to exit, any key to continue anyway"; read dummy
fi
git fetch origin
if [ $# -lt 1 ]; then
    echo "Usage $0 <branchname> (without origin/ prefix) e.g."
    echo "$0 dependabot/npm_and_yarn/ui/karma-6.1.0"
    echo; echo "Current remote dependabot branches"
    git branch -r | grep  origin/dependabot
    exit 1
fi
# git checkout $1
git checkout -b $1 origin/$1
git merge master -m "Merge branch 'master' into $1"
if echo $1|grep -q npm_and_yarn; then
    echo "npm / yarn fix, switching to /ui"
    cd ${script_dir}/../ui
    yarn install
    yarn test
    echo "Test finished, if successfull press any key to continue, else ctrl-c to exit"
    
    read dummy
    git checkout master
    git merge --no-ff $1 -m "Merge branch $1"
    echo "Merged, don't forget to push"
elif echo $1|grep -q github_actions; then  
    echo "Merging github actions, usually safe"
    git checkout master
    git merge --no-ff $1 -m "Merge branch $1"
    echo "Merged, don't forget to push"
else
    echo "$1 type not yet supported"
fi