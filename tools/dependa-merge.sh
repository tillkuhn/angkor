#!/usr/bin/env bash
function underline {
     # https://stackoverflow.com/a/5349842/4292075 :-)
    printf '=%.0s' {1..40}; printf "\n"
} 
if [ $# -lt 1 ]; then
    printf "\nUsage: $0 <branchname> (without 'origin/' prefix)\n"
    printf "Example: $0 dependabot/npm_and_yarn/ui/karma-6.1.0\n"
    printf "Tip: You can easily copy'n'paste the branch paste fro the PR in Gitlab (top section)\n\n" 
    printf "Current remote dependabot branches\n"; underline
    git branch -r | grep  origin/dependabot
    printf "\nMerged local branches:\n"; underline
    git branch --merged| grep -v master
    printf "\nTo delete, exec (use -D to force)...\ngit branch --merged | grep -v master | xargs git branch -d\n" 
    exit 1
fi
if ! git diff --quiet; then
    echo "git diff is dirty, ctrl-c to exit, any key to continue anyway"; read dummy
fi

script_dir=$(dirname ${BASH_SOURCE[0]})
git fetch origin
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
elif echo $branch|grep -q github_actions/; then  
    echo "Merging github actions, usually safe"
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo $branch|grep -q go_modules/; then  
    echo "Merging golang dependencies, usually safe"
    cd ${script_dir}/../tools
    make test
    echo "Test finished, if successfull press any key to continue, else ctrl-c to exit"
    read dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo $branch|grep -q gradle/; then  
    echo "Merging gradle dependencies, usually safe"
    cd ${script_dir}/../api
    gradle test
    echo "Test finished, if successfull press any key to continue, else ctrl-c to exit"
    read dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"    
else
    echo "$branch type not yet supported"
fi

echo "Don't forget to push to origin if merges took place. Have a nice day!"
