#!/bin/bash

#  ons_cluster_uninstall.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

DIR="${BASH_SOURCE%/*}"

if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
. "$DIR/_incl_.sh"

NODES=""

while [[ $# -gt 0 ]]; do
	case $1 in
	-z|--zone)
		ZONE="$2"; shift 2;;
	-s|--store)
		STORE="$2"; shift 2;;
	*)
		[ -z "$NODES" ] && NODES=$1 || NODES="$NODES $1"
		shift;;
	esac
done

# uninstall ons on all nodes

for node in $NODES
do
	$ssh_ opc@$node "mkdir -p $TMP/log"
	$scp_ $DIR/ons_node_uninstall.sh opc@$node:$TMP/
	$ssh_ -n -t opc@$node "$TMP/ons_node_uninstall.sh >> $TMP/log/ons_node_uninstall.log 2>&1" &
done

wait

# remove the flag that says we have a ons install

for node in $NODES
do
	$ssh_ opc@$node "rm -f $ons_install_flag"
done


exit 0
