#!/bin/bash

#  ons_server_install.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

DIR="${BASH_SOURCE%/*}"

if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
. "$DIR/_incl_.sh"

USAGE="Usage: $0 --server <ipaddr> --dbnodes <ipaddrs>"

if [[ $# -lt 4 ]]; then
	echo "$USAGE"
	exit 1
fi

security="off"
dbnodes=""

while [[ $# -gt 0 ]]; do
	case $1 in
	-s|--server)
		require_ipaddr $2
		server="$2"; shift 2;;
	-d|--dbnodes)
		shift
		while [[ $# -gt 0 ]]; do
			[[ ! "$1" =~ ^[0-9] ]] && break || require_ipaddr "$1"
			[ -z "$dbnodes" ] && dbnodes="$1" || dbnodes="$dbnodes $1"
			shift
		done;;
	*)
		echo "$USAGE"
		exit 1
	esac
done

if [ -z "$server" ] || [ -z "$dbnodes" ]; then
	echo "$USAGE"
	exit 1
fi

has_ons_install=`$ssh_ opc@$server "[ -f "$ons_install_flag" ] && echo true || echo false"`

if [ "$has_ons_install" == "false" ]; then
	$ssh_ opc@$server "touch $ons_install_flag"
else
	echo "It appears the server has an existing Oracle NoSQL installation"
	echo "Run ons_server_uninstall.sh to uninstall the previous installation then try again"
	exit 1
fi

# make /tmp/ons directory on server

$ssh_ opc@$server "mkdir -p $TMP/log"

# append database 'ip hostname' list to /etc/hosts on server

tmp_hosts="$TMP/hosts"
>$tmp_hosts

helper_hosts=""

for dbnode in $dbnodes
do
	host=`$ssh_ opc@$dbnode hostname`
	route=`$ssh_ opc@$dbnode "sudo ip route get 1 | head -1"`
	ip=`echo $route | awk '{print \$NF;exit}'`
	echo "$ip $host #@dbhost" >> $tmp_hosts
	[ -z "$helper_hosts" ] && helper_hosts="$host:5000" || helper_hosts="$helper_hosts,$host:5000"
done

$scp_ -q $tmp_hosts opc@$server:$TMP/
hosts_copy="cat $tmp_hosts >> /etc/hosts"
$ssh_ opc@$server "sudo su -c '$hosts_copy'"

# copy jdk .rpm and nosql .tar.gz to server

jdk_rpm=`find $DIR -maxdepth 1 -name jdk*rpm`

if [ ! -f "$jdk_rpm" ]; then
	echo "jdk rpm not found!"
	exit 1
fi

ons_tar=`find $DIR -maxdepth 1 -name kv*tar.gz`

if [ ! -f "$ons_tar" ]; then
	echo "kv tar.gz not found!"
	exit 1
fi

has_jdk=`$ssh_ opc@$server "[ -f $TMP/$jdk_rpm ] && echo true || echo false"`

if [ "$has_jdk" == "false" ]; then
	$scp_ -q $jdk_rpm opc@$server:$TMP/ &
fi

has_ons=`$ssh_ opc@$server "[ -f $TMP/$ons_tar ] && echo true || echo false"`

if [ "$has_ons" == "false" ]; then
	$scp_ -q $ons_tar opc@$server:$TMP/ &
fi

# wait for file copies to finish

wait

# copy server setup file and run it

admin_node=`echo $dbnodes | awk '{ print $1 }'`
admin_host=`$ssh_ opc@$admin_node "hostname"`
admin_security_dir=`$ssh_ opc@$admin_node "find $KVROOT -name security"`

if [[ "$admin_security_dir" =~ security$ ]]; then
	security="on"
fi

$scp_ $DIR/ons_server_setup.sh opc@$server:$TMP/
$ssh_ opc@$server "$TMP/ons_server_setup.sh -helper-hosts $helper_hosts --security $security"

if [ "$security" == "on" ]; then
	rm -rf $DIR/security
	$scp_ -q -r opc@$admin_node:$admin_security_dir $DIR/
	$scp_ -r $DIR/security opc@$server:ons/KVROOT/
	rm -rf $DIR/security
fi

exit 0


