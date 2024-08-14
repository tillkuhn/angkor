#!/usr/bin/env bash
# ATTENTION!!! Changing user-data will trigger destruction
# and subsequent recreation of the underlying EC2 Instance
# check if not run via cloud init ...
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

echo "[INFO] Installing python3 with pip and required developer packages for docker-compose"
yum -y -q install python37 "python3-devel.$(uname -m)" libpython3.7-dev libffi-devel openssl-devel make gcc
yum groupinstall -q -y "Development Tools"
python3 -m pip install --upgrade pip
python3 --version; python3 -m pip --version
echo "[INFO] Installing additional common python packages with pip3"
python3 -m pip install -q --disable-pip-version-check install flask boto3 pynacl
# 2024-08-14: Address docker-compose issue ImportError: urllib3 v2.0 only supports OpenSSL 1.1.1+, currently the 'ssl' module is compiled with 'OpenSSL 1.0.2k-fips  26 Jan 2017'. See:
# 1) See https://github.com/urllib3/urllib3/issues/3016 use ssl 1.1 (yum install openssl11 openssl11-devel) does not work
# 2) see https://stackoverflow.com/questions/76187256/importerror-urllib3-v2-0-only-supports-openssl-1-1-1-currently-the-ssl-modu downgrade url lib to <2.x (does work)
python3 -m pip uninstall urllib3
python3 -m pip install 'urllib3<2.0'
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
  echo "[INFO] Installing docker-compose with pip3"
  # https://github.com/docker/compose/issues/6831#issuecomment-829797181
  python3 -m pip install -q --disable-pip-version-check docker-compose
  chmod +x /usr/local/bin/docker-compose
  ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
  docker-compose --version
else
  echo "[INFO] docker-compose already installed"
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

# https://developer.hashicorp.com/vault/tutorials/hcp-vault-secrets-get-started/hcp-vault-secrets-install-cli
if [ ! -x /usr/bin/vlt ]; then
  echo "[INFO] Installing Install HCP Vault Secrets CLI (vlt)"
  curl -fsSL https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo | sudo tee /etc/yum.repos.d/hashicorp.repo
  sudo yum update
  sudo yum install vlt -y
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

echo "[INFO] Cloud Init completed, running /home/ec2-user/appctl.sh all"
sudo -H -u ec2-user bash -c 'cd /home/ec2-user; ./appctl.sh all'
