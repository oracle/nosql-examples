#!/bin/bash

#  ons_cluster_install.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

DIR="${BASH_SOURCE%/*}"

if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
. "$DIR/_incl_.sh"

USAGE="Usage: $0 –-zone <zoneid> --store <dbname> [-P <passphrase>] <ipaddrs>"

if [[ $# -lt 7 ]]; then
	echo "$USAGE"
	exit 1
fi

username="admin"
passphrase="$" # Command-line passphrase
capacity="$"
partitions="$"

NODES=""

while [[ $# -gt 0 ]]; do
	case $1 in
	-c|--capacity)
		capacity="$2"; shift 2;;
	-p|--partitions)
		partitions="$2"; shift 2;;
	-z|--zone)
		ZONE="$2"; shift 2;;
	-s|--store)
		STORE="$2"; shift 2;;
	-u|--username)
		username="$2";
		require_username $username 
		shift 2;;		
	-P)
		passphrase="$2";
		require_passphrase $passphrase 
		shift 2;;		
	*)
		require_ipaddr $1
		[ -z "$NODES" ] && NODES=$1 || NODES="$NODES $1"
		shift;;
	esac
done

if [ -z "$ZONE" ] || [ -z "$STORE" ] || [ -z "$NODES" ]; then
	echo "$USAGE"
	exit 1
fi

if [ `echo "$NODES" | wc -w` -lt 3 ]; then
	echo "Must have at least 3 storage nodes (IP addresses)"
	exit 1
fi

if [ "$passphrase" == "$" ]; then
	echo "Enter a passphrase to create a secure database."
	while true; do
		read -s -p "Enter passphrase (empty for no passphrase): " passphrase; echo
		require_passphrase $passphrase
		read -s -p "Enter same passphrase again: " same; echo
		if [ "$passphrase" != "$same" ]; then
			echo "Passphrases do not match.  Try again."
		else
			break
		fi
	done
fi

passphrase="$passphrase"
[ -z "$passphrase" ] && security="off" || security="on"

if [ "$security" == "on" ]; then
	read -p "Enter username ($username): " username_entered; echo
	[ ! -z "$username_entered" ] && username=$username_entered
	require_username $username
fi

admin_node=`echo $NODES | awk '{ print $1 }'`

>/tmp/known_hosts

hypervisor=`$ssh_ opc@$admin_node "cat /proc/cpuinfo | grep hypervisor"`
[ -z "$hypervisor" ] && hypervisor="false" || hypervisor="true"

drive_count=`$ssh_ opc@$admin_node "sudo fdisk -l | grep '^Disk /dev/nvme' | wc -l" 2>/dev/null`

if [ $((drive_count)) -eq 0 ]; then
	drive_count=`$ssh_ opc@$admin_node "sudo fdisk -l | grep '^Disk /dev/sd[b-e]' | wc -l" 2>/dev/null`
fi

if [ $((drive_count)) -eq 0 ]; then
	echo "no mountable drives found on $admin_node"
	exit 1
fi

if [ "$capacity" == "$" ] || [ $((capacity)) -lt 1 ] || [ $((capacity)) -gt $((drive_count)) ]; then
	while true; do
		capacity=$drive_count
		read -p "Enter capacity ($capacity): " capacity_entered; echo
		[ ! -z "$capacity_entered" ] && capacity=$capacity_entered
		if [ $((capacity)) -lt 1 ] || [ $((capacity)) -gt $((drive_count)) ]; then
			echo "Capacity must be in range 1-$drive_count.  Try again."
		else
			break
		fi
	done
fi

if [ "$partitions" == "$" ] || [ $((partitions)) -lt $((capacity)) ]; then
	while true; do
		partitions=$((capacity*30))
		read -p "Enter partitions ($partitions): " partitions_entered; echo
		[ ! -z "$partitions_entered" ] && partitions=$partitions_entered
		if [ $((partitions)) -lt $((capacity)) ]; then
			echo "Partitions must be >= $capacity.  Try again."
		else
			break
		fi
	done
fi

tmp_hosts="$TMP/hosts"
>$tmp_hosts

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

ons_version_pattern="([1-9]+).([0-9]+).([0-9]+)"
[[ "$ons_tar" =~ $ons_version_pattern ]] && major="${BASH_REMATCH[1]}" && minor="${BASH_REMATCH[2]}" && patch="${BASH_REMATCH[3]}"

if [[ $major -lt 4 || ($major -eq 4 && ($minor -lt 3 || ($minor -eq 3 && $patch -lt 11))) ]]; then
  echo "Oracle NoSQL Database version must be 4.3.11 or greater"
  exit 1
fi

# set the flag on each node that says we have an ons install

for node in $NODES
do
    has_ons_install=`$ssh_ opc@$node "[ -f $ons_install_flag ] && echo true || echo false"`
	if [ "$has_ons_install" == "false" ]; then
		$ssh_ opc@$node "touch $ons_install_flag"
	else
		echo "It appears the cluster has an existing Oracle NoSQL installation"
		echo "Run ons_cluster_uninstall.sh to uninstall the previous installation then try again"
		exit 1
	fi
done

# generate the hosts file that will be appended to /etc/hosts on all nodes

for node in $NODES
do
	$ssh_ opc@$node "mkdir -p $TMP/log"
	host=`$ssh_ opc@$node hostname`
	route=`$ssh_ opc@$node "sudo ip route get 1 | head -1"`
	ip=`echo $route | awk '{print \$NF;exit}'`
	echo "$ip $host #@dbhost" >> $tmp_hosts
	$scp_ $DIR/ons_node_init.sh opc@$node:$TMP/
	$ssh_ opc@$node "$TMP/ons_node_init.sh"
done

# copy the jdk .rpm and nosql .tar.gz files to all nodes

for node in $NODES
do
    has_jdk=`$ssh_ opc@$node "[ -f $TMP/$jdk_rpm ] && echo true || echo false"`
	if [ "$has_jdk" == "false" ]; then
		$scp_ -q $jdk_rpm opc@$node:$TMP/ &
	fi
    has_ons=`$ssh_ opc@$node "[ -f $TMP/$ons_tar ] && echo true || echo false"`
	if [ "$has_ons" == "false" ]; then
		$scp_ -q $ons_tar opc@$node:$TMP/ &
	fi
done

# wait for copy jobs to finish

wait

# copy files to all nodes and run node install jobs in parallel

for node in $NODES
do
	$scp_ $DIR/ons_node_install.sh $tmp_hosts opc@$node:$TMP/
	hosts_copy="cat $tmp_hosts >> /etc/hosts"
	$ssh_ opc@$node "sudo su -c '$hosts_copy'"
	$ssh_ -n -t opc@$node "$TMP/ons_node_install.sh --security $security --store $STORE --username $username >> $TMP/log/ons_node_install.log 2>&1" &
done

# wait for node install jobs to finish

wait

# run makebootconfig on all nodes: first node creates security directory, others get a copy

store_security="configure"

for node in $NODES
do
	$scp_ $DIR/ons_node_makeboot.sh opc@$node:$TMP/
	makeboot="$TMP/ons_node_makeboot.sh --capacity $capacity --security $security -P $passphrase -store-security $store_security"
	if [ $security == "off" ]; then
		$ssh_ opc@$node "$makeboot"
	else
		if [ $store_security == "configure" ]; then
			$ssh_ opc@$node "$makeboot"
			rm -rf $DIR/security
			$scp_ -r opc@$node:ons/KVROOT/security .
			store_security="enable"
		else
			$scp_ -r $DIR/security opc@$node:ons/KVROOT/
			$ssh_ opc@$node "$makeboot"
		fi
	fi
done

rm -rf $DIR/security

# start storage nodes

for node in $NODES
do
	$scp_ $DIR/ons_node_start.sh opc@$node:$TMP/
	$ssh_ opc@$node "$TMP/ons_node_start.sh"
done

# create deploy plan

plan="$TMP/plan"

cat > $plan <<EOF
configure -name $STORE
plan deploy-zone -name $ZONE -rf 3 -wait
EOF

admin_hostname=`$ssh_ opc@$admin_node hostname`
echo "plan deploy-sn -zn zn1 -host $admin_hostname -port 5000 -wait" >> $plan

POOL="$ZONE"Pool

cat >> $plan <<EOF
plan deploy-admin -sn sn1 -wait
pool create -name $POOL
pool join -name $POOL -sn sn1
EOF

num=1
for node in $NODES
do
	if [ "$node" != "$admin_node" ]; then
		host=`$ssh_ opc@$node hostname`
		echo "plan deploy-sn -zn zn1 -host $host -port 5000 -wait" >> $plan
		echo "plan deploy-admin -sn sn$num -wait" >> $plan
		echo "pool join -name $POOL -sn sn$num" >> $plan
	fi
	let "num++"
done

cat >> $plan <<EOF
topology create -name topo -pool $POOL -partitions $partitions
plan deploy-topology -name topo -wait
exit
EOF

# copy deploy plan to admin node and load it

$scp_ $DIR/ons_plan_load.sh $plan opc@$admin_node:$TMP/
$ssh_ opc@$admin_node "$TMP/ons_plan_load.sh --plan $plan --security $security --username $username --passphrase $passphrase"

exit 0