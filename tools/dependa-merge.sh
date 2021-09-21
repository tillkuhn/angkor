#!/usr/bin/env bash
me='🤖'
ctrlc='press ctrl-c to exit or any key to continue'

function usage { 
    cmd=$(basename $0)
    printf 'Usage: %s [-l] [-t] <branchname> ('origin/' prefix not required)\n' "$cmd"
    printf 'Examples:\n  %s dependabot/npm_and_yarn/ui/karma-6.1.0 (merge a branch, run tests before and after)\n' "$cmd"
    printf '  %s -l (to list all branches)\n' "$cmd"
    printf "\nTip: You can easily copy'n'paste the branch paste fro the PR in Gitlab (top section)\n\n" 
    exit 1
}

function listbranches() {
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
}

function underline {
     # https://stackoverflow.com/a/5349842/4292075 :-)
    printf '=%.0s' {1..40}; printf "\n"
} 
runtests=0
while getopts ":tl" o; do
    case "${o}" in
        # https://stackoverflow.com/a/16496491/4292075 for advanced
        t)
            runtests=1 #${OPTARG}
            ;;
        l)
            listbranches
            ;;
        *)
            echo "Unknown option $o"; usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "$1" ]; then
    usage
fi

printf '%s Welcome to DependaMerge - your friendly local buddy for Dependabot PRs \n' "$me" 


echo "${me} Checking if current branch contains changes ..."
if ! git diff --quiet; then
    echo "${me} ⚠️  git diff is dirty, $ctrlc (but local commit is recommended): "
    read -r dummy
fi

script_dir=$(dirname ${BASH_SOURCE[0]})
echo "${me} Fetching remote refs, cleanup obsolete remote tracking branches"
git fetch origin --prune
branch=$1
branch=${branch#origin/}; # Remove origin prefix if present in arg

echo "${me} Merging $branch"
git checkout -b $branch origin/$branch
git merge master -m "Merge branch 'master' into $branch"
if echo "$branch" | grep -q npm_and_yarn; then
    printf '\n%s Merging npm/yarn dependencies, this may cause issues' "$me"
    cd "${script_dir}"/../angular || exit
    if [ $runtests -eq 1 ]; then
        yarn test
        echo "${me} Test finished, $ctrlc (if successful): "
    else 
        printf '\n%s Tests skipped (-t runs tests before merge). %s: ' "$me" "$ctrlc"
    fi
    read -r dummy
    
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"
elif echo $branch|grep -q github_actions/; then
    printf '\n%s Merging github action dependencies, this is usually safe' "$me"
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo $branch|grep -q go_modules/; then  
    printf '\n%s Merging go dependencies, this is usually safe' "$me"
    cd "${script_dir}"/../tools || exit
    make test
    echo "${me} Test finished, if successful press any key to continue, else ctrl-c to exit"
    read -r dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"

elif echo "$branch"|grep -q gradle/; then
    printf '\n%s Merging gradle dependencies, this is usually safe' "$me"
    cd "${script_dir}"/../kotlin || exit
    gradle test
    echo "${me} Test finished, $ctrlc: "
    read -r dummy
 
    git checkout master
    git merge --no-ff $branch -m "Merge branch $branch"    
else
    echo "${me} ⚠️ $branch type not yet supported"
fi

# disable https://qastack.com.de/programming/12147360/git-branch-d-gives-warning
# try git push --delete origin old_branch
echo "${me} About to remove merged branch $branch from origin and locally."
echo "${me} $ctrlc"
read -r dummy
git push --delete origin "$branch"
git branch -d "$branch"

echo "${me} 👍 Finished merging $branch."
echo "${me} Don't forget to push to origin if merges took place. Have a nice day!"
