#!/usr/bin/env bash
me='🤖'
function underline {
     # https://stackoverflow.com/a/5349842/4292075 :-)
    printf '=%.0s' {1..40}; printf "\n"
} 
printf '%s Welcome to DependaMerge\n' "$me"
if [ $# -lt 1 ]; then
    printf 'Usage: %s <branchname> ('origin/' prefix not required)\n' "$0"
    printf 'Example: %s dependabot/npm_and_yarn/ui/karma-6.1.0\n' "$0"
    printf "Tip: You can easily copy'n'paste the branch paste fro the PR in Gitlab (top section)\n\n" 
    printf "Current remote dependabot branches\n"; underline
    echo "${me} fetching remotes"
    git fetch
    echo "${me} listing remotes matching origin/dependabot"
    git branch -r | grep  origin/dependabot
    printf "\nMerged local branches:\n"; underline
    echo "${me} listing merged local branches except master"
    git branch --merged| grep -v master
    printf "\nTips:\n"; underline
    printf "\nTo delete, run (use -D to force):\ngit branch --merged | grep -v master | xargs git branch -d\n"
    printf "\nTo prune tracking branches not / no longer on the remote run:\ngit remote prune origin\n"
    exit 1
fi

echo "${me} check if current branch contains changes"
if ! git diff --quiet; then
    echo "${me} ⚠️ git diff is dirty, ctrl-c to exit, any key to continue nevertheless"; read -r dummy
fi

script_dir=$(dirname ${BASH_SOURCE[0]})
git fetch origin
branch=$1
branch=${branch#origin/}; #Remove origin prefix if present

echo "${me} Merging $branch"
git checkout -b $branch origin/$branch
git merge master -m "Merge branch 'master' into $branch"
if echo "$branch" | grep -q npm_and_yarn; then
    echo "${me} npm / yarn fix, switching to /angular"
    cd "${script_dir}"/../angular || exit
    yarn test
    echo "${me} Test finished, if successfull press any key to continue, else ctrl-c to exit"
    read -r dummy
    
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"
elif echo $branch|grep -q github_actions/; then  
    echo "${me} Merging github actions, usually safe"
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo $branch|grep -q go_modules/; then  
    echo "${me} Merging golang dependencies, usually safe"
    cd "${script_dir}"/../tools || exit
    make test
    echo "${me} Test finished, if successful press any key to continue, else ctrl-c to exit"
    read -r dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo $branch|grep -q gradle/; then  
    echo "${me} Merging gradle dependencies, usually safe"
    cd "${script_dir}"/../kotlin || exit
    gradle test
    echo "Test finished, if successful press any key to continue, else ctrl-c to exit"
    read -r dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"    
else
    echo "$branch type not yet supported"
fi

# disable https://qastack.com.de/programming/12147360/git-branch-d-gives-warning
# try git push --delete origin old_branch
echo "${me} About to remove merged branch $branch from origin and locally."
echo "${me} If OK press any key to continue, else ctrl-c to exit"
git push --delete origin "$branch"
git branch -d "$branch"
# echo "git branch --merged | grep $branch | xargs git branch -d"

echo "${me} Finished. Don't forget to push to origin if merges took place. Have a nice day!"
