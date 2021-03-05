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
branch=$1
branch=${branch#origin/}; #Remove origin prefix if present

echo "Merging $branch"
# git checkout $branch
git checkout -b $branch origin/$branch
git merge master -m "Merge branch 'master' into $branch"
if echo $branch|grep -q npm_and_yarn; then
    echo "npm / yarn fix, switching to /ui"
    cd ${script_dir}/../ui
    yarn install
    yarn test
    echo "Test finished, if successfull press any key to continue, else ctrl-c to exit"
    
    read dummy
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"
    echo "Merged, don't forget to push"
elif echo $branch|grep -q github_actions; then  
    echo "Merging github actions, usually safe"
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"
    echo "Merged, don't forget to push"
else
    echo "$branch type not yet supported"
fi