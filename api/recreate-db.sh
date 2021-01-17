#!/usr/bin/env bash

export AWS_PROFILE=timafe
ENV_FILE=${HOME}/.angkor/.env
any_key_timeout=5 # secons
bucket_name=$(grep "^BUCKET_NAME" $ENV_FILE |cut -d= -f2-)
appid=$(grep "^APPID" $ENV_FILE |cut -d= -f2-)
local_db_dev=${appid}_dev
local_db_test=${appid}_test
local_role=${appid}_dev
local_dump=/tmp/${appid}_latest.dump

logit() {  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"; }

logit "${appid}: Restoring DB from remote backup in $bucket_name"

pg_ctl -D /usr/local/var/postgres status
if [ $? -eq 3 ]; then
  logit "psql is not running (exit 3), press CTRL-C to exit, any other key to start (autostart in ${any_key_timeout}s)"
  read -t $any_key_timeout dummy
  pg_ctl -D /usr/local/var/postgres start
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
read -t $any_key_timeout dummy

psql postgres <<-EOF
    DROP DATABASE IF EXISTS $local_db_dev;
    CREATE DATABASE $local_db_dev owner=$local_role;
    DROP DATABASE IF EXISTS $local_db_test;
    CREATE DATABASE $local_db_test owner=$local_role;
EOF

psql $local_db_dev <<-EOF
  CREATE EXTENSION "uuid-ossp";
  CREATE EXTENSION "pg_trgm";
EOF
psql $local_db_test <<-EOF
  CREATE EXTENSION "uuid-ossp";
  CREATE EXTENSION "pg_trgm";
EOF

# https://dba.stackexchange.com/questions/84798/how-to-make-pg-dump-skip-extension
# https://stackoverflow.com/a/31470664/4292075
logit "Existing db recreated, triggering pg_restore"
set +x
pg_restore -l --single-transaction  $local_dump  |grep -v EXTENSION >$(dirname $local_dump)/pg_restore_list
pg_restore --use-list $(dirname $local_dump)/pg_restore_list \
           --no-owner --role=$local_role -U $local_role -d $local_db_dev  --single-transaction $local_dump
{ set +x; } 2>/dev/null
logit "Backup finished, running select check on $local_db_dev ($local_db_test remains empty)"
psql -U $local_role -d $local_db_dev <<-EOF
SELECT table_name,pg_size_pretty( pg_total_relation_size(quote_ident(table_name)))
FROM information_schema.tables WHERE table_schema = 'public'
ORDER BY pg_total_relation_size(quote_ident(table_name)) DESC
EOF
psqlexit=$?
if [ $psqlexit -ne 0 ]; then
  echo "psql failed with exit code $psqlexit"
  exit $psqlexit;
fi

logit "Syncing s3://${bucket_name}/imagine with ${bucket_name}-dev"
aws s3 sync s3://${bucket_name}/imagine s3://${bucket_name}-dev/imagine
logit "Recreate DB finished"
