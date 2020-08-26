#!/usr/bin/env bash
# variables in this file are substitued by terraform templates
# so you need to use double dollar signs ($$) to escape variables
SCRIPT=$(basename $${BASH_SOURCE[0]})

logit() {
  printf "%(%Y-%m-%d %T)T %s\n" -1 "$1"
}

if [ $# -lt 1 ]; then
    set -- help # display help if called w/o args
fi

# common start
export WORKDIR=$(dirname $${BASH_SOURCE[0]})
mkdir -p $${WORKDIR}/docs $${WORKDIR}/logs $${WORKDIR}/backup

# pull file artifacts needed for all targets from s3
if [[ "$*" == *update* ]] || [[ "$*" == *all* ]]; then
  logit "Updating docker-compose and script artifacts including myself"
  aws s3 cp s3://${bucket_name}/deploy/$${SCRIPT} $${WORKDIR}/$${SCRIPT} # update myself
  aws s3 cp s3://${bucket_name}/deploy/docker-compose.yml $${WORKDIR}/docker-compose.yml
  chmod ugo+x $${WORKDIR}/$${SCRIPT}
fi

# init cron daily jobs
if [[ "$*" == *init-cron* ]] || [[ "$*" == *all* ]]; then
  logit "Setting up scheduled tasks  in /etc/cron.daily"
  sudo bash -c "cat >/etc/cron.daily/renew-cert" <<-'EOF'
/home/ec2-user/deploy.sh renew-cert >>/home/ec2-user/logs/renew-cert.log 2>&1
EOF
  sudo chmod 755 /etc/cron.daily/renew-cert

  sudo bash -c "cat >/etc/cron.daily/backup-db" <<-'EOF'
/home/ec2-user/deploy.sh backup-db >>/home/ec2-user/logs/backup-db.log 2>&1
EOF

  sudo bash -c "cat >/etc/cron.daily/docker-prune" <<-'EOF'
docker system prune -f >>/home/ec2-user/logs/docker-prune.log 2>&1
EOF

  for SCRIPT in backup-db renew-cert docker-prune; do sudo chmod 755 /etc/cron.daily/$${SCRIPT}; done
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
         --pre-hook "docker-compose --no-ansi --file $${WORKDIR}/docker-compose.yml stop ${appid}-ui" \
         --post-hook "docker-compose --no-ansi --file $${WORKDIR}/docker-compose.yml start ${appid}-ui" \
         $${CERTBOT_ADD_ARGS} certonly
    set +x
  else
    echo ${appid}-ui is down or not yet installed, cerbot can take safely over port 80
    sudo --preserve-env=WORKDIR certbot --standalone -m ${certbot_mail} --agree-tos --expand --redirect -n ${certbot_domain_str}
         $${CERTBOT_ADD_ARGS} certonly
  fi

  # if files relevant to letsencrypt changed, trigger backup update
  if sudo find /etc/letsencrypt/ -mtime -1 |grep -q "."; then
    logit "Files in /etc/letsencrypt changes, trigger backup"
    sudo tar -C /etc -zcf /tmp/letsencrypt.tar.gz letsencrypt
    sudo aws s3 cp --sse=AES256 /tmp/letsencrypt.tar.gz s3://${bucket_name}/backup/letsencrypt.tar.gz
    sudo rm -f /tmp/letsencrypt.tar.gz
  else
    logit "Files in /etc/letsencrypt are unchanged, skip backup"
  fi

fi

# python or golang webhook
if [[ "$*" == *webhook* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying webhook"
  aws s3 cp s3://${bucket_name}/deploy/captain-hook.py $${WORKDIR}/captain-hook.py
  chmod ugo+x $${WORKDIR}/captain-hook.py
fi

# antora docs
if [[ "$*" == *docs* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying Antora docs"
  set -x
  aws s3 sync --delete s3://${bucket_name}/docs $${WORKDIR}/docs/
  set +x
fi

# api deployment
if [[ "$*" == *api* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying API Backend"
  # pull recent docker images from dockerhub
  docker pull ${docker_user}/${appid}-api:${api_version}
  docker-compose --file $${WORKDIR}/docker-compose.yml up --detach ${appid}-api
fi

if [[ "$*" == *ui* ]] || [[ "$*" == *all* ]]; then
  logit "Deploying UI Frontend"
  docker pull ${docker_user}/${appid}-ui:${ui_version}
  docker-compose --file $${WORKDIR}/docker-compose.yml up --detach ${appid}-ui
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
    echo "  all         Runs all targets"
    echo "  ui          Deploys Angular UI"
    echo "  api         Deploys Spring Boot API"
    echo "  docs        Deploys Antora Docs"
    echo "  webhook     Deploys Python Webhook"
    echo "  update      Update script and compose file"
    echo "  renew-cert  Deploys and renews SSL certificate"
    echo "  init-cron   Init Cronjobs"
    echo "  backup-db   Backup Database"
    echo "  help        This help"
    echo
fi
