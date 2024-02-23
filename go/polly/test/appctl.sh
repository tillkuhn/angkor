#!/usr/bin/env bash
# Mock script to simulate POLLY_DELEGATE

logit() {  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"; }
logit "script=$0 arg1=$1"
