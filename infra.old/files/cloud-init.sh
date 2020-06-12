#!/usr/bin/env bash
if [ "$EUID" -ne 0 ]
  then echo "Please run with sudo"
  exit
fi
echo "Running cloud-init custom script ..."
yum update -y
amazon-linux-extras install java-openjdk11 -y
amazon-linux-extras install nginx1 -y
wget -r --no-parent -A 'epel-release-*.rpm' http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/
rpm -Uvh dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-*.rpm
yum-config-manager --enable epel*
yum install -y certbot python2-certbot-nginx unzip

mkdir -p ${appdir}
unzip -o ${appdir}/webapp.zip -d /usr/share/nginx/html

if [ -d /etc/letsencrypt/live ]; then
  echo "/etc/letsencrypt already exists with contend, nothing do do"
elif aws s3api head-object --bucket ${bucket_name} --key letsencrypt/letsencrypt.tar.gz; then
  echo "local /etc/letsencrypt missing, downloading letsencrypt config from sr"
  aws s3 cp s3://${bucket_name}/letsencrypt/letsencrypt.tar.gz ${appdir}/
  cd /etc
  tar -xvf ${appdir}/letsencrypt.tar.gz
else
  echo "not local or remote letsencrypt  nginx config found, new one will be requested"
fi
echo "Launching nginx and certbot ...."
systemctl enable nginx
systemctl stop nginx
certbot --nginx -m ${certbot_mail} --agree-tos --redirect -n -d ${domain_name}

echo "Sync letsencrypt with s3"
tar -C /etc -zcf /tmp/letsencrypt.tar.gz letsencrypt
aws s3 cp --sse=AES256 /tmp/letsencrypt.tar.gz s3://${bucket_name}/letsencrypt/letsencrypt.tar.gz

echo "Replacing nginx.conf with enhanced version ..."
cp  ${appdir}/nginx.conf /etc/nginx/nginx.conf
# curl http://169.254.169.254/latest/user-data

##
echo "Registering ${appid}.service as systemd service ..."
cp  ${appdir}/${appid}.service /etc/systemd/system
systemctl enable ${appid}
systemctl start ${appid}
