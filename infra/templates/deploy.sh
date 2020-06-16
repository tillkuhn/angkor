#!/usr/bin/env bash
aws s3 cp s3://${bucket_name}/deploy/docker-compose.yml /home/ec2-user/docker-compose.yml
docker pull ${docker_user}/${appid}-api:${api_version}
docker pull ${docker_user}/${appid}-ui:${ui_version}
docker-compose --file /home/ec2-user/docker-compose.yml up --detach
docker ps
