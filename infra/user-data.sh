#!/usr/bin/env bash
# ATTENTION!!! Chaning user-data will result in destroy/recreate of the EC2 Instance

sudo yum update -y
sudo amazon-linux-extras install docker -y
sudo systemctl start docker.service
sudo usermod -a -G docker ec2-user

## rule of thumb for < 2GB memory: Take memory * 2
SWAPSIZEMB=$(grep MemTotal /proc/meminfo | awk '$1 == "MemTotal:" {printf "%.0f", $2 / 512 }')
if [ ! -f /mnt/swapfile ]; then
    echo "[INFO] Enabling Swap Support with ${SWAPSIZEMB}MB"
    dd if=/dev/zero of=/mnt/swapfile bs=1M count=${SWAPSIZEMB}
    chown root:root /mnt/swapfile
    chmod 600 /mnt/swapfile
    mkswap /mnt/swapfile
    swapon /mnt/swapfile ## to disable run swapoff -a
    swapon -a
else
    echo "[DEBUG] Swap already enabled with ${SWAPSIZEMB}MB"
fi
if ! egrep "^/mnt/swapfile" /etc/fstab >/dev/null; then
    echo "[INFO] creating fstab enty for swap"
    echo "/mnt/swapfile swap swap defaults 0 0" >>/etc/fstab
fi
