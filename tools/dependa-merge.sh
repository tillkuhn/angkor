#!/usr/bin/env bash
me='🤖'
function underline {
     # https://stackoverflow.com/a/5349842/4292075 :-)
    printf '=%.0s' {1..40}; printf "\n"
} 
printf "${me} Welcome to DependaMerge\n"
if [ $# -lt 1 ]; then
    printf "Usage: $0 <branchname> ('origin/' prefix not required)\n"
    printf "Example: $0 dependabot/npm_and_yarn/ui/karma-6.1.0\n"
    printf "Tip: You can easily copy'n'paste the branch paste fro the PR in Gitlab (top section)\n\n" 
    printf "Current remote dependabot branches\n"; underline
    echo "${me} fetching remotes"
    git fetch
    echo "${me} listing remotes matching origin/dependabot"
    git branch -r | grep  origin/dependabot
    printf "\nMerged local branches:\n"; underline
    echo "${me} listing merged local branches except master"
    git branch --merged| grep -v master
    printf "\nTo delete, exec (use -D to force):\ngit branch --merged | grep -v master | xargs git branch -d\n" 
    printf "To prunes tracking branches not / no longer on the remote:\ngit remote prune origin\n"
    exit 1
fi

echo "${me} check if current branch contains changes"
if ! git diff --quiet; then
    echo "${me} git diff is dirty, ctrl-c to exit, any key to continue nevertheless"; read dummy
fi

script_dir=$(dirname ${BASH_SOURCE[0]})
git fetch origin
branch=$1
branch=${branch#origin/}; #Remove origin prefix if present

echo "${me} Merging $branch"
# git checkout $branch
git checkout -b $branch origin/$branch
git merge master -m "Merge branch 'master' into $branch"
if echo $branch|grep -q npm_and_yarn; then
    echo ${me} npm / yarn fix, switching to /ui"
    cd ${script_dir}/../ui
    yarn install
    yarn test
    echo "${me} Test finished, if successfull press any key to continue, else ctrl-c to exit"
    read dummy
    
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"
elif echo $branch|grep -q github_actions/; then  
    echo "${me} Merging github actions, usually safe"
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo $branch|grep -q go_modules/; then  
    echo "${me} Merging golang dependencies, usually safe"
    cd ${script_dir}/../tools
    make test
    echo "${me} Test finished, if successfull press any key to continue, else ctrl-c to exit"
    read dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo $branch|grep -q gradle/; then  
    echo "${me} Merging gradle dependencies, usually safe"
    cd ${script_dir}/../api
    gradle test
    echo "Test finished, if successfull press any key to continue, else ctrl-c to exit"
    read dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"    
else
    echo "$branch type not yet supported"
fi

echo "${me} Removing merged branches matching $branch"
git branch --merged | grep  | xargs git branch -d

echo "${me} Finished. Don't forget to push to origin if merges took place. Have a nice day!"
