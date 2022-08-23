#!/bin/bash

#  ons_node_uninstall.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

TMP=/tmp/ons
mkdir -p $TMP

# kill nosql

java -Xmx256m -Xms256m -jar $KVHOME/lib/kvstore.jar stop -root $KVROOT

sleep 5

for proc in `ps aux | grep "opc.*java" | grep -v grep | awk '{ print $2 }'`; do
	echo "kill: $proc"
	kill -9 $proc
done

for dir in `df -h | grep nvme | awk '{print $NF}'`; do
   rm -rf $dir/rg*
done

# uninstall nosql

INSTALL_DIR=/var/ons
sudo rm -rf $INSTALL_DIR

rm -rf ~/ons

sed -i '/KVHOME/d' ~/.bashrc
sed -i '/KVROOT/d' ~/.bashrc
sed -i '/runadmin/d' ~/.bashrc
sed -i '/sql.jar/d' ~/.bashrc

sudo sed -i.bak '/^opc/d' /etc/security/limits.conf
sudo sed -i.bak 's/^[#]//g' /etc/fstab

# undo changes to /etc/hosts

sudo sed -i.bak '/#@dbhost/d' /etc/hosts

# unwind network changes

version_pattern="([[:digit:]]\.[[:digit:]])"
[[ `cat /etc/redhat-release` =~ $version_pattern ]] && rhrel_version="${BASH_REMATCH[1]}"

if [[ $rhrel_version < "7" ]]; then
	reject="reject-with icmp-host-prohibited"
	if [[ ! `sudo service iptables status` =~ $reject ]]; then
		sudo /sbin/iptables -A FORWARD -j REJECT --$reject
		sudo /sbin/iptables -A INPUT -j REJECT --$reject
	fi
fi

# unmount drives, remove mount directories and delete partitions

drives=`sudo fdisk -l | grep nvme | sort | cut -f 2 -d ' ' | sed 's/://g'`

if [ -z "$drives" ]; then
	drives=`sudo fdisk -l | grep sd[b-e] | sort | cut -f 2 -d ' ' | sed 's/://g'`
fi

for drive in $drives; do
	mount_dir=`echo $drive | sed 's/dev/ons/g'`
	sudo umount $mount_dir
	sudo rm -rf $mount_dir
	printf "d\nw" | sudo fdisk -u -c $drive
done

sudo sed -i.bak '/nvme/d' /etc/fstab

rm -f ~/.jline-oracle.kv*

exit 0