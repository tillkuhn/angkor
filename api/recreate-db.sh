#!/usr/bin/env bash

export AWS_PROFILE=timafe
ENV_FILE=${HOME}/.angkor/.env

bucket_name=$(grep "^bucket_name" $ENV_FILE |cut -d= -f2-)
appid=$(grep "^appid" $ENV_FILE |cut -d= -f2-)
local_db=${appid}_dev
local_role=${appid}_dev
local_dump=/tmp/${appid}_latest.dump

logit() {  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"; }

logit "${appid}: Restoring DB from remote backup in $bucket_name"

pg_ctl -D /usr/local/var/postgres status
if [ $? -eq 3 ]; then
  logit "psql is not running (exit 3), press CTRL-C to exit, any other key to start"
  read dummy
  pg_ctl -D /usr/local/var/postgres start
  sleep 1
fi

set -x; aws s3 cp s3://${bucket_name}/backup/db/$(basename $local_dump)  $local_dump;
{ set +x; } 2>/dev/null # do not display set +x

# put to script to enable recreate user
# DROP USER IF EXISTS angkor_dev;
# CREATE USER angkor_dev with password 'xxx';
# GRANT angkor_dev to <root>;
# export PGPASSWORD=$APP_USER_PW

logit "Recreating $local_db - THIS WILL ERASE ALL LOCAL DATA!!!"
logit "Press CTRL-C to exit, any other key to continue"
read dummy

psql postgres <<-EOF
    DROP DATABASE IF EXISTS $local_db;
    CREATE DATABASE $local_db owner=$local_role;
EOF

psql $local_db <<-EOF
  CREATE EXTENSION "uuid-ossp";
  CREATE EXTENSION "pg_trgm";
EOF

# https://dba.stackexchange.com/questions/84798/how-to-make-pg-dump-skip-extension
# https://stackoverflow.com/a/31470664/4292075
logit "Existing db recreated, triggering pg_restore"
set +x
pg_restore -l --single-transaction  $local_dump  |grep -v EXTENSION >$(dirname $local_dump)/pg_restore_list
pg_restore --use-list $(dirname $local_dump)/pg_restore_list \
           --no-owner --role=$local_role -U $local_role -d $local_db  --single-transaction $local_dump
{ set +x; } 2>/dev/null
logit "Backup finished, running select check"
psql -U $local_role -d $local_db -c 'SELECT COUNT(*) FROM place'
psqlexit=$?
if [ $psqlexit -ne 0 ]; then
  echo "psql failed with exit code $psqlexit"
  exit $psqlexit;
fi

logit "Syncing ${bucket_name}/imagine with ${bucket_name}-dev"
aws s3 sync s3://${bucket_name}/imagine s3://${bucket_name}-dev/imagine
