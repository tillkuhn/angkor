#!/usr/bin/env bash
logit() {  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"; }
logit "Action: $1"
