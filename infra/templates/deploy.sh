#!/usr/bin/env bash
# variables in this file are substitued by terraform templates
# so you need to use double dollar signs ($$) to escape variables
SCRIPT=$(basename $${BASH_SOURCE[0]})
WORKDIR=$(dirname $${BASH_SOURCE[0]})

# pull recent file artifacts from s3
mkdir -p $${WORKDIR}/docs
aws s3 cp s3://${bucket_name}/deploy/$${SCRIPT} $${WORKDIR}/$${SCRIPT} # update myself
aws s3 cp s3://${bucket_name}/deploy/docker-compose.yml $${WORKDIR}/docker-compose.yml
aws s3 sync --delete s3://${bucket_name}/docs $${WORKDIR}/docs/

# pull recent docker images from dockerhub
docker pull ${docker_user}/${appid}-api:${api_version}
docker pull ${docker_user}/${appid}-ui:${ui_version}

# reload my selector
WORKDIR=$${WORKDIR} docker-compose --file $${WORKDIR}/docker-compose.yml up --detach
docker ps
chmod ugo+x $${WORKDIR}/$${SCRIPT}
