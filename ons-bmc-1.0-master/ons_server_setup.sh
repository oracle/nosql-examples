#!/bin/bash

#  ons_server_setup.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

TMP=/tmp/ons
mkdir -p $TMP

helper_hosts=""
security="off"

while [[ $# -gt 1 ]]; do
	case $1 in
	-helper-hosts)
		helper_hosts="$2"; shift 2;;
	--security)
		security="$2"; shift 2;;
	*)
		shift;;
	esac
done

# install jdk

JDKRPM=`find $TMP -maxdepth 1 -name jdk*rpm`
sudo rpm -ivh $JDKRPM

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

if [ ! -z $helper_hosts ]; then
	if [ "$security" == "off" ]; then
		echo 'alias sql="java -jar $KVHOME/lib/sql.jar -helper-hosts $helper_hosts"' | sed s/\$helper_hosts/$helper_hosts/ >> ~/.bashrc
	else
		echo 'alias sql="java -jar $KVHOME/lib/sql.jar -helper-hosts $helper_hosts -security $KVROOT/security/client.security"' | sed s/\$helper_hosts/$helper_hosts/ >> ~/.bashrc
	fi
fi

exit 0


