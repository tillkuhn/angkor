#!/usr/bin/env bash
echo "Running cloud-init custom script ..."
sudo yum update -y
sudo amazon-linux-extras install java-openjdk11 -y
sudo amazon-linux-extras install nginx1 -y
sudo systemctl enable nginx
sudo systemctl stop nginx
sudo wget -r --no-parent -A 'epel-release-*.rpm' http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/
sudo rpm -Uvh dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-*.rpm
sudo yum-config-manager --enable epel*
sudo yum install -y certbot python2-certbot-nginx unzip
sudo mkdir -p ${appdir}
sudo unzip -o ${appdir}/webapp.zip -d /usr/share/nginx/html
if [ -d /etc/letsencrypt/live ]; then
  echo "/etc/letsencrypt already exists with contend, nothing do do"
elif aws s3api head-object --bucket ${bucket_name} --key letsencrypt/letsencrypt.tar.gz; then
  echo "local /etc/letsencrypt missing, downloading letsencrypt config from sr"
  sudo aws s3 cp s3://${bucket_name}/letsencrypt/letsencrypt.tar.gz ${appdir}/
  cd /etc
  sudo tar -xvf ${appdir}/letsencrypt.tar.gz
else
  echo "not local or remote letsencrypt  nginx config found, new one will be requested"
fi
sudo certbot --nginx -m ${certbot_mail} --agree-tos --redirect -n -d ${domain_name}
echo "Sync letsencrypt with s3"
sudo tar -C /etc -zcf /tmp/letsencrypt.tar.gz letsencrypt
sudo aws s3 cp --sse=AES256 /tmp/letsencrypt.tar.gz s3://${bucket_name}/letsencrypt/letsencrypt.tar.gz

