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

# docker
echo "[INFO] Update packages and install + enable docker"
yum -y -q update
yum -y -q install deltarpm
amazon-linux-extras install -y -q docker
usermod -a -G docker ec2-user
systemctl enable docker.service
systemctl start docker.service

# certbot
echo "[INFO] Installing Certbot and requesting cert"
wget -q -r --no-parent -A 'epel-release-*.rpm' http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/
rpm -Uvh dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-*.rpm
yum-config-manager -q --enable epel*
yum install -y -q certbot unzip
# certbot certonly --dry-run
certbot --standalone --dry-run -m klaus@klaus.de --agree-tos --redirect -n -d hase1.timafe.net certonly

# setup app
sudo -H -u ec2-user bash -c "echo ${appid} >>docker-compose.yml"
