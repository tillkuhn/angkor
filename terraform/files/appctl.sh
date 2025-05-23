#!/usr/bin/env bash
#####################################################################
# Controller script to set up and manager angkor components
######################################################################
# consider 'set -eux pipefail'
# set -u tells the shell to treat expanding an unset parameter an error, which helps to catch e.g. typos in variable names.
# set -e tells the shell to exit if a command exits with an error (except if the exit value is tested in some other way). T
# more inspiration: https://ollama.ai/install.sh, https://gist.github.com/mohanpedala/1e2ff5661761d3abd0385e8223e16425
SCRIPT=$(basename "${BASH_SOURCE[0]}")
WORKDIR=$(dirname "${BASH_SOURCE[0]}")  # the location of this script is considered to be the working directory
ENV_CONFIG="${WORKDIR}/.env_config"     # we expect env_config to be pulled from s3 during user-data initialization
export WORKDIR

# logging function with timestamp
logit() {  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"; }

# check if appctl is running as root, so no sudo magic is required for tasks that require elevated permissions
is_root() { [ ${EUID:-$(id -u)} -eq 0 ]; }

# publish takes both action and message arg and publishes it to the system topic
publish() { [ -x "${WORKDIR}"/tools/topkapi ] && "${WORKDIR}"/tools/topkapi -source appctl -action "$1" -message "$2" -topic system -source appctl; }

# experimental function for new confluent /  cloud-event based event exchange
# $1 = sub-event / function-name (e.g. db-backup)
# $2 = subject according to https://github.com/cloudevents/
# $3 = error_code (0 indicates success)
# $4 = optional outcome
publish_v2() {
  if [ -x "${WORKDIR}"/tools/rubin ]; then
    "${WORKDIR}"/tools/rubin -env-file "${WORKDIR}"/.env -ce -key "${SCRIPT}/$1" \
      -source "urn:ce:${SCRIPT}:$1" -type "net.timafe.event.system.$1.v1" \
      -subject "$2" -topic "system.events" -record "{\"error_code\":$3,\"actor\":\"$USER\",\"outcome\":\"$4\"}"
  else
    logit "WARN: ${WORKDIR}/tools/rubin not (yet) installed"
  fi
}

# no args? we think you need serious help
if [ $# -lt 1 ]; then
    set -- help # inject help argument if called w/o args
fi

# source variables form .env in working directory
# shellcheck disable=SC1090
if [ -f "$ENV_CONFIG" ]; then
  logit "Loading environment from $ENV_CONFIG"
  set -a; source "$ENV_CONFIG"; set +a  # -a = auto export variables
else
  logit "FATAL: environment config file $ENV_CONFIG not found"
  exit 1
fi

###############################
# block for common setup tasks
if [[ "$*" == *setup* ]] || [[ "$*" == *all* ]]; then
  logit "Performing common init tasks"
  mkdir -p "${WORKDIR}/docs" "${WORKDIR}/logs" "${WORKDIR}/backup" "${WORKDIR}/tools" "${WORKDIR}/upload"
  # get appid and other keys via ec2 tags. region returns AZ at the end, so we need to crop it
  # not available during INIT when run as part of user-data????
  # APPID=$(aws ec2 describe-tags --filters "Name=resource-id,Values=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)" \
  #    "Name=key,Values=appid" --output=json  | jq -r .Tags[0].Value)
  AWS_REGION=$(curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone|sed 's/[a-z]$//')
  aws configure set default.region "$AWS_REGION"
  aws configure set region "$AWS_REGION"
  logit "APPID=$APPID AWS_REGION=$AWS_REGION"

  # ${APPID,,} = make lowercase
  grep -q -e  "^alias ${APPID,,}=" ~/.bashrc || echo "alias ${APPID,,}=~/appctl.sh" >>.bashrc
  grep -q -e  "^alias ak=" ~/.bashrc || echo "alias ak=~/appctl.sh" >>.bashrc
  grep -q -e  "^alias appctl=" ~/.bashrc || echo "alias appctl=~/appctl.sh" >>.bashrc
  grep -q -e  "^alias l=" ~/.bashrc || echo "alias l='ls -aCF'" >>.bashrc
  grep -q  "/usr/bin/fortune" ~/.bashrc || \
    echo 'echo "$-" | grep i > /dev/null && [ -x /usr/bin/fortune ] && /usr/bin/fortune' >>.bashrc
fi

# todo read vars from ssm param store
#for P in $(aws ssm get-parameters-by-path --path "/angkor/prod" --output json | jq -r  .Parameters[].Name); do
#  K=$(echo $P|tr '[:lower:]' '[:upper:]')
# String operator ## trims everything from the front until a '/', greedily.
#  echo "${K##*/}=$(aws ssm get-parameter --name $P --with-decryption --query "Parameter.Value" --output text)"
#done


# pull file artifacts needed for all targets from s3
if [[ "$*" == *update* ]] || [[ "$*" == *all* ]]; then
  logit "Updating docker-compose and script artifacts including myself"
  aws s3 sync "s3://${BUCKET_NAME}/deploy" "${WORKDIR}" --exclude "*/*"
  chmod ugo+x "${WORKDIR}/${SCRIPT}"
fi

# new: pull secrets from HCP Vault Platform App Secrets, also runs on 'update'
# expects HCP_ORGANIZATION,HCP_PROJECT,HCP_CLIENT_ID and HCP_CLIENT_SECRET in .env
# todo: https://docs.docker.com/compose/environment-variables/set-environment-variables/#use-the-env_file-attribute
if [[ "$*" == *pull-secrets* ]] || [[ "$*" == *update* ]] || [[ "$*" == *all* ]]; then
  # vlt login; vlt apps list
  secrets_store="rt-secrets"
  env_file="${WORKDIR}/.env_secrets"
  echo "# Generated - DO NOT EDIT. Secrets pulled from HCP $secrets_store by appctl.sh" >"$env_file"
  logit "Pulling secrets from HCP $secrets_store"
  env -i PATH="$PATH" HCP_ORGANIZATION="$HCP_ORGANIZATION" HCP_CLIENT_ID="$HCP_CLIENT_ID"  HCP_CLIENT_SECRET="$HCP_CLIENT_SECRET" HCP_PROJECT="$HCP_PROJECT" \
     vlt run --app-name "$secrets_store"  printenv | grep -ve "^HCP_" | grep -ve "^PATH" >>"$env_file"
  no_of_secrets=$(grep -c -ve '^#' "$env_file")
  logit "Secrets pulled from $secrets_store: $no_of_secrets"
  cat "$ENV_CONFIG" "$env_file" >"${WORKDIR}/.env"
fi

# init daily cron jobs
if [[ "$*" == *init-cron* ]] || [[ "$*" == *all* ]]; then
  logit "Setting up scheduled tasks  in /etc/cron.daily"
  # renew cert script if to check if cert can be renewed
  sudo bash -c "cat >/etc/cron.daily/renew-cert" <<-'EOF'
/home/ec2-user/appctl.sh renew-cert >>/home/ec2-user/logs/renew-cert.log 2>&1
EOF

  # cron daily backup script to backup db and local files to s3
  sudo bash -c "cat >/etc/cron.daily/backup-all" <<-'EOF'
/home/ec2-user/appctl.sh backup-db >>/home/ec2-user/logs/backup-db.log 2>&1
/home/ec2-user/appctl.sh backup-s3 >>/home/ec2-user/logs/backup-data.log 2>&1
EOF

  # cleanup scrip for stale docker images
  sudo bash -c "cat >/etc/cron.daily/docker-prune" <<-'EOF'
docker system prune -f >>/home/ec2-user/logs/docker-prune.log 2>&1
EOF

  # remindabot launch script
  sudo bash -c "cat >/etc/cron.daily/remindabot" <<-'EOF'
/home/ec2-user/tools/remindabot -envfile=/home/ec2-user/.env >>/home/ec2-user/logs/remindabot.log 2>&1
EOF

  # generic loop to make sure every cron-script is executable
  for SCRIPT in backup-all renew-cert docker-prune remindabot; do sudo chmod ugo+x /etc/cron.daily/${SCRIPT}; done
fi

# trigger regular database and s3 bucket backups
# todo cleanup older dumps locally and s3 (via lifecycle rule), use variables for db host and app-user
if [[ "$*" == *backup-db* ]]; then
  # https://docs.elephantsql.com/elephantsql_api.html
  #logit "Trigger PostgresDB for db=$DB_USERNAME via ElephantSQL API" # db username = dbname
  #publish "runjob:backup-db" "Backup PostgresDB for DB ${DB_USERNAME}@api.elephantsql.com"
  #curl -sS -i -u ":$DB_API_KEY" https://api.elephantsql.com/api/backup -d "db=$DB_USERNAME"
  pg_version=16
  db_dump_dir=${WORKDIR}/backup/db
  mkdir -p "$db_dump_dir"
  dumpfile=$db_dump_dir/${DB_USERNAME}_$(date +"%Y-%m-%d-at-%H-%M-%S").sql
  dumpfile_latest=$db_dump_dir/${APPID}_latest.dump
  db_host=$(echo "$DB_URL"|cut -d/ -f3|cut -d: -f1) # todo refactor variables since DB_URL is jdbc specific
  logit "Creating local backup $dumpfile from $db_host and upload to s3://$BUCKET_NAME pg_version=$pg_version"
  # run psql via docker since there's no longer a compatible psql client for amazon linux 2 client for pg 16
  docker run -v "$db_dump_dir:$db_dump_dir" --rm -e PGPASSWORD="$APPCTL_DB_PASSWORD" postgres:$pg_version pg_dump -F p -c --no-owner -h "$db_host" -U "$DB_USERNAME" -d "$DB_USERNAME" -f "$dumpfile"
  # PGPASSWORD=$APPCTL_DB_PASSWORD pg_dump -F p -c --no-owner -h "$db_host" -U "$DB_USERNAME" "$DB_USERNAME" >"$dumpfile"
  dumpfile_basename=$(basename "$dumpfile")
  aws s3 cp --storage-class STANDARD_IA "$dumpfile" "s3://${BUCKET_NAME}/backup/db/history/$dumpfile_basename"

  #replica_info=$(echo "$APPCTL_REPLICA_DB_URL"|cut -d@ -f2)
  #logit "Replicating to $replica_info from $dumpfile"
  #psql -d "${APPCTL_REPLICA_DB_URL}" <"$dumpfile" >"${WORKDIR}/backup/db/${APPID}_replica.log" 2>&1

  logit "Creating custom formatted latest backup $dumpfile_latest + upload to s3://$BUCKET_NAME"
  # PGPASSWORD=$APPCTL_DB_PASSWORD pg_dump -h "$db_host" -U "$DB_USERNAME" "$DB_USERNAME" -Z2 -Fc > "$dumpfile_latest"
  docker run -v $db_dump_dir:$db_dump_dir --rm -e PGPASSWORD="$APPCTL_DB_PASSWORD" postgres:$pg_version pg_dump -Z2 -Fc --no-owner -h "$db_host" -U "$DB_USERNAME" -d "$DB_USERNAME" -f "$dumpfile_latest"
  dumpfile_latest_basename=$(basename "$dumpfile_latest")
  aws s3 cp --storage-class STANDARD_IA "$dumpfile_latest" "s3://${BUCKET_NAME}/backup/db/$dumpfile_latest_basename"

  if is_root; then
    logit "Running with sudo, adapting local backup permissions in $db_dump_dir"
    /usr/bin/chown -R ec2-user:ec2-user "$db_dump_dir"
  fi
  publish_v2 "backup-db" "$DB_USERNAME@$db_host" 0 "DB $db_host successfully dumped up to $dumpfile and exported s3://$BUCKET_NAME"
fi

if [[ "$*" == *backup-s3* ]]; then
  logit "Backup app bucket s3://${BUCKET_NAME}/ to ${WORKDIR}/backup/"
  publish "runjob:backup-s3" "Triggering Backup for ${WORKDIR}/backup files to s3://${BUCKET_NAME}/"
  aws s3 sync "s3://${BUCKET_NAME}" "${WORKDIR}/backup/s3" --exclude "deploy/*" --exclude "imagine/songs/*"
  if is_root; then
    logit "Running with sudo, adapting local backup permissions"
    /usr/bin/chown -R ec2-user:ec2-user "${WORKDIR}"/backup/s3
  fi
fi

# renew certbot certificate if it's close to expiry date
if [[ "$*" == *renew-cert* ]] || [[ "$*" == *all* ]]; then
  logit "Deploy and renew SSL Certificates"
  publish "runjob:certbot" "Starting certbot in standalone mode for ${CERTBOT_DOMAIN_STR} "

  CERTBOT_ADD_ARGS="" # use --dry-run to simulate certbot interaction
  if ! hash certbot >/dev/null 2>/dev/null; then
    publish "runjob:certbot" "ERROR: certbot not installed"
  elif docker ps --no-trunc -f name="^/${APPID}-ui$" |grep -q "$APPID"; then
    echo "${APPID}-ui is up, adding temporary shut down hook for certbot renew"
    set -x
    # CERTBOT_DOMAIN_STR can have multiple values, e.g. "-d bla.net -d www.bla.net -d dev.bla.net"
    # so we need do skip double quotes despite the "word splitting" warning (since we actually want splitting here)
    sudo --preserve-env=WORKDIR certbot --standalone -m "${CERTBOT_MAIL}" --agree-tos --expand --redirect -n ${CERTBOT_DOMAIN_STR} \
         --pre-hook "docker-compose --no-ansi --file ${WORKDIR}/docker-compose.yml stop ${APPID}-ui" \
         --post-hook "docker-compose --no-ansi --file ${WORKDIR}/docker-compose.yml start ${APPID}-ui" \
         ${CERTBOT_ADD_ARGS} certonly
    set +x
  else
    echo "${APPID}-ui is down or not yet installed, so certbot can take safely over port 80"
    sudo --preserve-env=WORKDIR certbot --standalone -m "${CERTBOT_MAIL}" --agree-tos --expand --redirect -n ${CERTBOT_DOMAIN_STR} \
         ${CERTBOT_ADD_ARGS} certonly
  fi

  # if files relevant to letsencrypt changed today, trigger backup update and push notification event
  renew_cert_outcome=""
  if sudo find /etc/letsencrypt/ -type f -mtime -1 |grep -q "."; then
    renew_cert_outcome="Files in /etc/letsencrypt changed after certbot run, trigger backup"
    publish "renew:cert" "SSL Cert has been renewed for ${CERTBOT_DOMAIN_STR} "

    sudo tar -C /etc -zcf /tmp/letsencrypt.tar.gz letsencrypt
    sudo aws s3 cp --sse=AES256 /tmp/letsencrypt.tar.gz "s3://${BUCKET_NAME}/backup/letsencrypt.tar.gz"
    sudo rm -f /tmp/letsencrypt.tar.gz
  else
    renew_cert_outcome="Files in /etc/letsencrypt are unchanged after certbot run, skip backup"
  fi
  logit "$renew_cert_outcome"
  publish_v2 "renew-cert" "${CERTBOT_DOMAIN_STR}" 0 "$renew_cert_outcome"

fi

# deploy antora docs (which is volume mounted into nginx)
if [[ "$*" == *deploy-docs* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying Antora docs, clean local dir first to prevent sync issues"
  set -x
  rm -rf "${WORKDIR}"/docs/*
  aws s3 sync --delete "s3://${BUCKET_NAME}/deploy/docs" "${WORKDIR}/docs/"
  set +x
  publish "deploy:docs" "Recent Antora docs have been synced from s3://${BUCKET_NAME}/deploy/docs to ${WORKDIR}/docs/"
fi

# deploy backend (api)
if [[ "$*" == *deploy-api* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying API Backend"
  # pull recent docker images from dockerhub
  docker pull "${DOCKER_USER}/${APPID}-api:${API_VERSION}"
  docker-compose --file "${WORKDIR}"/docker-compose.yml up --detach "${APPID}"-api
fi

# deploy frontend
if [[ "$*" == *deploy-ui* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying UI Frontend"
  docker pull "${DOCKER_USER}/${APPID}-ui:${UI_VERSION}"
  docker-compose --file "${WORKDIR}"/docker-compose.yml up --detach "${APPID}"-ui
fi

# deploy golang SQS Poller and other tools ....
if [[ "$*" == *deploy-tools* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying healthbells and imagine"
  docker pull "ghcr.io/${DOCKER_USER}/${APPID}-tools:main"
  docker-compose --file "${WORKDIR}/docker-compose.yml" up --detach healthbells
  docker-compose --file "${WORKDIR}/docker-compose.yml" up --detach imagine

  logit "Extracting tools from docker image and copy them to ~/tools"
  # container will be shown with -a only, and remove by docker system prune
  container_id=$(docker create --rm "ghcr.io/${DOCKER_USER}/${APPID}-tools:main")
  docker cp "${container_id}:/tools/" /home/ec2-user/
  /usr/bin/chmod ugo+x /home/ec2-user/tools/*

  logit "Installing polly.service for event polling"
  # https://jonathanmh.com/deploying-go-apps-systemd-10-minutes-without-docker/
  logit "Setting up systemd service /etc/systemd/system/polly.service"
  sudo bash -c "cat >/etc/systemd/system/polly.service" <<-EOF
[Unit]
Description=polly SQS polling service
After=network.target remote-fs.target nss-lookup.target docker.service

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user
#ExecStartPre=/usr/bin/mkdir -p /home/ec2-user/tools
ExecStart=/home/ec2-user/tools/polly
SuccessExitStatus=143
SyslogIdentifier=polly
EnvironmentFile=/home/ec2-user/.env
# restart automatically Clean exit code or signal
# In this context, a clean exit means an exit code of 0, or one of the signals SIGHUP, SIGINT, SIGTERM or SIGPIPE
# Restart=on-success
Restart=always
RestartSec=15s

[Install]
WantedBy=multi-user.target
EOF
  sudo systemctl daemon-reload
  sudo systemctl enable polly
  sudo systemctl start polly
  systemctl status polly
fi

# if target requires docker-compose interaction, show  docker containers once
if [[ "$*" == *ui* ]] ||  [[ "$*" == *api* ]] || [[ "$*" == *all* ]]; then
  docker ps
fi

# thanks https://cloudcone.com/docs/article/check-which-folders-use-the-highest-disk-space-in-linux/
if [[ "$*" == *disk-usage* ]]; then
  df -hk
  sudo  du -h /  2>/dev/null | grep '[0-9\.]\+G'
fi

# help is required - display usage
if [[ "$*" == *help* ]]; then
    echo "Usage: $SCRIPT [target]"
    echo
    echo "Targets:"
    echo "  all           Runs all targets"
    echo "  backup-db     Backup Database"
    echo "  backup-s3     Backup S3 Data Bucket"
    echo "  deploy-api    Deploys Spring Boot API"
    echo "  deploy-docs   Deploys Antora Docs"
    echo "  deploy-tools  Deploys tools such as sqs-poller"
    echo "  deploy-ui     Deploys Angular UI"
    echo "  disk-usage    Show folders with highest disk space consumption"
    echo "  help          This help"
    echo "  init-cron     Init Cronjob(s)"
    echo "  pull-secrets  Pull secrets from HCP Vault Secrets (experimental)"
    echo "  renew-cert    Deploys and renews SSL certificate"
    echo "  setup         Setup config, directories etc."
    echo "  update        Update myself and docker-compose config"
    echo
fi
