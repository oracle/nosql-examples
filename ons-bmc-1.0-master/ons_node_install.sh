#!/bin/bash

#  ons_node_install.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

TMP=/tmp/ons
mkdir -p $TMP

security="off"
store=""

while [[ $# -gt 1 ]]; do
	case $1 in
	--security)
		security=$2; shift 2;;
	--store)
		store=$2; shift 2;;
	--username)
		username=$2; shift 2;;
	*)
		shift;;
	esac
done

>$TMP/fstab

# format drives, install file system and mount

drives=`sudo fdisk -l | grep nvme | sort | cut -f 2 -d ' ' | sed 's/://g'`

if [ -z "$drives" ]; then
	drives=`sudo fdisk -l | grep sd[b-e] | sort | cut -f 2 -d ' ' | sed 's/://g'`
fi

for drive in $drives; do
	echo -e "n\np\n1\n\n\nw" | sudo fdisk -u -c $drive
	sudo mkfs.ext4 -F $drive
	mount_dir=`echo $drive | sed 's/dev/ons/g'`
	sudo mkdir -p $mount_dir
	sudo mount $drive $mount_dir
	sudo chown opc:opc $mount_dir
	printf "$drive\t$mount_dir\text4\tdefaults\t0\t0\n" >> $TMP/fstab
done

sudo su -c "cat $TMP/fstab >> /etc/fstab"

# configure network

version_pattern="([[:digit:]]\.[[:digit:]])"
[[ `cat /etc/redhat-release` =~ $version_pattern ]] && rhrel_version="${BASH_REMATCH[1]}"

if [[ $rhrel_version > "7" ]]; then
	sudo firewall-cmd --permanent --zone=public --add-rich-rule=’rule family="ipv4" source address="10.0.0.0/27" port protocol="tcp" port="5000-5050" accept’
else
	sudo /sbin/iptables -D FORWARD -j REJECT --reject-with icmp-host-prohibited
	sudo /sbin/iptables -D INPUT -j REJECT --reject-with icmp-host-prohibited
	sudo service iptables status
fi

# install ntp

sudo yum -y install ntp

# configure ntp

sudo sed -i 's/^OPTIONS=\"-u/OPTIONS=\"-x -u/' /etc/sysconfig/ntpd # force time sync on start
sudo sed -i 's/^.*192.168.1.0 mask.*$/restrict 192.168.1.0 mask 255.255.255.0 nomodify notrap/' /etc/ntp.conf
sudo sed -i 's/^\(server [0-3]\).*$/\1.us.pool.ntp.org/g' /etc/ntp.conf

# start ntp and add to boot config

sudo service ntpd start
sudo chkconfig ntpd on

# install jdk

JDKRPM=`find $TMP -maxdepth 1 -name jdk*rpm`
sudo rpm -ivh $JDKRPM

# turn swap off

sudo sed -i.bak 's/\(^[^#].*swap.*$\)/#\1/g' /etc/fstab
sudo swapoff -a
free -m

# install nosql

INSTALL_DIR=/var/ons
sudo mkdir -p $INSTALL_DIR
cd $INSTALL_DIR

KVTAR=`find $TMP -maxdepth 1 -name kv*tar.gz`
sudo tar xf $KVTAR

KVDIR=`find $PWD -maxdepth 1 -name kv*`

cd
mkdir ons
cd ons
ln -s $KVDIR KVHOME
mkdir KVROOT

java -Xmx256m -Xms256m -jar KVHOME/lib/kvclient.jar

KVHOME=$PWD/KVHOME
KVROOT=$PWD/KVROOT

echo "export KVHOME=$KVHOME" >> ~/.bashrc
echo "export KVROOT=$KVROOT" >> ~/.bashrc

if [ "$security" == "off" ]; then
	echo 'alias runadmin="java -Xmx256m -Xms256m -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host `hostname`"' >> ~/.bashrc
	echo 'alias sql="java -Xmx256m -Xms256m -jar $KVHOME/lib/sql.jar -helper-hosts `hostname`:5000 -store $store"' | sed s/\$store/$store/ >> ~/.bashrc
else
	echo 'alias runadmin="java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host `hostname` -security $KVROOT/security/client.security -store $store -username $username"' | sed s/\$store/$store/ | sed s/\$username/$username/ >> ~/.bashrc
	echo 'alias sql="java -jar $KVHOME/lib/sql.jar -helper-hosts `hostname`:5000 -store $store -security $KVROOT/security/client.security -username $username"' | sed s/\$store/$store/ | sed s/\$username/$username/ >> ~/.bashrc
fi

exit 0