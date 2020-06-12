 #! /bin/bash
sudo aws s3 sync s3://${bucket_name}/deploy ${appdir}
chmod ugo+x ${appdir}/cloud-init.sh
${appdir}/cloud-init.sh
