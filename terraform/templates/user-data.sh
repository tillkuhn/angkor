#!/usr/bin/env bash
# ATTENTION!!! Changing user-data will trigger destruction
# and subsequent recreation of the underlying EC2 Instance
#
# TIP: To view the user-data on a running instance, simply type:
# cat /var/lib/cloud/instance/user-data.txt
#
# check if not running as root, resp. via cloud init ...
if [ "$EUID" -ne 0 ]; then
  echo "[FATAL] Detected UID $UID, please run with sudo"
  exit
fi

## rule of thumb for < 2GB memory: Take memory * 2
# shellcheck disable=SC2034
SWAP_SIZE_MB=$(grep MemTotal /proc/meminfo | awk '$1 == "MemTotal:" {printf "%.0f", $2 / 512 }')
if [ ! -f /mnt/swapfile ]; then
  echo "[INFO] Enabling Swap Support with $${SWAP_SIZE_MB}MB"
  # we need to use $$ notation, or terraform will consider it a template variable
  # shellcheck disable=SC1083
  dd if=/dev/zero of=/mnt/swapfile bs=1M count=$${SWAP_SIZE_MB}
  chown root:root /mnt/swapfile
  chmod 600 /mnt/swapfile
  mkswap /mnt/swapfile
  swapon /mnt/swapfile ## to disable, run swapoff -a
  swapon -a
else
  echo "[DEBUG] Swap already enabled with $${SWAP_SIZE_MB}MB"
fi
if ! grep -E "^/mnt/swapfile" /etc/fstab >/dev/null; then
  echo "[INFO] creating fstab entry for swap"
  echo "/mnt/swapfile swap swap defaults 0 0" >>/etc/fstab
fi

# python, docker, docker-compose and other common packages
echo "[INFO] Updating yum packages and add deltarpm support"
yum -y -q update
yum -y -q install deltarpm

echo "[INFO] Installing python3.8 with pip and required developer packages for docker-compose"
# make sure we get at least python3.8 since 3.7 is EOL:  amazon-linux-extras | grep python
# https://repost.aws/questions/QUtA3qNBaLSvWPfD5kFwI0_w/python-3-10-on-ec2-running-amazon-linux-2-and-the-openssl-upgrade-requirement
yum list installed python3 && yum remove -q -y python3 # remove existing python3 if installed
amazon-linux-extras install -y -q python3.8; rpm -ql python38; python3.8 --version

# install python3-devel.$(uname -m) would resets symlink to 3.8 back to 3.7 so we postpone it
echo "[INFO] Installing python developer packages + Development tools"
yum install -q -y "python3-devel.$(uname -m)" libpython3.8-dev libffi-devel openssl-devel make gcc
yum groupinstall -q -y "Development Tools"

echo "[INFO] Sym-linking python3 with new python3.8 version"
ln -fs /usr/bin/python3.8 /usr/bin/python3; ln -fs /usr/bin/pydoc3.8 /usr/bin/pydoc
python3 --version
# --root-user-action=ignore fails on older versions of pip, so we need to upgrade to ~25 first w/o this option
echo "[INFO] Installing / upgrading pip"
python3 -m pip install --upgrade pip
python3 -m pip --version

echo "[INFO] Installing additional common python packages with pip3"
python3 -m pip install -q --disable-pip-version-check --root-user-action=ignore flask boto3 pynacl
# 2024-08-14: Address docker-compose issue ImportError: urllib3 v2.0 only supports OpenSSL 1.1.1+, currently the 'ssl' module is compiled with 'OpenSSL 1.0.2k-fips  26 Jan 2017'. See:
# 1) See https://github.com/urllib3/urllib3/issues/3016 use ssl 1.1 (yum install openssl11 openssl11-devel) does not work
# 2) see https://stackoverflow.com/questions/76187256/importerror-urllib3-v2-0-only-supports-openssl-1-1-1-currently-the-ssl-modu downgrade url lib to <2.x (does work)
python3 -m pip uninstall -y --root-user-action=ignore urllib3
python3 -m pip install --root-user-action=ignore 'urllib3<2.0'
# 2024-08-14: END HACK

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
  echo "[INFO] Installing docker-compose by downloading binary docker-compose-$(uname -s)-$(uname -m)"
  # https://stackoverflow.com/a/65478517/4292075
  # https://gist.github.com/npearce/6f3c7826c7499587f00957fee62f8ee9
  # python3 -m pip install -q --disable-pip-version-check docker-compose   # https://github.com/docker/compose/issues/6831#issuecomment-829797181
  curl -sSL https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
  ls -l /usr/local/bin/docker-compose
  chmod +x /usr/local/bin/docker-compose
  ln -fs /usr/local/bin/docker-compose /usr/bin/docker-compose
  docker-compose --version
else
  echo "[INFO] docker-compose already installed in /usr/bin/docker-compose"
fi

# TODO PG 15 not yet available with amazon-linux-extras ...
# Workaround: https://dbastreet.com/?p=1503 (add e /etc/yum.repos.d/pgdg.repo)
# install lib https://download.postgresql.org/pub/repos/yum/15/redhat/rhel-7.10-aarch64/postgresql15-libs-15.5-1PGDG.rhel7.aarch64.rpm
# and on top: https://download.postgresql.org/pub/repos/yum/15/redhat/rhel-7.10-aarch64/postgresql15-15.5-1PGDG.rhel7.aarch64.rpm
if [ ! -x /usr/bin/psql ]; then
  pg_version=15
  echo "[INFO] Installing postgresql$pg_version including pg_dump"
  # amazon-linux-extras install -y -q postgresql$pg_version   ## only offers <15 versions (status: 2023-11-20)
  wget https://download.postgresql.org/pub/repos/yum/15/redhat/rhel-7.10-aarch64/postgresql15-libs-15.5-1PGDG.rhel7.aarch64.rpm
  wget https://download.postgresql.org/pub/repos/yum/15/redhat/rhel-7.10-aarch64/postgresql15-15.5-1PGDG.rhel7.aarch64.rpm
  yum install -q -y postgresql15-libs-15.5-1PGDG.rhel7.aarch64.rpm
  yum install -q -y postgresql15-15.5-1PGDG.rhel7.aarch64.rpm
  psql --version
else
  echo "[INFO] postgresql$pg_version already installed"
fi



# install common packages
echo "[INFO] Installing common packages letsencrypt, certbot, git"
# https://aws.amazon.com/de/premiumsupport/knowledge-center/ec2-enable-epel/
# https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/SSL-on-amazon-linux-2.html#letsencrypt
# 2024-10: this complicated option no longer worksm but apparently 'amazon-linux-extras install epel' does
# wget -q -r --no-parent -A 'epel-release-*.rpm' http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/
# rpm -Uvh dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-*.rpm
amazon-linux-extras  install -y -q epel
yum-config-manager -q --enable epel* | grep "\[epel" # quiet is not quiet at all
yum install -y -q certbot unzip git jq lzop fortune-mod nmap-ncat

# certbot certonly --dry-run
echo "[INFO] Checking x1letsencrypt history status"
# bucket_name is not assigned, but injected into the template
# shellcheck disable=SC2154
if [ -d /etc/letsencrypt/live ]; then
  echo "[INFO] /etc/letsencrypt already exists with content (wip: care for changed domain names)"
elif aws s3api head-object --bucket "${bucket_name}" --key backup/letsencrypt.tar.gz; then
  echo "[INFO] local /etc/letsencrypt/live not found but s3 backup is available, downloading archive"
  aws s3 cp "s3://${bucket_name}/backup/letsencrypt.tar.gz" /tmp/letsencrypt.tar.gz
  tar -C /etc -xvf /tmp/letsencrypt.tar.gz
else
  echo "[INFO] No local or s3 backed up letsencrypt config found, new one will be requested"
fi
if [ ! -f /etc/ssl/certs/dhparam.pem ]; then
  # https://scaron.info/blog/improve-your-nginx-ssl-configuration.html
  echo "[INFO] Generating /etc/ssl/certs/dhparam.pem with OpenSSL and stronger key-size. this could take a while"
  openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048 2>/dev/null # takes about 30s
fi

# setup app home directory with scripts and permissions
echo "[INFO] Setting up application home"
curl -sS http://169.254.169.254/latest/user-data >/home/ec2-user/user-data.sh
aws s3 cp "s3://${bucket_name}/deploy/appctl.sh" /home/ec2-user/appctl.sh
aws s3 cp "s3://${bucket_name}/deploy/.env_config" /home/ec2-user/.env_config
chmod ugo+x /home/ec2-user/appctl.sh
chown ec2-user:ec2-user /home/ec2-user/appctl.sh /home/ec2-user/user-data.sh

echo "[INFO] Cloud Init completed, running /home/ec2-user/appctl.sh setup, pull-secrets and deployments"
sudo -H -u ec2-user bash -c 'cd /home/ec2-user; ./appctl.sh setup pull-secrets; ./appctl.sh all'
