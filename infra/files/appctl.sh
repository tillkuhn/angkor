#!/usr/bin/env bash
SCRIPT=$(basename ${BASH_SOURCE[0]})
export WORKDIR=$(dirname ${BASH_SOURCE[0]})

## useful functions
logit() {  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"; }
isroot() { [ ${EUID:-$(id -u)} -eq 0 ]; }
publish() { [ -x ${WORKDIR}/tools/topkapi ] && ${WORKDIR}/tools/topkapi -source appctl -action "$1" -message "$2" -topic system; }

# no args? you need help
if [ $# -lt 1 ]; then
    set -- help # display help if called w/o args
fi

# source variables form .env
if [ -f ${WORKDIR}/.env ]; then
  logit "Loading environment from ${WORKDIR}/.env"
  set -a; source ${WORKDIR}/.env; set +a  # -a = auto export
else
  logit "environment file ${WORKDIR}/.env not found"
  exit 1
fi

# common setup
if [[ "$*" == *setup* ]] || [[ "$*" == *all* ]]; then
  logit "Performing common init taks"
  mkdir -p ${WORKDIR}/docs ${WORKDIR}/logs ${WORKDIR}/backup ${WORKDIR}/tools ${WORKDIR}/upload
  # get appid and other keys via ec2 tags. region returns AZ at the end, so we need to crop it
  # not available during INIT when run as part of user-data????
  # APPID=$(aws ec2 describe-tags --filters "Name=resource-id,Values=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)" \
  #    "Name=key,Values=appid" --output=json  | jq -r .Tags[0].Value)
  AWS_REGION=$(curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone|sed 's/[a-z]$//')
  aws configure set default.region $AWS_REGION
  aws configure set region $AWS_REGION
  logit "APPID=$APPID AWS_REGION=$AWS_REGION"

  # ${APPID,,} = make lowercase
  grep -q -e  "^alias ${APPID,,}=" ~/.bashrc || echo "alias ${APPID,,}=~/appctl.sh" >>.bashrc
  grep -q -e  "^alias appctl=" ~/.bashrc || echo "alias appctl=~/appctl.sh" >>.bashrc
  grep -q -e  "^alias l=" ~/.bashrc || echo "alias l='ls -aCF'" >>.bashrc
  grep -q  "/usr/bin/fortune" ~/.bashrc || \
    echo 'echo "$-" | grep i > /dev/null && [ -x /usr/bin/fortune ] && /usr/bin/fortune' >>.bashrc
  # todo git config user.name and user.email
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
  aws s3 sync s3://${BUCKET_NAME}/deploy ${WORKDIR} --exclude "*/*"
  chmod ugo+x ${WORKDIR}/${SCRIPT}
fi

# init daily cron jobs
if [[ "$*" == *init-cron* ]] || [[ "$*" == *all* ]]; then
  logit "Setting up scheduled tasks  in /etc/cron.daily"
  # renew cert if it's due
  sudo bash -c "cat >/etc/cron.daily/renew-cert" <<-'EOF'
/home/ec2-user/appctl.sh renew-cert >>/home/ec2-user/logs/renew-cert.log 2>&1
EOF

  # backup to s3 or local fs
  sudo bash -c "cat >/etc/cron.daily/remindabot" <<-'EOF'
/home/ec2-user/appctl.sh backup-db >>/home/ec2-user/logs/backup-db.log 2>&1
/home/ec2-user/appctl.sh backup-s3 >>/home/ec2-user/logs/backup-data.log 2>&1
EOF

  # docker cleanup
  sudo bash -c "cat >/etc/cron.daily/docker-prune" <<-'EOF'
docker system prune -f >>/home/ec2-user/logs/docker-prune.log 2>&1
EOF

  # remindabot
  sudo bash -c "cat >/etc/cron.daily/remindabot" <<-'EOF'
/home/ec2-user/tools/remindabot -envfile=/home/ec2-user/.env >>/home/ec2-user/logs/remindabot.log 2>&1
EOF

  # generic loop to make sure everything cronscript is executable
  for SCRIPT in backup-all renew-cert docker-prune remindabot; do sudo chmod ugo+x /etc/cron.daily/${SCRIPT}; done
fi

# trigger regular database and s3 bucket backups
## todo cleanup older dumps locally and s3 (via lifecyle rule), use variables for db host and appuser
if [[ "$*" == *backup-db* ]]; then
  # https://docs.elephantsql.com/elephantsql_api.html
  logit "Trigger PostgresDB for db=$DB_USERNAME via elephantsql API" # db username = dbname
  publish "startjob:backup-db" "Backup PostgresDB for db=$DB_USERNAME"
  curl -sS -i -u :${DB_API_KEY} https://api.elephantsql.com/api/backup -d "db=$DB_USERNAME"
  mkdir -p ${WORKDIR}/backup/db
  dumpfile=${WORKDIR}/backup/db/${DB_USERNAME}_$(date +"%Y-%m-%d-at-%H-%M-%S").sql
  dumpfile_latest=${WORKDIR}/backup/db/${APPID}_latest.dump
  logit "Creating local backup $dumpfile + upload to s3://${BUCKET_NAME}"
  PGPASSWORD=$DB_PASSWORD pg_dump -h balarama.db.elephantsql.com -U $DB_USERNAME $DB_USERNAME >$dumpfile
  aws s3 cp --storage-class STANDARD_IA $dumpfile s3://${BUCKET_NAME}/backup/db/history/$(basename $dumpfile)
  logit "Creating custom formatted latest backup $dumpfile_latest + upload to s3://${BUCKET_NAME}"
  PGPASSWORD=$DB_PASSWORD pg_dump -h balarama.db.elephantsql.com -U $DB_USERNAME $DB_USERNAME -Z2 -Fc > $dumpfile_latest
  aws s3 cp --storage-class STANDARD_IA $dumpfile_latest s3://${BUCKET_NAME}/backup/db/$(basename $dumpfile_latest)
  if isroot; then
    logit "Running with sudo, adapting local backup permissions"
    /usr/bin/chown -R ec2-user:ec2-user ${WORKDIR}/backup/db
  fi
fi

if [[ "$*" == *backup-s3* ]]; then
  logit "Backup app bucket s3://${BUCKET_NAME}/ to ${WORKDIR}/backup/"
  publish "startjob:backup-s3" "Backup app bucket s3://${BUCKET_NAME}/"
  aws s3 sync s3://${BUCKET_NAME} ${WORKDIR}/backup/s3 --exclude "deploy/*"
  if isroot; then
    logit "Running with sudo, adapting local backup permissions"
    /usr/bin/chown -R ec2-user:ec2-user ${WORKDIR}/backup/s3
  fi
fi

# renew certbot certificate if it's close to expiry date
if [[ "$*" == *renew-cert* ]] || [[ "$*" == *all* ]]; then
  logit "Deploy and renew SSL Certificates"
  publish "startjob:renew-cert" "Running certbot for ${CERTBOT_DOMAIN_STR} "

  CERTBOT_ADD_ARGS="" # use --dry-run to simulate cerbot interaction
  if docker ps --no-trunc -f name=^/${APPID}-ui$ |grep -q ${APPID}; then
    echo ${APPID}-ui is up, adding tempory shut down hook for cerbot renew
    set -x
    sudo --preserve-env=WORKDIR certbot --standalone -m ${CERTBOT_MAIL} --agree-tos --expand --redirect -n ${CERTBOT_DOMAIN_STR} \
         --pre-hook "docker-compose --no-ansi --file ${WORKDIR}/docker-compose.yml stop ${APPID}-ui" \
         --post-hook "docker-compose --no-ansi --file ${WORKDIR}/docker-compose.yml start ${APPID}-ui" \
         ${CERTBOT_ADD_ARGS} certonly
    set +x
  else
    echo ${APPID}-ui is down or not yet installed, cerbot can take safely over port 80
    sudo --preserve-env=WORKDIR certbot --standalone -m ${CERTBOT_MAIL} --agree-tos --expand --redirect -n ${CERTBOT_DOMAIN_STR} \
         ${CERTBOT_ADD_ARGS} certonly
  fi

  # if files relevant to letsencrypt changed, trigger backup update
  if sudo find /etc/letsencrypt/ -type f -mtime -1 |grep -q "."; then
    logit "Files in /etc/letsencrypt changes, trigger backup"
    sudo tar -C /etc -zcf /tmp/letsencrypt.tar.gz letsencrypt
    sudo aws s3 cp --sse=AES256 /tmp/letsencrypt.tar.gz s3://${BUCKET_NAME}/backup/letsencrypt.tar.gz
    sudo rm -f /tmp/letsencrypt.tar.gz
  else
    logit "Files in /etc/letsencrypt are unchanged, skip backup"
  fi
fi

# antora docs
if [[ "$*" == *deploy-docs* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying Antora docs CLEAN FIRST"
  set -x
  rm -rf ${WORKDIR}/docs/*
  aws s3 sync --delete s3://${BUCKET_NAME}/deploy/docs ${WORKDIR}/docs/
  set +x
fi

# api deployment
if [[ "$*" == *deploy-api* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying API Backend"
  # pull recent docker images from dockerhub
  docker pull ${DOCKER_USER}/${APPID}-api:${API_VERSION}
  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach ${APPID}-api
fi

# zu deployment
if [[ "$*" == *deploy-ui* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying UI Frontend"
  docker pull ${DOCKER_USER}/${APPID}-ui:${UI_VERSION}
  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach ${APPID}-ui
fi

# golang SQS Poller and other tools ....
if [[ "$*" == *deploy-tools* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying healthbells"
  docker pull ${DOCKER_USER}/${APPID}-tools:latest
  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach healthbells
  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach imagine

  logit "Extracing tools from docker image to host"
  docker cp $(docker create --rm ${DOCKER_USER}/${APPID}-tools:latest):/tools/ /home/ec2-user/
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

## if target required docker-compose interaction, show processes
if [[ "$*" == *ui* ]] ||  [[ "$*" == *api* ]] || [[ "$*" == *all* ]]; then
  docker ps
fi

## display usage
if [[ "$*" == *help* ]]; then
    echo "Usage: $SCRIPT [target]"
    echo
    echo "Targets:"
    echo "  all          Runs all targets"
    echo "  setup        Setup config, directories etc."
    echo "  update       Update myself and docker-compose config"
    echo "  deploy-ui    Deploys Angular UI"
    echo "  deploy-api   Deploys Spring Boot API"
    echo "  deploy-docs  Deploys Antora Docs"
    echo "  deploy-tools Deploys tools such as sqs-poller"
    echo "  renew-cert   Deploys and renews SSL certificate"
    echo "  init-cron    Init Cronjobs"
    echo "  backup-db    Backup Database"
    echo "  backup-s3    Backup S3 Databucket"
    echo "  help         This help"
    echo
fi
