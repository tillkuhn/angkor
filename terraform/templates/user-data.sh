#!/usr/bin/env bash
# ATTENTION!!! Changing user-data will trigger destruction
# and subsequent recreation of the underlying EC2 Instance
# check if not run via cloud init ...
if [ "$EUID" -ne 0 ]; then
  echo "[FATAL] Detected UID $UID, please run with sudo"
  exit
fi

## rule of thumb for < 2GB memory: Take memory * 2
SWAPSIZEMB=$(grep MemTotal /proc/meminfo | awk '$1 == "MemTotal:" {printf "%.0f", $2 / 512 }')
if [ ! -f /mnt/swapfile ]; then
  echo "[INFO] Enabling Swap Support with $${SWAPSIZEMB}MB"
  dd if=/dev/zero of=/mnt/swapfile bs=1M count=$${SWAPSIZEMB}
  chown root:root /mnt/swapfile
  chmod 600 /mnt/swapfile
  mkswap /mnt/swapfile
  swapon /mnt/swapfile ## to disable run swapoff -a
  swapon -a
else
  echo "[DEBUG] Swap already enabled with $${SWAPSIZEMB}MB"
fi
if ! egrep "^/mnt/swapfile" /etc/fstab >/dev/null; then
  echo "[INFO] creating fstab enty for swap"
  echo "/mnt/swapfile swap swap defaults 0 0" >>/etc/fstab
fi

# python, docker, docker-compose and other common packages
echo "[INFO] Updating yum packages and add deltarpm support"
yum -y -q update
yum -y -q install deltarpm

echo "[INFO] Installing python3 with pip and required developer packages for docker-compose"
yum -y -q install python37 python3-devel.$(uname -m) libpython3.7-dev libffi-devel openssl-devel make gcc
yum groupinstall -q -y "Development Tools"
python3 -m pip install --upgrade pip
python3 --version; python3 -m pip --version
echo "[INFO] Installing additional common python packages with pip3"
python3 -m pip install -q --disable-pip-version-check install flask boto3 pynacl

if [ ! -x /usr/bin/docker ]; then
  echo "[INFO] Installing docker"
  amazon-linux-extras install -y -q docker
  docker --version
  usermod -a -G docker ec2-user
  systemctl enable docker.service
  systemctl start docker.service
else
  echo "[INFO] docker-compose already installed"
fi

if [ ! -x /usr/bin/docker-compose ]; then
  echo "[INFO] Installing docker-compose with pip3"
  # https://github.com/docker/compose/issues/6831#issuecomment-829797181
  python3 -m pip install -q --disable-pip-version-check docker-compose
  chmod +x /usr/local/bin/docker-compose
  ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
  docker-compose --version
else
  echo "[INFO] docker-compose already installed"
fi

if [ ! -x /usr/bin/psql ]; then
  echo "[INFO] Installing postgresql11 with pg_dump"
  amazon-linux-extras install -y -q postgresql11
  psql --version
else
  echo "[INFO] postgresql11 already installed"
fi


# install common packages
echo "[INFO] Installing common packages letsencrypt, certbot, git"
# https://aws.amazon.com/de/premiumsupport/knowledge-center/ec2-enable-epel/
# https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/SSL-on-amazon-linux-2.html#letsencrypt
wget -q -r --no-parent -A 'epel-release-*.rpm' http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/
rpm -Uvh dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-*.rpm
yum-config-manager -q --enable epel* | grep "\[epel" # quiet is not quiet at all
yum install -y -q certbot unzip git jq lzop fortune-mod nmap-ncat

# certbot certonly --dry-run
echo "[INFO] Checking x1letsencrypt history status"
if [ -d /etc/letsencrypt/live ]; then
  echo "[INFO] /etc/letsencrypt already exists with content (wip: care for changed domain names)"
elif aws s3api head-object --bucket ${bucket_name} --key backup/letsencrypt.tar.gz; then
  echo "[INFO] local /etc/letsencrypt/live not found but s3 backup is availble, downloading archive"
  aws s3 cp s3://${bucket_name}/backup/letsencrypt.tar.gz /tmp/letsencrypt.tar.gz
  tar -C /etc -xvf /tmp/letsencrypt.tar.gz
else
  echo "[INFO] No local or s3 backed up letsencrypt config found, new one will be requested"
fi
if [ ! -f /etc/ssl/certs/dhparam.pem ]; then
  # https://scaron.info/blog/improve-your-nginx-ssl-configuration.html
  echo "[INFO] Generating /etc/ssl/certs/dhparam.pem with OpenSSL and stronger keysize. this could take a while"
  openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048 2>/dev/null # takes about 30s
fi

# setup app home directory with scripts and permissions
echo "[INFO] Setting up application home"
curl -sS http://169.254.169.254/latest/user-data >/home/ec2-user/user-data.sh
aws s3 cp s3://${bucket_name}/deploy/appctl.sh /home/ec2-user/appctl.sh
aws s3 cp s3://${bucket_name}/deploy/.env /home/ec2-user/.env
chmod ugo+x /home/ec2-user/appctl.sh
chown ec2-user:ec2-user /home/ec2-user/appctl.sh /home/ec2-user/user-data.sh

echo "[INFO] Cloud Init completed, running /home/ec2-user/appctl.sh all"
sudo -H -u ec2-user bash -c 'cd /home/ec2-user; ./appctl.sh all'
