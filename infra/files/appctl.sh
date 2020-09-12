#!/usr/bin/env bash
SCRIPT=$(basename ${BASH_SOURCE[0]})
export WORKDIR=$(dirname ${BASH_SOURCE[0]})

logit() {
  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"
}

# no args? you need help
if [ $# -lt 1 ]; then
    set -- help # display help if called w/o args
fi

# source variables form .env
if [ -f ${WORKDIR}/.env ]; then
  logit "Loading environment from ${WORKDIR}/.env"
  set -a; source .env; set +a  # -a = auto export
else
  logit "environment file ${WORKDIR}/.env not found"
  exit 1
fi

# common setup
if [[ "$*" == *setup* ]] || [[ "$*" == *all* ]]; then
  logit "Performing common init taks"
  mkdir -p ${WORKDIR}/docs ${WORKDIR}/logs ${WORKDIR}/backup ${WORKDIR}/tools
  # get appid and other keys via ec2 tags. region returns AZ at the end, so we need to crop it
  APPID=$(aws ec2 describe-tags --filters "Name=resource-id,Values=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)" \
      "Name=key,Values=appid" --output=json  | jq -r .Tags[0].Value)
  AWS_REGION=$(curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone|sed 's/[a-z]$//')
  aws configure set default.region $AWS_REGION
  aws configure set region $AWS_REGION
  logit "APPID=$APPID AWS_REGION=$AWS_REGION"

  grep -q -e  "^alias ${APPID}=" ~/.bashrc || echo "alias ${APPID}=~/appctl.sh" >>.bashrc
  grep -q -e  "^alias appctl=" ~/.bashrc || echo "alias appctl=~/appctl.sh" >>.bashrc
  # todo git config user.name and user.email
  # todo aws configure set region eu-central-1

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
  aws s3 cp s3://${bucket_name}/deploy/${SCRIPT} ${WORKDIR}/${SCRIPT} # update myself
  aws s3 cp s3://${bucket_name}/deploy/docker-compose.yml ${WORKDIR}/docker-compose.yml
  aws s3 cp s3://${bucket_name}/deploy/.env ${WORKDIR}/.env
  chmod ugo+x ${WORKDIR}/${SCRIPT}
fi

# init cron daily jobs
if [[ "$*" == *init-cron* ]] || [[ "$*" == *all* ]]; then
  logit "Setting up scheduled tasks  in /etc/cron.daily"
  sudo bash -c "cat >/etc/cron.daily/renew-cert" <<-'EOF'
/home/ec2-user/appctl.sh renew-cert >>/home/ec2-user/logs/renew-cert.log 2>&1
EOF
  sudo chmod 755 /etc/cron.daily/renew-cert

  sudo bash -c "cat >/etc/cron.daily/backup-db" <<-'EOF'
/home/ec2-user/appctl.sh backup-db >>/home/ec2-user/logs/backup-db.log 2>&1
EOF

  sudo bash -c "cat >/etc/cron.daily/docker-prune" <<-'EOF'
docker system prune -f >>/home/ec2-user/logs/docker-prune.log 2>&1
EOF

  for SCRIPT in backup-db renew-cert docker-prune; do sudo chmod 755 /etc/cron.daily/${SCRIPT}; done
fi

# regular database backups
if [[ "$*" == *backup-db* ]]; then
  logit "Backup PostgresDB, to be implemented"
fi

# cerbot renew
if [[ "$*" == *renew-cert* ]] || [[ "$*" == *all* ]]; then
  logit "Deploy and renew SSL Certificates"
  CERTBOT_ADD_ARGS="" # use --dry-run to simulate cerbot interaction
  if docker ps --no-trunc -f name=^/${appid}-ui$ |grep -q ${appid}; then
    echo ${appid}-ui is up, adding tempory shut down hook for cerbot renew
    set -x
    sudo --preserve-env=WORKDIR certbot --standalone -m ${certbot_mail} --agree-tos --expand --redirect -n ${certbot_domain_str} \
         --pre-hook "docker-compose --no-ansi --file ${WORKDIR}/docker-compose.yml stop ${appid}-ui" \
         --post-hook "docker-compose --no-ansi --file ${WORKDIR}/docker-compose.yml start ${appid}-ui" \
         ${CERTBOT_ADD_ARGS} certonly
    set +x
  else
    echo ${appid}-ui is down or not yet installed, cerbot can take safely over port 80
    sudo --preserve-env=WORKDIR certbot --standalone -m ${certbot_mail} --agree-tos --expand --redirect -n ${certbot_domain_str} \
         ${CERTBOT_ADD_ARGS} certonly
  fi

  # if files relevant to letsencrypt changed, trigger backup update
  if sudo find /etc/letsencrypt/ -type f -mtime -1 |grep -q "."; then
    logit "Files in /etc/letsencrypt changes, trigger backup"
    sudo tar -C /etc -zcf /tmp/letsencrypt.tar.gz letsencrypt
    sudo aws s3 cp --sse=AES256 /tmp/letsencrypt.tar.gz s3://${bucket_name}/backup/letsencrypt.tar.gz
    sudo rm -f /tmp/letsencrypt.tar.gz
  else
    logit "Files in /etc/letsencrypt are unchanged, skip backup"
  fi

fi

# python or golang webhook
if [[ "$*" == *deploy-tools* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying tools"
  aws s3 cp s3://${bucket_name}/deploy/tools/* ${WORKDIR}/tools
  chmod ugo+x ${WORKDIR}/tools/*
fi

# antora docs
if [[ "$*" == *deploy-docs* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying Antora docs"
  set -x
  aws s3 sync --delete s3://${bucket_name}/deploy/docs ${WORKDIR}/docs/
  set +x
fi

# api deployment
if [[ "$*" == *deploy-api* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying API Backend"
  # pull recent docker images from dockerhub
  docker pull ${docker_user}/${appid}-api:${api_version}
  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach ${appid}-api
fi

if [[ "$*" == *deploy-ui* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying UI Frontend"
  docker pull ${docker_user}/${appid}-ui:${ui_version}
  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach ${appid}-ui
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
    echo "  help         This help"
    echo
fi
