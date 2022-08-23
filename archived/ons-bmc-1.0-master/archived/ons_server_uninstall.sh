#!/bin/bash

#  ons_server_uninstall.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

DIR="${BASH_SOURCE%/*}"

if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
. "$DIR/_incl_.sh"

USAGE="Usage: $0 --server <ipaddr>"

if [[ $# -lt 2 ]]; then
	echo "$USAGE"
	exit 1
fi

while [[ $# -gt 0 ]]; do
	case $1 in
	-s|--server)
		server="$2"; shift 2;;
	*)
		echo "$USAGE"
		exit 1
	esac
done

if [ -z "$server" ]; then
	echo "$USAGE"
	exit 1
fi

$ssh_ opc@$server "mkdir -p $TMP"
$scp_ $DIR/ons_server_unwind.sh opc@$server:$TMP/
$ssh_ opc@$server $TMP/ons_server_unwind.sh

$ssh_ opc@$server "rm -f $ons_install_flag"

exit 0
