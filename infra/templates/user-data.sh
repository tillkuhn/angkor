#!/usr/bin/env bash
# ATTENTION!!! Chaning user-data will result in destroy/recreate of the EC2 Instance

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

# docker and docker-compose
echo "[INFO] Update common packages"
yum -y -q update
yum -y -q install deltarpm

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
  echo "[INFO] Installing docker-compose"
  # docker-compose https://acloudxpert.com/how-to-install-docker-compose-on-amazon-linux-ami/
  curl -sS -L https://github.com/docker/compose/releases/download/1.21.0/docker-compose-`uname -s`-`uname -m` \
     | sudo tee /usr/local/bin/docker-compose > /dev/null
  chmod +x /usr/local/bin/docker-compose
  ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
  docker-compose --version
else
  echo "[INFO] docker-compose already installed"
fi

# certbot
echo "[INFO] Installing Certbot and requesting cert"
wget -q -r --no-parent -A 'epel-release-*.rpm' http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/
rpm -Uvh dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-*.rpm
yum-config-manager -q --enable epel* | grep "\[epel" # quiet is not quiet at all
yum install -y -q certbot unzip
# certbot certonly --dry-run
certbot --standalone --dry-run -m klaus@klaus.de --agree-tos --redirect -n -d hase1.timafe.net certonly
certbot certificates # verify

# setup app
sudo -H -u ec2-user bash -c "echo ${appid} >docker-compose.yml"
