#!/usr/bin/env bash
# variables in this file are substitued by terraform templates
# so you need to use double dollar signs ($$) to escape variables
SCRIPT=$(basename $${BASH_SOURCE[0]})
export WORKDIR=$(dirname $${BASH_SOURCE[0]})
mkdir -p $${WORKDIR}/docs $${WORKDIR}/logs

if [ $# -lt 1 ]; then
    set -- help
fi

# common start
# pull file artifacts needed for all targets from s3
if [[ "$*" == *update* ]] || [[ "$*" == *all* ]]; then
  aws s3 cp s3://${bucket_name}/deploy/$${SCRIPT} $${WORKDIR}/$${SCRIPT} # update myself
  aws s3 cp s3://${bucket_name}/deploy/docker-compose.yml $${WORKDIR}/docker-compose.yml
  chmod ugo+x $${WORKDIR}/$${SCRIPT}
fi

# cerbot
if [[ "$*" == *cert* ]] || [[ "$*" == *all* ]]; then
  if docker ps --no-trunc -f name=^/${appid}-ui$ |grep -q ${appid}; then
    echo ${appid}-ui is up, adding tempory shut down hook for cerbot renew
    set -x
    sudo certbot --standalone -m ${certbot_mail} --agree-tos --expand --redirect -n ${certbot_domain_str} \
                 --pre-hook "docker-compose stop ${appid}-ui" --post-hook "docker-compose start ${appid}-ui" certonly
    set +x
  else
    echo ${appid}-ui is down or not yet installed, cerbot can take safely over port 80
    sudo certbot --standalone -m ${certbot_mail} --agree-tos --expand --redirect -n ${certbot_domain_str} $${certbot_hooks[@]} certonly
  fi
fi

# python webhook
if [[ "$*" == *webhook* ]] || [[ "$*" == *all* ]]; then
  aws s3 cp s3://${bucket_name}/deploy/captain-hook.py $${WORKDIR}/captain-hook.py
  chmod ugo+x $${WORKDIR}/captain-hook.py
fi

# antora docs
if [[ "$*" == *docs* ]] || [[ "$*" == *all* ]]; then
  set -x
  aws s3 sync --delete s3://${bucket_name}/docs $${WORKDIR}/docs/
  set +x
fi

# api deployment
if [[ "$*" == *api* ]] || [[ "$*" == *all* ]]; then
  # pull recent docker images from dockerhub
  docker pull ${docker_user}/${appid}-api:${api_version}
  docker-compose --file $${WORKDIR}/docker-compose.yml up --detach ${appid}-api
fi

if [[ "$*" == *ui* ]] || [[ "$*" == *all* ]]; then
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
    echo "  cert        Deploys and renews SSL certificate"
    echo "  webhook     Deploys Python Webhook"
    echo "  update      Update script and compose file"
    echo "  help        This help"
    echo
fi
