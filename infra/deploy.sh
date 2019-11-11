#!/usr/bin/env bash
ENVSCRIPT=$(dirname ${BASH_SOURCE[0]})/bin/setenv.sh
if [ ! -f $ENVSCRIPT ]; then
   echo "$ENVSCRIPT not found"
   exit 1
fi
. $ENVSCRIPT

yarn build
aws s3 sync ./dist/ $S3_WEBAPP --region ${AWS_REGION} --delete --size-only --profile ${AWS_PROFILE}
## size-only is not good for index.html as the size may not change but the checksum of included scripts does
aws s3 cp ./dist/index.html ${S3_WEBAPP}/index.html --region ${AWS_REGION} --profile ${AWS_PROFILE}
