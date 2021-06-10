#!/usr/bin/env bash
#json-server [options] <source>
#
#Optionen:
#  --config, -c               Path to config file  [Standard: "json-server.json"]
#  --port, -p                 Set port                           [Standard: 3000]
#  --host, -H                 Set host                    [Standard: "localhost"]
#  --watch, -w                Watch file(s)                             [boolean]
#  --routes, -r               Path to routes file
#  --middlewares, -m          Paths to middleware files                   [array]
#  --static, -s               Set static files directory
#  --read-only, --ro          Allow only GET requests                   [boolean]
#  --no-cors, --nc            Disable Cross-Origin Resource Sharing     [boolean]
#  --no-gzip, --ng            Disable GZIP Content-Encoding             [boolean]
#  --snapshots, -S            Set snapshots directory             [Standard: "."]
#  --delay, -d                Add delay to responses (ms)
#  --id, -i                   Set database id property (e.g. _id)[Standard: "id"]
#  --foreignKeySuffix, --fks  Set foreign key suffix (e.g. _id as in post_id)
#                                                                [Standard: "Id"]
#  --quiet, -q                Suppress log messages from output         [boolean]
#  --help, -h                 Hilfe anzeigen                            [boolean]
#  --version, -v              Version anzeigen                          [boolean]
#
#Beispiele:
#  json-server db.json
#  json-server file.js
#  json-server http://example.com/db.json

if ! which json-server >/dev/null; then
  echo "Please install https://github.com/typicode/json-server with yarn / npm globally"
  exit 1
fi
echo "Starting json-server $(json-server --version)"
json-server  --port 8080 --watch --routes server/routes.json server/db.json

## for Mocking AWS Resources
SERVICES=dynamodb DEFAULT_REGION=eu-central-1  localstack --debug start  --host
awslocal dynamodb list-tables
