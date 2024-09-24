#!/usr/bin/env bash

#set -e -o pipefail
if [ -z "$PGDATA" ]; then echo "PGDATA not set"; exit 1; fi
if ! hash pg_ctl 2>/dev/null; then echo psql client tools such as pg_ctl are not installed; exit 2; fi

export AWS_PROFILE=timafe
ENV_FILE=${HOME}/.angkor/.env
any_key_timeout=5 # seconds
bucket_name=$(grep "^BUCKET_NAME" $ENV_FILE |cut -d= -f2-)
appid=$(grep "^APPID" $ENV_FILE |cut -d= -f2-)
local_db_dev=${appid}_dev
local_db_test=${appid}_test
local_role=${appid}_dev
local_dump=/tmp/${appid}_latest.dump

logit() {  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"; }

logit "${appid}: Restoring DB from remote backup in $bucket_name PGDATA=$PGDATA"

pg_ctl -D "$PGDATA" status
if [ $? -eq 3 ]; then
  logit "psql is not running (exit 3), press CTRL-C to exit, any other key to start (autostart in ${any_key_timeout}s)"
  read -t $any_key_timeout dummy
  pg_ctl -D "$PGDATA" -l "${PGDATA}"/log.txt start
  sleep 1
fi

local_dump_base=$(basename $local_dump)
set -x; aws s3 cp s3://${bucket_name}/backup/db/$local_dump_base $local_dump;
{ set +x; } 2>/dev/null # do not display set +x

# put to script to enable recreate user
# DROP USER IF EXISTS angkor_dev;
# CREATE USER angkor_dev with password 'xxx';
# GRANT angkor_dev to <root>;
# export PGPASSWORD=$APP_USER_PW

logit "Recreating $local_db_dev + $local_db_test - THIS WILL ERASE ALL LOCAL DATA!!!"
logit "Press CTRL-C to exit, any other key to continue (autostart in ${any_key_timeout}s)"
# shellcheck disable=SC2034
read -t $any_key_timeout dummy

psql postgres <<-EOF
    SELECT pg_terminate_backend(pg_stat_activity.pid)
    FROM pg_stat_activity
    WHERE pg_stat_activity.datname = '$local_db_dev'
      AND pid <> pg_backend_pid();

    DROP DATABASE IF EXISTS $local_db_dev;
    DROP ROLE $local_role;
    CREATE ROLE $local_role;
    CREATE DATABASE $local_db_dev owner=$local_role;

    DROP DATABASE IF EXISTS $local_db_test;
    CREATE DATABASE $local_db_test owner=$local_role;
EOF

# todo: check if still required with neon db
psql $local_db_dev <<-EOF
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  CREATE EXTENSION IF NOT EXISTS "pg_trgm";
  CREATE EXTENSION IF NOT EXISTS "hstore";
EOF

psql $local_db_test <<-EOF
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  CREATE EXTENSION IF NOT EXISTS "pg_trgm";
  CREATE EXTENSION IF NOT EXISTS "hstore";
EOF

# https://dba.stackexchange.com/questions/84798/how-to-make-pg-dump-skip-extension
# https://stackoverflow.com/a/31470664/4292075
logit "Existing DBs $local_db_dev and $local_db_test re-initialized, triggering pg_restore"
set -x
pg_restore -l --single-transaction  $local_dump  |grep -v EXTENSION >"$(dirname $local_dump)/pg_restore_list"
pg_restore --use-list "$(dirname $local_dump)/pg_restore_list" \
           --no-owner --role=$local_role -U $local_role -d $local_db_dev  --single-transaction $local_dump
{ set +x; } 2>/dev/null
logit "Backup finished, running select check on $local_db_dev ($local_db_test remains empty)"
logit "Most recent backup may be from last night, run 'appctl backup-db' for a fresh one!"

psql -U $local_role -d $local_db_dev <<-EOF
  SELECT table_name,pg_size_pretty( pg_total_relation_size(quote_ident(table_name)))
  FROM information_schema.tables WHERE table_schema = 'public'
  ORDER BY pg_total_relation_size(quote_ident(table_name)) DESC
EOF
psql_exit=$?
if [ psql_exit -ne 0 ]; then
  echo "psql failed with exit code psql_exit"
  exit psql_exit;
fi

logit "Syncing s3://${bucket_name}/imagine with ${bucket_name}-dev"
# AWS S3 SYNC exclude patterns: https://stackoverflow.com/a/32394703/4292075
#  *: Matches everything
#  ?: Matches any single character
#  [sequence]: Matches any character in sequence
#  [!sequence]: Matches any character not in sequence
# Storage Classes: https://pkg.go.dev/github.com/aws/aws-sdk-go-v2/service/s3/types#ObjectStorageClass
aws s3 sync s3://${bucket_name}/imagine s3://${bucket_name}-dev/imagine --delete \
    --exclude '*songs/A*' --exclude '*songs/C*' --exclude '*songs/S*' --exclude '*songs/E*' \
    --storage-class ONEZONE_IA
logit "Recreate DBs in $PGDATA finished"
