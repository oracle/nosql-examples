#!/bin/bash

TMP=/tmp/ons
mkdir -p $TMP

ons_install_flag="./.ons"

scp_="scp -p -o UserKnownHostsFile=/tmp/known_hosts -o StrictHostKeyChecking=no"
ssh_="ssh -o UserKnownHostsFile=/tmp/known_hosts -o StrictHostKeyChecking=no"

function require_ipaddr {
	byteval="([0]|[1-9][0-9]?|1[0-9]{2}|2[0-4][0-9]|25[0-5])"
	ipregex="^($byteval\.){3}($byteval)$"
	if [[ ! $1 =~ $ipregex ]]; then
		echo "$1 is not a valid IP address"
		exit 1
	fi
}

function require_passphrase {
	valid_passphrase="^[A-Za-z0-9!#%&()*+,./:;=_?-]{6,}$"
	if [[ ! -z "$1" ]] && [[ ! $1 =~ $valid_passphrase ]]; then
		echo "Passphrase requires at least 6 alpha-numeric/punctuation characters"
		exit 1
	fi
}

function require_username {
	valid_username="^[A-Za-z][A-Za-z0-9_]*$"
	if [[ ! -z "$1" ]] && [[ ! $1 =~ $valid_username ]]; then
		echo "Username must start with alpha and contain only alpha, numeric and underscore characters"
		exit 1
	fi
}

