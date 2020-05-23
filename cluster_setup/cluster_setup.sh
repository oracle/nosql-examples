#!/usr/bin/env bash

# Copyright (C) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
#
# This file was distributed by Oracle as part of a version of Oracle NoSQL
# Database made available at:
#
# http://www.oracle.com/technetwork/database/database-technologies/nosqldb/downloads/index.html
#
# Please see the LICENSE file included in the top-level directory of the
# appropriate version of Oracle NoSQL Database for a copy of the license and
# additional information.

# script to help set up a basic functioning Oracle NoSQL cluster

# Prerequisites:
# - a local .tar.gz or .zip file of a reasonably current kv release (ex: kv-ee-19.5.11.tar.gz)
# - java8 or higher installed on target machine(s)
# - ssh access to target machine(s) from local machine

# A few notes about this script:
#
# It is designed to run will all versions of bash 3 or greater. There
# are significant features in bash 4+ that could help in this script,
# but some systems may not have bash 4+.
#
# It is intended for setup of a reasonably small cluster (less than 10 machines).
# Using this script to create a very large cluster may take a lot of time.
#

export TMPDIR=/tmp

trap "/bin/rm -f $TMPDIR/*.$$" exit

verbose=0
debug=0
force_overwrite=0
proxy_only=0
dotest=0
secure_store=1
autogen=0
test_vars=""

while [ "$1" != "" ] ; do
case "$1" in
    -v|--verbose)
    verbose=1
    ;;
    -d|--debug)
    debug=1
    verbose=1
    set -x
    ;;
    -f|--force)
    force_overwrite=1
    ;;
    -p|--proxy)
    proxy_only=1
    ;;
    -t|--test)
	# for internal testing
	dotest=1
	shift
	[ "$1" = "" ] && echo "Usage: -t test_vars_file" && exit 1
	test_vars="$1"
    ;;
    *)
        # unknown option
    ;;
esac
shift
done


# global host/ipaddr cache
declare -a hosts
declare -a ipaddrs

# location of saved values from previous run
SVARS=$TMPDIR/cluster_setup.vars
[ $dotest -eq 1 ] && SVARS=$TMPDIR/cluster_testing.vars

# check for inaccessible terminals db
skipclear=0
tput longname > /dev/null 2>&1
[ $? -ne 0 ] && skipclear=1

function clear_screen ()
{
	[ $debug -eq 1 ] && return
	[ $skipclear -eq 1 ] && return
	clear
}

function askcontinue ()
{
	defval=$1
	if [ "$defval" = "any" ] ; then
		echo ""
		echo -n "Hit <enter> to continue: "
		read yorn
		return
	fi
	echo ""
	echo -n "Continue? (y/n) [$defval]: "
	[ $dotest -eq 1 ] && return
	read yorn
	[ "$yorn" = "" ] && yorn=$defval
	[ "$yorn" != "y" -a "$yorn" != "Y" ] && exit 0
}

function save_vals ()
{
  echo "targzfile=$targzfile" > $SVARS
  echo "installdir=\"$installdir\"" >> $SVARS
  echo "storename=$storename" >> $SVARS
  echo "allhosts=\"$allhosts\"" >> $SVARS
  echo "sshuser=$sshuser" >> $SVARS
  echo "sshopts=\"$sshopts\"" >> $SVARS
  echo "all_data_paths=\"$all_data_paths\"" >> $SVARS
  echo "all_log_paths=\"$all_log_paths\"" >> $SVARS
  echo "startport=$startport" >> $SVARS
  echo "repfactor=$repfactor" >> $SVARS
  echo "proxyport=$proxyport" >> $SVARS
  echo "secure_store=$secure_store" >> $SVARS
  declare -p ipaddrs >> $SVARS 2>/dev/null
}

function write_words_to_file ()
{
	words="$1"
	file=$2

	/bin/rm -f $file
	for i in $words ; do
		echo $i >> $file
	done
}

# read and verify an integer input
# set global $intval to read-in value
function read_integer ()
{
	min=$1
	max=$2
	def=$3

	read intval
	[ "$intval" = "" ] && intval=$def
	[ "$intval" = "" ] && echo "Input must be an integer." && exit 1
	[[ $((intval)) != $intval ]] && echo "Input must be an integer." && exit 1
	[ $intval -lt $min -o $intval -gt $max ] && echo "Value must be between $min and $max." && exit 1

#	echo $intval
}

function get_prev_settings ()
{
	[ ! -s $SVARS ] && return
	echo ""
	echo -n "Use cached values from last program run? (y/n) [y]: "
	read yorn
	[ "$yorn" = "" ] && yorn=y
	[ "$yorn" != "y" -a "$yorn" != "Y" ] && /bin/rm -f $SVARS && return
	source $SVARS
	echo ""
	echo "Previous cached values imported. Values will show in brackets ([]) at"
	echo "the end of each prompt. Hit <enter> to use previous values."
	echo ""
	askcontinue any
}

function splash_screen ()
{
	clear_screen
	echo ""
	echo "This script is intended to help set up a small Oracle NoSQL cluster for"
	echo "testing and basic usage. The following requirements must already be met:"
	echo ""
	echo " 1) ssh access to host machine(s)"
	echo " 2) a downloaded Oracle NoSQL release .tar.gz or .zip file"
	echo " If not running in Oracle OCI environment, the following are also required:"
	echo "    3) disk/nvme data drives set up on host machine(s)"
	echo "    4) Java 8+ installed on host machine(s)"
	echo ""
	echo "Downloads can be obtained from"
	echo "https://www.oracle.com/database/technologies/nosql-database-server-downloads.html"
	echo ""
	askcontinue any
}

function defprompt ()
{
	val="$1"
	if [ "$val" = "" ] ; then
		echo -n ": "
	else
		echo -n " [$val]: "
	fi
}

function get_topdir ()
{
	local gzfile=$1
	local expjar=$2

	if [ ! -s $gzfile ] ; then
		echo "$gzfile does not exist or is inaccessible"
		exit 1
	fi
	if [[ $gzfile =~ \.zip$ ]] ; then
		topdir=$(unzip -l $gzfile | grep "/$expjar\$" | sed -e 's/^.* //' | awk -F/ '{print $1}')
	else
		topdir=$(gzip -dc $gzfile | tar tvf - | grep "/$expjar\$" | sed -e 's/^.* //' | awk -F/ '{print $1}'
)
	fi
	if [ "$topdir" = "" ] ; then
		echo "$gzfile does not contain a valid nosql release"
		exit 1
	fi
	echo $topdir
}

function clear_ipaddr_cache ()
{
	unset ipaddrs
	declare -a ipaddrs
}

# get ipaddr from cache.
# set global $cached_ipaddr to cached value.
function get_ipaddr_cached ()
{
	local host=$1
	cached_ipaddr=""

	# see if we have it cached
	local i=0
	while [ $i -lt ${#hosts[*]} ] ; do
		if [ "${hosts[$i]}" = "$host" ] ; then
			cached_ipaddr=${ipaddrs[$i]}
			return
		fi
		i=$(expr $i + 1)
	done
}

function get_targzfile ()
{
	clear_screen
	echo ""
	echo "Enter path to the locally downloaded Oracle NoSQL release "
	echo "tar.gz or zip file (ex: kv-ee-19.5.13.tar.gz)"
	echo ""
	echo -n "tar.gz/.zip file"
	defprompt $targzfile
	if [ $dotest -eq 0 ] ; then
		read -e tfile
		[ "$tfile" = "" -a "$targzfile" = "" ] && exit 1
		[ "$tfile" = "" ] && tfile=$targzfile
		targzfile=$tfile
	fi
	save_vals
	# expand "~", etc
	source $SVARS
	get_topdir "$targzfile" kvstore.jar
	targztop=$topdir
	is_enterprise=0
	is_old_community=0
	[[ $targzfile =~ kv-ee- ]] && is_enterprise=1
	[[ $targzfile =~ kv-ce-18 ]] && is_old_community=1
	save_vals
}

function get_storename ()
{
	clear_screen
	echo ""
	echo "Enter a name to use for the store. This should be a short, simple"
	echo "name with only alphanumeric characters. It will be used throughout"
	echo "the setup and later by connecting clients."
	echo ""
	[ "$storename" = "" ] && storename=DefaultStore
	sname=""
	while [ "$sname" = "" ] ; do
		echo -n "Store name"
		defprompt $storename
		read sname
		[ "$sname" = "" ] && sname=$storename
		xtra=$(echo "$sname" | tr -d '[:alnum:]')
		if [ "$xtra" != "" ] ; then
			echo ""
			echo "Store name cannot contain non-alphanumeric characters."
			echo ""
			sname=""
		fi
	done
	storename=$sname
	save_vals
}


function check_host_oci ()
{
	local host=$1

	# ssh into the host, see if we have unmounted nvmes
cat > $TMPDIR/nosql_check_oci.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x
export LANG=C

# sometimes users have PATH/etc in bash_profile and not in bashrc. Hmmm.
[ -s ~/.bashrc ] && source ~/.bashrc > /dev/null 2>&1
[ -s ~/.bash_profile ] && source ~/.bash_profile > /dev/null 2>&1

# Is this an OCI instance?
ls /etc/oci-util* > /dev/null 2>&1
[ \$? -ne 0 ] && exit 9

# check sudo
have_sudo=0
sudo -n echo ok > /dev/null 2>&1
[ \$? -ne 0 ] && exit 10


# what nvme drives are available, and are they already mounted?
drives=\$(sudo fdisk -l 2>/dev/null | grep nvme | sort | cut -f 2 -d ' ' | sed 's/://g')
[ "\$drives" = "" ] && exit 11

# if all are mounted, bail out
unmounted=""
for i in \$drives ; do
	mount | grep \$i > /dev/null 2>&1
	[ \$? -ne 0 ] && unmounted="\$unmounted \$i"
done
[ "\$unmounted" = "" ] && exit 12

echo -n "host_nvmes=\\"" > /tmp/nosql_drives
for i in \$unmounted ; do
	echo -n "\$i " >> /tmp/nosql_drives
done
echo "\\"" >> /tmp/nosql_drives

exit 0
EOT

	#echo "Checking on $host..."
	scp $sshopts $TMPDIR/nosql_check_oci.sh.$$ ${sshat}$host:nosql_check_oci.sh > /dev/null
	if [ $? -ne 0 ] ; then
		echo ""
		echo ""
		echo "There appears to be a problem with your ssh credentials."
		echo "Please verify that you can run the following command with"
		echo "no errors, then run this script again."
		echo ""
		echo "   ssh $sshopts ${sshat}$host \"echo ok\""
		echo ""
		exit 1
	fi
	is_oci_instance=0
	ssh $sshopts ${sshat}$host "chmod 755 ./nosql_check_oci.sh ; ./nosql_check_oci.sh ; ret=\$? ; rm -f ./nosql_check_oci.sh ; exit \$ret"
	ret=$?
	[ $ret -ge 10 -a $ret -le 12 ] && is_oci_instance=1
	[ $ret -ge 9 -a $ret -le 12 ] && return $ret
	if [ $ret -ne 0 ] ; then
		exit 1
	fi
	is_oci_instance=1
	scp $sshopts ${sshat}$host:/tmp/nosql_drives $TMPDIR/nosql_drives.$$ > /dev/null 2>&1
	if [ $? -ne 0 ] ; then
		nvme_drives=""
		#echo ""
		#echo "Error: can't collect NVMe drive data from $host"
		return 1
	fi
	ssh $sshopts ${sshat}$host "rm -f /tmp/nosql_drives" >/dev/null 2>&1

	source $TMPDIR/nosql_drives.$$
	/bin/rm -f $TMPDIR/nosql_drives.$$

	if [ "$nvme_drives" = "" ] ; then
		nvme_drives="$host_nvmes"
		return 0
	fi

	if [ "$host_nvmes" != "$nvme_drives" ] ; then
		nvme_drives=""
		#echo ""
		#echo "$host appears to have different NVMe drives than others. Skipping OCI checks."
		return 1
	fi

	return 0
}

function check_all_hosts_oci ()
{
	i=0
	while [ $i -lt $numhosts ] ; do
		host=${hosts[$i]}
		check_host_oci $host
		ret=$?
		[ $ret -eq 9 ] && return 1
		[ $ret -ne 0 ] && return 2
		i=$(expr $i + 1)
	done
	return 0
}

function mount_drives_on_host ()
{
	local host=$1
	local drives="$2"

cat > $TMPDIR/nosql_mount_nvmes.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x
export LANG=C

trap "/bin/rm -f /tmp/*.\$\$" exit

# sometimes users have PATH/etc in bash_profile and not in bashrc. Hmmm.
[ -s ~/.bashrc ] && source ~/.bashrc > /dev/null 2>&1
[ -s ~/.bash_profile ] && source ~/.bash_profile > /dev/null 2>&1

echo ""
echo "Setting up NVMe drives on $host..."

myid=\$(whoami)
/bin/rm -f /tmp/nosql_fstab
for drive in $drives ; do
	echo "  formatting and mounting \$drive..."
	echo -e "n\\np\\n1\\n\\n\\nw" | sudo fdisk -u -c \$drive > /tmp/nosql.\$\$ 2>&1
	[ \$? -ne 0 ] && cat /tmp/nosql.\$\$ && exit 1
	sudo mkfs.ext4 -F \$drive > /tmp/nosql.\$\$ 2>&1
	[ \$? -ne 0 ] && cat /tmp/nosql.\$\$ && exit 1
	mount_dir=\$(echo \$drive | sed 's#/dev#$installdir/data#')
	sudo mkdir -p \$mount_dir  > /tmp/nosql.\$\$ 2>&1
	[ \$? -ne 0 ] && cat /tmp/nosql.\$\$ && exit 1
	sudo mount \$drive \$mount_dir > /tmp/nosql.\$\$ 2>&1
	[ \$? -ne 0 ] && cat /tmp/nosql.\$\$ && exit 1
	sudo chown \$myid:\$myid \$mount_dir > /tmp/nosql.\$\$ 2>&1
	[ \$? -ne 0 ] && cat /tmp/nosql.\$\$ && exit 1
	printf "\$drive\\t\$mount_dir\\text4\\tdefaults\\t0\\t0\\n" >> /tmp/nosql_fstab
done
sudo /bin/bash -c "cat /tmp/nosql_fstab >> /etc/fstab"
/bin/rm -f /tmp/nosql_fstab

exit 0
EOT

	scp $sshopts $TMPDIR/nosql_mount_nvmes.sh.$$ ${sshat}$host:nosql_mount_nvmes.sh > /dev/null
	if [ $? -ne 0 ] ; then
		echo ""
		echo ""
		echo "There appears to be a problem with your ssh credentials."
		echo "Please verify that you can run the following command with"
		echo "no errors, then run this script again."
		echo ""
		echo "   ssh $sshopts ${sshat}$host \"echo ok\""
		echo ""
		exit 1
	fi
	ssh $sshopts ${sshat}$host "chmod 755 ./nosql_mount_nvmes.sh ; ./nosql_mount_nvmes.sh ; ret=\$? ; rm -f ./nosql_mount_nvmes.sh ; exit \$ret"
	return $?
}


function setup_raw_oci_drives ()
{
	# skip if we're just adding proxy setup
	[ $proxy_only -eq 1 ] && return
	# skip if we can tell we're not on oci
	[ "$sshuser" = "ec2-user" ] && return

	clear_screen
	echo ""
	echo -n "Checking if all target hosts are running in Oracle OCI..."
	check_all_hosts_oci
	ret=$?
	[ $ret -eq 1 ] && echo "no" && sleep 1 && return
	echo "yes"
	[ $ret -ne 0 -o "$nvme_drives" = "" ] && sleep 1 && return

	echo ""
	echo ""
	echo "It appears that the target hosts have raw, unmounted NVMe drives."
	echo ""
	echo "Would you like this program to attempt to format and mount them"
	echo "for use in the Oracle NoSQL cluster?"
	echo ""
	echo -n "Format and mount NVMe drives on all target hosts? (y/n): "
	read yorn
	if [ "$yorn" != "y" -a "$yorn" != "Y" ] ; then
		echo ""
		echo "Skipping NVMe mounting."
		echo ""
		sleep 1
		return
	fi

	local i=0
	while [ $i -lt $numhosts ] ; do
		host=${hosts[$i]}
		mount_drives_on_host $host "$nvme_drives"
		[ $? -ne 0 ] && exit 1
		i=$(expr $i + 1)
	done
	local dp=""
	for nvme in $nvme_drives ; do
		mount_dir=$(echo $nvme | sed "s#/dev#$installdir/data#")
		dp="$dp $mount_dir"
	done
	all_data_paths="$dp"
	echo ""
	echo "NVMe drives mounted successfully."
	echo ""
	echo "The following paths will be used for Oracle NoSQL data:"
	echo ""
	echo "$all_data_paths"
	echo ""
	skip_data_paths=1
	skip_log_paths=1
	askcontinue any
}


function setup_firewall_on_host ()
{
	local host=$1
	local ipbits=$2
	local startport=$3
	local endport=$4

	echo ""
	echo "Setting up firewall rules for ports ${startport}-${endport} on $host..."

cat > $TMPDIR/nosql_setup_firewall.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x
export LANG=C

trap "/bin/rm -f /tmp/*.\$\$" exit

# sometimes users have PATH/etc in bash_profile and not in bashrc. Hmmm.
[ -s ~/.bashrc ] && source ~/.bashrc > /dev/null 2>&1
[ -s ~/.bash_profile ] && source ~/.bash_profile > /dev/null 2>&1

sudo firewall-cmd --permanent --direct --passthrough ipv4 -I INPUT_ZONES_SOURCE -p tcp --dport $startport:$endport -s $ipbits -m conntrack --ctstate NEW,ESTABLISHED -j ACCEPT
[ \$? -ne 0 ] && exit 1
sudo firewall-cmd --direct --passthrough ipv4 -I INPUT_ZONES_SOURCE -p tcp --dport $startport:$endport -s $ipbits -m conntrack --ctstate NEW,ESTABLISHED -j ACCEPT

exit \$?
EOT

	scp $sshopts $TMPDIR/nosql_setup_firewall.sh.$$ ${sshat}$host:nosql_setup_firewall.sh > /dev/null
	if [ $? -ne 0 ] ; then
		echo ""
		echo ""
		echo "There appears to be a problem with your ssh credentials."
		echo "Please verify that you can run the following command with"
		echo "no errors, then run this script again."
		echo ""
		echo "   ssh $sshopts ${sshat}$host \"echo ok\""
		echo ""
		exit 1
	fi
	ssh $sshopts ${sshat}$host "chmod 755 ./nosql_setup_firewall.sh ; ./nosql_setup_firewall.sh ; ret=\$? ; rm -f ./nosql_setup_firewall.sh ; exit \$ret"
	return $?
}

function get_lowest_bitmask ()
{
	# determine smallest bitmask to use to get all hosts in one IP range
	/bin/rm -f $TMPDIR/ips.$$
	for desthost in ${hosts[*]} ; do
		get_ipaddr_cached $desthost
		echo "$cached_ipaddr" >> $TMPDIR/ips.$$
	done

    # don't allow a bitmask less than 16
    q1=($(cat $TMPDIR/ips.$$ | cut -d. -f 1 | sort -u))
    [ ${#q1[*]} -ne 1 ] && bitmask="" && return 1
    q2=($(cat $TMPDIR/ips.$$ | cut -d. -f 2 | sort -u))
    [ ${#q2[*]} -ne 1 ] && bitmask="" && return 1

    q3=($(cat $TMPDIR/ips.$$ | cut -d. -f 3 | sort -u))
    [ ${#q3[*]} -ne 1 ] && bitmask="$q1.$q2.0.0/16" && return 0
    bitmask="$q1.$q2.$q3.0/24"
    return 0
}

function setup_firewall_rules ()
{
	local startport=$1
	local endport=$2

	for desthost in ${hosts[*]} ; do
		setup_firewall_on_host $desthost $bitmask $startport $endport
	done
}


function get_installdir ()
{
	clear_screen
	echo ""
	echo "Oracle NoSQL programs installation:"
	echo ""
	echo "Enter the desired install directory absolute path for Oracle NoSQL"
	echo "programs on each host. Oracle suggests using /opt/oracle/nosql for"
	echo "this value. JAR files, scripts, etc. will be located here. You will"
	echo "be prompted in subsequent steps for the paths for NoSQL data and logs."
	echo ""
	echo "The directory will be created if it does not already exist."
	echo ""
	echo -n "Install path"
	[ "$installdir" = "" ] && installdir=/opt/oracle/nosql
	defprompt $installdir
	read -e idir
	[ "$idir" = "" -a "$installdir" = "" ] && exit 1
	[ "$idir" = "" ] && idir=$installdir
	installdir=$idir
	save_vals
}

function get_hosts ()
{
	clear_screen
	echo ""
	echo "Enter hostnames or IP addresses of the hosts you want to run Oracle"
	echo "NoSQL server processes on."
	echo "You need to have ssh access to these machines from where this"
	echo "program is running. Separate hostnames/IPs with spaces."
	echo ""
	echo -n "hosts/IPs"
	defprompt "$allhosts"
	read ahosts
	[ "$ahosts" = "" -a "$allhosts" = "" ] && exit 1
	[ "$ahosts" = "" ] && ahosts="$allhosts"
	[ "$ahosts" != "$allhosts" ] && clear_ipaddr_cache
	allhosts="$ahosts"
	hosts=($allhosts)
	numhosts=${#hosts[*]}
	[ $numhosts -eq 0 ] && exit 1

	echo ""
	echo "If ssh access to these hosts requires a specific username, enter"
	echo "that here. If not, just hit <enter>."
	echo ""
	echo -n "ssh username"
	defprompt $sshuser
	read suser
	[ "$suser" != "" ] && sshuser=$suser
	sshat=""
	[ "$sshuser" != "" ] && sshat="${sshuser}@"

	echo ""
	echo "If ssh access to these hosts requires any other ssh options, enter the"
	echo "ssh options, all on one line, here. For no options, just hit <enter>."
	echo ""
	echo "The most common use of ssh options is to supply an alternate keyfile"
	echo "path, with \"-i <path_to_key_file>\", but any valid ssh options can"
	echo "be entered here."
	echo ""
	echo -n "ssh options"
	defprompt "$sshopts"
	read -e sopts
	[ "$sopts" != "" ] && sshopts="$sopts"

	save_vals
}

function collect_host_data ()
{
	local host=$1
	local hostnum=$2

	# ssh into the host, collect all info needed.
cat > $TMPDIR/nosql_collect.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x
export LANG=C

# sometimes users have PATH/etc in bash_profile and not in bashrc. Hmmm.
[ -s ~/.bashrc ] && source ~/.bashrc > /dev/null 2>&1
[ -s ~/.bash_profile ] && source ~/.bash_profile > /dev/null 2>&1

# check sudo
have_sudo=0
sudo -n echo ok > /dev/null 2>&1
[ \$? -eq 0 ] && have_sudo=1

function fixpaths ()
{
	mypwd=\$(pwd)
	fixed_paths=\$(echo "\$1" | sed -e "s# [\\.~]/# \$mypwd/#g" -e "s# \\.\\./# \$mypwd/../#g" -e "s#~#\$mypwd/#g" -e "s# /#|/#g" -e "s# #|\$mypwd/#g" -e 's/|/ /g' -e 's/^ //' -e 's#//#/#g')
}

if [ $hostnum -eq 0 ] ; then
	# expand "~" etc. in paths given
	# note local vs. remote substitution
	fixpaths "$installdir"
	installdir="\$fixed_paths"
	fixpaths "$all_data_paths"
	all_data_paths="\$fixed_paths"
	fixpaths "$all_log_paths"
	all_log_paths="\$fixed_paths"
else
	installdir="$installdir"
fi

echo "Checking for existing Oracle NoSQL installation..."
ps auxww | grep 'java.*kvstore' | grep -v grep > /dev/null 2>&1
if [ \$? -eq 0 -a $proxy_only -eq 0 ] ; then
	echo ""
	if [ $force_overwrite -eq 1 ] ; then
		echo "-force: Stopping nosql on $host..."
		\$installdir/kvstore/scripts/stop_kvstore.sh
		pkill -f 'java.*kvstore.jar'
		pkill -f 'java.*httpproxy.jar'
		echo ""
		sleep 3
	else
		echo "$host appears to already be running Oracle NoSQL. If you wish to"
		echo "use this program to change the current setup, please log in to $host"
		echo "first and stop all running Oracle NoSQL processes."
		echo ""
		exit 1
	fi
fi

has_prev_install=0
ls -la \$installdir/kvroot > /dev/null 2>&1
if [ \$? -eq 0 ] ; then
	has_prev_install=1
fi


function get_first_ip ()
{
	local ipaddrs="\$1"
	[ "\$ipaddrs" = "" ] && return
	local firstip=""
	# prefer 10.0.x.x over others
	for i in \$ipaddrs ; do
		[ "\$firstip" = "" ] && firstip=\$i
		[[ \$i =~ ^10\\.0 ]] && echo \$i && return
	done
	echo \$firstip
}

# get internal ip
echo "getting internal IP address..."
ipaddr=""
ifc=ifconfig
[ -x /sbin/ifconfig ] && ifc=/sbin/ifconfig
[ -x /usr/sbin/ifconfig ] && ifc=/usr/sbin/ifconfig
ipaddrs=\$(\$ifc | egrep '10\\.[0-9]*\\.[0-9]*\\.[0-9]|172.[123][0-9]*\\.[0-9]*\\.[0-9]|192\\.168\\.[0-9]*\\.[0-9]' | sed -e 's/1/|/' -e 's/^.*|/1/' -e 's/ .*$//')
[ "\$ipaddrs" = "" ] && ipaddrs=\$(hostname -i 2>/dev/null)
ipaddr=\$(get_first_ip "\$ipaddrs")
[ "\$ipaddr" != "" ] && echo "\$ipaddr" > ~/myip

echo "checking hostname-to-ip mapping..."
myhost=\$(hostname)
myip=\$(hostname -i 2>/dev/null)
if [ "\$myip" != "\$ipaddr" ] ; then
	if [ \$have_sudo -eq 1 ] ; then
		sudo /bin/bash -c "echo \"\$ipaddr \$myhost\" >> /etc/hosts 2>/dev/null"
		sleep 1
	fi
	myip=\$(hostname -i 2>/dev/null)
fi
if [ "\$myip" != "\$ipaddr" -a  "\$myip" != "" ] ; then
  echo "Hostname \$myhost on $host does not resolve to ip address \$ipaddr."
  echo "This may cause connection issues."
  echo ""
fi
if [ "\$myip" = "" ] ; then
	echo ""
	echo "Hostname \$myhost on $host does not resolve to any ip address."
	echo "This will likely cause subsequent steps to fail."
	echo ""
fi

function increase_user_limits ()
{
	# if on OCI, try to increase proc/file limits
	[ "$is_oci_instance" != "1" -o \$have_sudo -ne 1 ] && return
	myid=\$(whoami)
	grep "^\$myid.*nproc" /etc/security/limits.conf > /dev/null 2>&1
	[ \$? -eq 0 ] && return

	echo "Increasing file/process limits for user \"\$myid\"..."
	echo "# \$myid entries created by Oracle NoSQL cluster install" > /tmp/nosql_limits
	echo "\$myid   hard   nproc   20000" >> /tmp/nosql_limits
	echo "\$myid   soft   nproc   20000" >> /tmp/nosql_limits
	echo "\$myid   hard   nofile   100000" >> /tmp/nosql_limits
	echo "\$myid   soft   nofile   100000" >> /tmp/nosql_limits
	sudo /bin/bash -c "cat /tmp/nosql_limits >> /etc/security/limits.conf"
	/bin/rm -f /tmp/nosql_limits
}

function get_local_memgb ()
{
	memkb=\$(grep 'MemAvailable:' /proc/meminfo | awk '{print \$2}')
	# default: 16GB (this will be reduced automatically later)
	[ "\$memkb" = "" ] && memkb=16000000
	memgb=\$(expr \$memkb / 1000000)
	[ \$memgb -eq 0 ] && memgb=1
}


function get_java_homedir ()
{
	javahome=\$(java -XshowSettings:properties -version 2>&1 | grep 'java.home = ' | awk '{print \$3}')
	[ "\$javahome" != "" ] && return 0

	if [ "$is_oci_instance" = "1" ] ; then
		# see if we can install java
		sudo yum install -y java > /dev/null 2>&1
		if [ \$? -eq 0 ] ; then
			javahome=\$(java -XshowSettings:properties -version 2>&1 | grep 'java.home = ' | awk '{print \$3}')
			[ "\$javahome" != "" ] && return 0
		fi
	fi

	echo "Error: java does not seem to be installed on $host."
	exit 1
}

echo "Checking java installation..."
get_java_homedir
jversion=\$(\$javahome/bin/java -version 2>&1)
if [ \$? -ne 0 ] ; then
	echo "Error: java does not seem to be installed in \$javahome/bin/java on $host."
	exit 1
fi
# Java version "n.x.x"
version=\$(echo \$jversion | awk -F\" '{print \$2}' | awk -F. '{print \$1}')
if [ "\$version" = "1" ] ; then
	# Java version "1.n.x"
	version=\$(echo \$jversion | awk -F\" '{print \$2}' | awk -F. '{print \$2}')
fi
if [ "\$version" = "" ] ; then
	echo "\$javahome on $host does not appear to point to valid java installation."
	exit 1
fi
if [ \$version -lt 8 -o \$version -gt 15 ] ; then
	echo ""
	echo "Oracle NoSQL requires java version 8 or higher. The java installation on"
	echo "$host is \$version."
	echo ""
	exit 1
fi

echo "Collecting system resources..."

have_nc=0
which nc > /dev/null 2>&1
[ \$? -eq 0 ] && have_nc=1

get_local_memgb
increase_user_limits

echo "have_sudo=\$have_sudo" > nosql_vars
echo "javahome=\$javahome" >> nosql_vars
echo "ipaddr=\$ipaddr" >> nosql_vars
echo "memgb=\$memgb" >> nosql_vars
echo "has_prev_install=\$has_prev_install" >> nosql_vars
echo "have_nc=\$have_nc" >> nosql_vars
if [ $hostnum -eq 0 ] ; then
	echo "installdir=\$installdir" >> nosql_vars
	echo "all_data_paths=\\"\$all_data_paths\\"" >> nosql_vars
	echo "all_log_paths=\\"\$all_log_paths\\"" >> nosql_vars
fi

EOT


	echo "Collecting / verifying data on $host..."
	scp $sshopts $TMPDIR/nosql_collect.sh.$$ ${sshat}$host:nosql_collect.sh > /dev/null
	if [ $? -ne 0 ] ; then
		echo ""
		echo "There appears to be a problem with your ssh credentials."
		echo "Please verify that you can run the following command with"
		echo "no errors, then run this script again."
		echo ""
		echo "   ssh $sshopts ${sshat}$host \"echo ok\""
		echo ""
		exit 1
	fi
	ssh $sshopts ${sshat}$host "chmod 755 ./nosql_collect.sh ; ./nosql_collect.sh ; ret=\$? ; rm -f ./nosql_collect.sh ; exit \$ret"
	if [ $? -ne 0 ] ; then
		exit 1
	fi
	scp $sshopts ${sshat}$host:nosql_vars $TMPDIR/nosql_vars.$$ > /dev/null 2>&1
	if [ $? -ne 0 ] ; then
		echo ""
		echo "Error: can't collect data from $host"
		exit 1
	fi
	ssh $sshopts ${sshat}$host "rm -f nosql_vars" >/dev/null 2>&1

	source $TMPDIR/nosql_vars.$$
	rm -f $TMPDIR/nosql_vars.$$

	host_capacity=$(expr $memgb / 30)
	[ $host_capacity -lt $min_capacity ] && host_capacity=$min_capacity
	[ $host_capacity -gt $max_host_capacity ] && max_host_capacity=$host_capacity

	if [ "$has_prev_install" = "1" -a $proxy_only -eq 0 ] ; then
		echo ""
		echo "$host appears to have a previous installation of Oracle NoSQL."
		echo ""
		if [ $force_overwrite -eq 1 ] ; then
			echo "-force: overwriting."
			echo ""
			sleep 2
		else
			echo "Is it OK to overwrite the previous installation?"
			echo ""
			echo -n "Overwrite? (y/n) [n]: "
			read yorn
			[ "$yorn" != "y" -a "$yorn" != "Y" ] && exit 1
		fi
		will_overwrite=1
	fi

	ipaddrs[$hostnum]=$ipaddr
}

function collect_all_hosts_data ()
{
	i=0
	while [ $i -lt $numhosts ] ; do
		host=${hosts[$i]}
		clear_screen
		collect_host_data $host $i
		i=$(expr $i + 1)
	done
	save_vals
}


function get_data_paths ()
{
	[ "$skip_data_paths" = "1" -a "$all_data_paths" != "" ] && return
	clear_screen
	echo ""
	echo "Enter the directory path(s) on the hosts where you want to store NoSQL data."
	echo "For best performance, it is highly recommended that these directores be NVMe"
	echo "or SSD devices, and there are no other programs/systems using them."
	echo ""
	echo "Paths will be created on all hosts if they do not already exist."
	echo ""
	echo "Multiple directories can be specified by separating paths with spaces."
	echo "Oracle NoSQL will try to make use of all paths entered."
	echo ""
	echo -n "data directory path(s)"
	[ "$all_data_paths" = "" ] && all_data_paths="$installdir/data"
	defprompt "$all_data_paths"
	read dpaths
	[ "$dpaths" = "" -a "$all_data_paths" = "" ] && exit 1
	[ "$dpaths" = "" ] && dpaths="$all_data_paths"
	all_data_paths="$dpaths"
	datapaths=($all_data_paths)
	[ ${#datapaths[*]} -eq 0 ] && exit 1

	save_vals
}

function get_log_paths ()
{
	[ "$skip_log_paths" = "1" -a "$all_data_paths" != "" ] && return
	clear_screen
	echo ""
	echo "If you wish to store nosql log files in a separate location than the"
	echo "data paths listed above, enter the directory path(s) where you want"
	echo "to store log files on all the hosts."
	echo ""
	echo "Hit <enter> by itself to store logs under the same paths as above."
	echo ""
	echo "Paths will be created on all hosts if they do not already exist."
	echo ""
	echo "Multiple directories can be specified by separating paths with spaces."
	echo ""
	echo -n "log directory path(s)"
	[ "$all_data_paths" = "$installdir/data" -a "$all_log_paths" = "" ] && all_log_paths="$installdir/logs"
	defprompt "$all_log_paths"
	read lpaths
	[ "$lpaths" = "" ] && lpaths="$all_log_paths"
	all_log_paths="$lpaths"

	save_vals
}

# read and verify integer port number
# set global $intport to read-in value
function read_integer_port ()
{
	local privok=$1
	local defport=$2

	read port
	[ "$port" = "" -a "$defport" = "" ] && exit 1
	[ "$port" = "" ] && port=$defport
	intport=$port
	# check for integer input
	[[ $((intport)) != $intport ]] && echo "Input must be an integer." && exit 1
	if [ $intport -le 1024 -a $privok -ne 1 ] ; then
		echo ""
		echo "This program can only install NoSQL on a non-restricted port."
		exit 1
	fi
	#echo $defport
}

function get_start_port ()
{
	clear_screen
	echo ""
	echo "Enter the desired port number to use as a base for communications. Oracle"
	echo "NoSQL will use a range of ports starting at this address (exact range will"
	echo "be determined in later steps). Oracle recommends a default port number of 5000."
	echo ""
	echo -n "Enter starting port number"
	[ "$startport" = "" ] && startport=5000
	defprompt $startport
	read_integer_port 0 $startport
	startport=$intport
	save_vals
}


function verify_passwd ()
{
	local passwd="$1"

	[ ${#passwd} -le 8 ] && echo "password must be at least 9 characters" && return 1
	schars=$(echo $passwd | tr -cd '[:space:]')
	[ ${#schars} -gt 0 ] && echo "password must not contain spaces" && return 1
	pchars=$(echo $passwd | tr -cd '~!@#$%^&*()+|:;?/,.')
	[ ${#pchars} -lt 2 ] && echo "password must contain at least 2 special ( "'~!@#%^&*()+|:;?/,.'" ) characters" && return 1
	stripped=$(echo $passwd | tr -cd '~!@#%^&*()+|:;?/,.[:alnum:]')
	[ ${#passwd} -ne ${#stripped} ] && echo "password contains invalid special characters" && return 1
	uchars=$(echo $passwd | tr -cd '[:upper:]')
	[ ${#uchars} -lt 2 ] && echo "password must contain at least 2 uppercase characters" && return 1
	lchars=$(echo $passwd | tr -cd '[:lower:]')
	[ ${#lchars} -lt 2 ] && echo "password must contain at least 2 lowercase characters" && return 1
	nchars=$(echo $passwd | tr -cd '[:digit:]')
	[ ${#nchars} -lt 2 ] && echo "password must contain at least 2 numerical digits" && return 1
	return 0
}

function read_passwd ()
{
	local username=$1
	echo ""
	echo "Passwords must be at least 9 characters and contain at least two each of"
	echo "upper, lower, digit and special ( ~!@#%^&*()+|:;?/,. ) characters."
	local passdone=0
	while [ $passdone -eq 0 ] ; do
		password=""
		while [ "$password" = "" ] ; do
			echo ""
			echo -n "$username password: "
			read -s password
			verify_passwd "$password"
			[ $? -ne 0 ] && password=""
		done
		password2=""
		while [ "$password2" = "" ] ; do
			echo ""
			echo -n "retype $username password: "
			read -s password2
			verify_passwd "$password2"
			[ $? -ne 0 ] && password2=""
		done
		echo ""
		if [ "$password" = "$password2" ] ; then
			passdone=1
		else
			echo "passwords don't match."
			echo ""
		fi
	done
}

function random_char ()
{
	local possible="$1"
	local len=${#possible}
	num=$(expr $RANDOM % $len)
	num=$(expr $num + 1)
	echo "$possible" | head -c$num | tail -c1
}

function random_password ()
{
	local passwd=""

	# TODO: more complicated algorithm?
	for i in 1 2 3 ; do
		passwd=$passwd$(random_char "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
		passwd=$passwd$(random_char "0123456789")
		passwd=$passwd$(random_char "~!@#%^&*(>)+|:;?/,.")
		passwd=$passwd$(random_char "abcdefghijklmnopqrstuvwxyz")
	done
	echo $passwd
}


function get_secure_passwds ()
{
	if [ $dotest -eq 1 ] ; then
		[ $secure_store -eq 0 ] && return
		admpass=$(random_password)
		nosqlpass=$(random_password)
		return
	fi

	[ $proxy_only -eq 1 ] && return

	clear_screen
	echo ""
	echo "This program will set up a secure store. If you do not want a secure"
	echo "store, enter \"n\" at the prompt below."
	echo ""
	echo -n "Set up secure store (y/n) [y]: "
	read yorn
	if [ "$yorn" != "" -a "$yorn" != "y" -a "$yorn" != "Y" ] ; then
		echo ""
		echo "Are you sure you want to set up an unrestricted, insecure store?"
		echo ""
		echo -n "Set up insecure store? (y/n) [n]: "
		read yorn
		if [ "$yorn" = "y" -o "$yorn" = "Y" ] ; then
			secure_store=0
			return
		fi
	fi
	secure_store=1
	autogen=0
	clear_screen
	echo ""
	echo "Secure setup will create two DB users: \"admin\" and \"nosql\"."
	echo ""
	echo "\"admin\" is used for administrative tasks (topology, maintenance, etc.)"
	echo ""
	echo "\"nosql\" is used for client-side data operations (insert, query, etc.)"
	echo ""
	echo "Would you like this program to auto-generate passwords for the above"
	echo "users? Enter \"n\" here to enter passwords manually."
	echo ""
	echo -n "auto-generate user passwords? (y/n) [y]: "
	read yorn
	if [ "$yorn" != "n" -a "$yorn" != "N" ] ; then
		admpass=$(random_password)
		nosqlpass=$(random_password)
		autogen=1
		echo ""
		echo "Passwords generated. Copy these to a safe location for later use."
		echo ""
		echo "admin password: $admpass"
		echo "nosql password: $nosqlpass"
		echo ""
		askcontinue any
		return
	fi
	clear_screen
	echo ""
	echo "Enter the store admin password. A user named 'admin' will be created with"
	echo "full privileges in the store with this password. Save it in a safe location"
	echo "now: it will not be shown later."
	echo ""
	read_passwd admin
	admpass=$password

	clear_screen
	echo ""
	echo "Enter the nosql user password. A user named 'nosql' will be created with"
	echo "default privileges in the store with this password. Save it in a safe location"
	echo "now: it will not be shown later."
	echo ""
	read_passwd nosql
	nosqlpass=$password
}



function get_rep_factor ()
{
	clear_screen
	echo ""
	if [ $numhosts -eq 1 ] ; then
		echo "Since only one host machine is used, replication factor for this setup will"
		echo "default to 1. If you want to use a higher replication factor, please set up"
		echo "more than one host."
		askcontinue any
		repfactor=1
	elif [ $numhosts -eq 2 ] ; then
		echo "Enter the cluster replication factor (1 or 2). A replication"
		echo "factor of 1 is dangerous since no copies of the data will exist"
		echo "and you will lose data if a host dies."
		echo ""
		echo "For higher replication factors, please set up more hosts."
		echo ""
		echo -n "Replication factor"
		[ "$repfactor" = "" ] && repfactor=1
		defprompt $repfactor
		read_integer 1 2 $repfactor
		repfactor=$intval
	else
		echo "Enter the cluster replication factor (1, 2, or 3). A replication"
		echo "factor of 1 is dangerous since no copies of the data will exist"
		echo "and you will lose data if a host dies."
		echo ""
		echo "Replication factor of 3 is recommended for best data retention."
		echo ""
		echo -n "Replication factor"
		[ "$repfactor" = "" ] && repfactor=3
		defprompt $repfactor
		read_integer 1 3 $repfactor
		repfactor=$intval
	fi
	save_vals
}

function set_min_capacity ()
{
	# if RF>1, set minimum capacity to <RF>.
	# This will spread masters across different hosts.
	min_capacity=1
	if [ $repfactor -gt 1 ] ; then
		min_capacity=$repfactor
	fi

	# make sure capacity is at least the number of data paths given
	local dpaths=($all_data_paths)
	local numdpaths=${#dpaths[*]}
	[ $min_capacity -lt $numdpaths ] && min_capacity=$numdpaths
}

function calculate_port_ranges ()
{
	clear_screen
	echo ""
	echo "Calculating port ranges..."

	if [ $secure_store -eq 1 ] ; then
		snports=4
		rnports=$(expr $max_host_capacity \* 3)
		admports=3
	else
		snports=3
		rnports=$(expr $max_host_capacity \* 2)
		admports=2
	fi

	halow=$(expr $startport + 1)
	totalports=$(expr $snports + $rnports + $admports)
	hahigh=$(expr $halow + $totalports)
	harange="$halow,$hahigh"
	servicelow=$(expr $hahigh + 1)
	servicehigh=$(expr $servicelow + $totalports)
	servicerange="$servicelow,$servicehigh"
	if [ $repfactor -eq 2 ] ; then
		arbport=$(expr $servicehigh + 1)
		arblow=$(expr $arbport + 1)
		arbhigh=$(expr $arblow + 4)
		arbrange="$arblow,$arbhigh"
		arbservlow=$(expr $arbhigh + 1)
		arbservhigh=$(expr $arbservlow + 4)
		arbservrange="$arbservlow,$arbservhigh"
	else
		# used only for network connectivity tests
		arbservhigh=$servicehigh
	fi
}

function check_host_host_conn_port ()
{
	local srchost=$1
	local desthost=$2
	local port=$3

	get_ipaddr_cached $desthost
	local destip=$cached_ipaddr
	echo "  $srchost ---> $destip:$port..."
	# start listener on destport, run for 15 seconds
	ssh $sshopts ${sshat}$desthost "timeout -s 9 15s nc -n -l $port" &
	sleep 1
	ssh $sshopts ${sshat}$srchost "echo verified | nc -n $destip $port"
	ret=$?
	wait
	[ $ret -eq 0 ] && echo "       success" && return
	echo ""
	echo ""
	echo "Cannot verify network connectivity from $srchost to $desthost on port $port."
	echo ""
	echo "please consult your local sysadmins to enable communication to/from all"
	echo "hosts via tcp on ports $startport - $arbservhigh."
	echo ""
	exit 1
}

function verify_nc_installed ()
{
	local host=$1

	echo "Checking for \"nc\" on $host..."

	ssh $sshopts ${sshat}$host "which nc" > /dev/null 2>&1
	[ $? -eq 0 ] && return 0

	echo ""
	echo "Attempting to install \"nc\" on $host..."
	echo ""
	ssh $sshopts ${sshat}$host "sudo yum install -y nc"
	[ $? -eq 0 ] && return 0
	echo ""
	echo "Unable to install \"nc\" on $host."
	echo "network connectivy check aborted."
	echo ""
	askcontinue y
	return 1
}

function check_host_host_conn ()
{
	local srchost=$1
	local desthost=$2
	[ "$srchost" = "$desthost" ] && return
	for port in $startport $halow $arbservhigh ; do
		check_host_host_conn_port $srchost $desthost $port
	done
}

function check_network_host ()
{
	local host=$1
	echo ""
	echo "Checking network connectivity on $host..."
	for desthost in ${hosts[*]} ; do
		check_host_host_conn $host $desthost
	done
}

function run_network_tests ()
{
	# Method 2: check all at once
	# create script that runs on all hosts, verifying all connectivity in parallel

cat > $TMPDIR/start_listeners.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x
host=\$1
ipaddr=\$(cat ~/myip)
[ "\$ipaddr" = "" ] && ipaddr=127.0.0.1
for port in $startport $halow $arbservhigh ; do
	nohup nc -n -k -l \$port > /tmp/nc.\$port.out 2>&1 &
done
sleep 1
ret=0
for port in $startport $halow $arbservhigh ; do
	echo success | nc -n -w 10 \$ipaddr \$port
	[ \$? -ne 0 ] && ret=1 && echo "error starting listener on \$host:\$port"
done
[ \$ret -eq 0 ] && exit 0
for port in $startport $halow $arbservhigh ; do
	cat /tmp/nc.\$port.out
	/bin/rm -f /tmp/nc.\$port.out
done
echo ""
echo "Error starting test listeners on \$host."
for port in $startport $halow $arbservhigh ; do
	pkill -f "nc -n -k -l \$port"
done
exit 1
EOT

cat > $TMPDIR/stop_listeners.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x
for port in $startport $halow $arbservhigh ; do
	pkill -f "nc -n -k -l \$port"
done
exit 0
EOT

	SCR=$TMPDIR/verify_connectivity.sh.$$
	echo "#!/bin/bash" > $SCR
	echo "[ $debug -eq 1 ] && set -x" >> $SCR
	echo "myhost=\$1" >> $SCR
	echo "failed=0" >> $SCR
	for host in ${hosts[*]} ; do
		echo "if [ \"\$myhost\" != \"$host\" ] ; then" >> $SCR
		get_ipaddr_cached $host
		local destip=$cached_ipaddr
		for port in $startport $halow $arbservhigh ; do
			echo "echo success | nc -n -w 10 $destip $port" >> $SCR
			echo "if [ \$? -ne 0 ] ; then" >> $SCR
			echo "echo \"connectivity failed from \$myhost to $host ($destip) on port $port\"" >> $SCR
			echo "failed=1" >> $SCR
			echo "fi" >> $SCR
		done
		echo "fi" >> $SCR
	done
	echo "exit \$failed" >> $SCR

	# copy start, stop, and verify scripts to all hosts
	# use xargs to manage parallel executions
	echo ""
	echo "Copying network validation scripts to hosts in parallel..."
	echo ""
	write_words_to_file "$allhosts" $TMPDIR/hosts.$$
	cat $TMPDIR/hosts.$$ | xargs -P 30 -I HOST scp $sshopts $TMPDIR/stop_listeners.sh.$$ $TMPDIR/start_listeners.sh.$$ $TMPDIR/verify_connectivity.sh.$$ ${sshat}HOST:
	if [ $? -ne 0 ] ; then
		echo ""
		echo "Error copying network validation scripts. Skipping network tests."
		echo ""
		sleep 5
		return 0
	fi

	# start listeners on all hosts in parallel
	# use xargs to manage parallel executions
	echo ""
	echo "Starting listeners on all hosts in parallel..."
	cat $TMPDIR/hosts.$$ | xargs -P 30 -I HOST ssh $sshopts ${sshat}HOST "chmod 755 ./*.sh.$$ ; ./start_listeners.sh.$$"
	if [ $? -ne 0 ] ; then
		echo ""
		echo "Error starting listeners. Skipping network tests."
		echo ""
		sleep 5
		return 0
	fi


	echo "Running validation scripts on all hosts in parallel..."
	cat $TMPDIR/hosts.$$ | xargs -P 30 -I HOST ssh $sshopts ${sshat}HOST "./verify_connectivity.sh.$$ HOST"
	ret=$?

	echo "Stopping listeners and removing scripts all hosts in parallel..."
	cat $TMPDIR/hosts.$$ | xargs -P 30 -I HOST ssh $sshopts ${sshat}HOST "./stop_listeners.sh.$$ ; /bin/rm -f ./*.sh.$$"

	bitmask=
	get_lowest_bitmask
	if [ $ret -ne 0 -a "$bitmask" != "" -a "$is_oci_instance" = "1" -a "$inside_net_check" != "1" ] ; then
		echo ""
		echo "Network connectivity tests failed."
		echo ""
		echo "Since the target hosts are running in an OCI environment, this"
		echo "program can attempt to set up simple firewall rules to fix the"
		echo "network connectivity issues."
		echo ""
		echo ""
		echo "NOTE: The firewall settings that this program can set up may violate"
		echo "      your company security policies and potentially leave your"
		echo "      instances vulnerable to security issues. If you are unsure"
		echo "      about this, do not have this program modify firewall settings."
		echo ""
		echo -n "Have this program modify firewall settings? (y/n) [n]: "
		[ $dotest -ne 1 ] && read yorn
		[ $dotest -eq 1 ] && yorn=y
		if [ "$yorn" = "y" -o "$yorn" = "Y" ] ; then
			inside_net_check=1
			setup_firewall_rules $startport $arbservhigh
			run_network_tests
		else
			echo ""
			echo "please consult your local sysadmins to enable communication to/from all"
			echo "hosts via tcp on ports $startport - $arbservhigh."
			echo ""
			echo ""
			echo "Cluster setup and operation will likely fail; would you like to"
			echo "continue with cluster setup anyway?"
			echo ""
			askcontinue n
			return
		fi
	fi

	if [ $ret -ne 0 ] ; then
		echo ""
		echo "Network connectivity tests failed."
		echo ""
		echo "please consult your local sysadmins to enable communication to/from all"
		echo "hosts via tcp on ports $startport - $arbservhigh."
		echo ""
		echo ""
		echo "Cluster setup and operation will likely fail; would you like to"
		echo "continue with cluster setup anyway?"
		echo ""
		askcontinue n
	else
		echo ""
		echo "Network connectivity tests passed."
		echo ""
	fi
}

function check_network_connectivity ()
{
	[ $numhosts -le 1 ] && return
	clear_screen
	echo ""
	echo "The port range selected for this installation is ${startport}-$arbservhigh."
	echo ""
	echo "This program can optionally verify network port connectivity between"
	echo "hosts. Doing so will use the \"netcat\" utility (\"nc\") to verify"
	echo "connections. "
	if [ $have_sudo -eq 0 -a $have_nc -eq 0 ] ; then
		echo ""
		echo "It appears that \"nc\" is not installed on the target host(s)."
		echo "Unable to verify port connectivity."
		echo ""
		askcontinue y
		return
	fi
	if [ $have_sudo -eq 1 -a $have_nc -eq 0 ] ; then
		echo ""
		echo "If netcat is not installed, this program will attempt to"
		echo "install it by calling \"sudo yum install -y nc\"."
	fi
	echo ""
	echo "Would you like this program to try verifying network connectivity?"
	echo -n "(y/n) [y]: "
	if [ $dotest -eq 0 ] ; then
		read yorn
		[ "$yorn" = "" ] && yorn=y
		[ "$yorn" != "y" -a "$yorn" != "Y" ] && return
	fi
	[ "$do_network" = "no" ] && return

	if [ $have_nc -eq 0 ] ; then
		for host in ${hosts[*]} ; do
			verify_nc_installed $host
			[ $? -ne 0 ] && return
		done
	fi

	inside_net_check=0

	run_network_tests
}

function show_parameters ()
{
	local logpaths="$all_log_paths"
	if [ "$logpaths" = "" ] ; then
		for path in $all_data_paths ; do
			logpaths="${all_log_paths}$path/log "
		done
	fi
	echo ""
	echo "Parameters that will be used for cluster setup:"
	echo ""
	echo "  hosts:      ${hosts[*]}"
	echo "  ipaddrs:    ${ipaddrs[*]}"
	echo "  installdir: $installdir"
	echo "  mainport:   $startport"
	echo "  port_range: ${startport}-$arbservhigh"
	echo "  datapaths:  $all_data_paths"
	echo "  logpaths:   $logpaths"
	echo "  security:   $secure_store"
	echo "  repfactor:  $repfactor"
	echo ""

	if [ $will_overwrite -ne 0 ] ; then
		echo "WARNING: Continuing past this point will remove/destroy any existing"
		echo "         Oracle NoSQL data!"
		echo ""
	fi
	askcontinue y
}

function copy_tarfile_to_host ()
{
	local host=$1
	local tarfile=$2
	local user=$3
	local opts="$4"

	local sshat=""
	[ "$user" != "" ] && sshat="${user}@"

	tarbase=$(basename $tarfile)
	# check for already scp'ed targz file
	tsum=$(cksum $tarfile | awk '{print $1,$2}')
	osum=$(ssh $opts ${sshat}$host "cksum $tarbase | awk '{print \$1,\$2}'" 2>/dev/null)
	if [ "$tsum" != "$osum" ] ; then
		echo "Copying $tarbase to $host..."
		scp $opts $tarfile ${sshat}$host:
		[ $? -ne 0 ] && exit 1
	fi
	return 0
}
export -f copy_tarfile_to_host

function setup_kvstore_host ()
{
	local host=$1
	local isfirst=$2

	echo ""
	echo "Setting up $host..."
	#copy_tarfile_to_host $host $targzfile $sshuser "$sshopts"

	if [ $numhosts -gt 1 -a $secure_store -eq 1 -a $isfirst -eq 0 -a -s $TMPDIR/SEC.tar.gz.$$ ] ; then
		echo ""
		echo "Copying security information to $host..."
		scp $sshopts $TMPDIR/SEC.tar.gz.$$ ${sshat}$host:SEC.tar.gz
		if [ $? -ne 0 ] ; then
			echo ""
			echo "Error copying security information from $TMPDIR/SEC.tar.gz to $host"
			echo ""
			exit 1
		fi
	fi

	echo ""
	echo "Creating setup script for $host..."
	get_ipaddr_cached $host
	local ipaddr=$cached_ipaddr

	targzbase=$(basename $targzfile)

	cat > $TMPDIR/nosql_setup.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x

# autogenerated by cluster_setup

trap "/bin/rm -f /tmp/*.\$\$" exit


function test_dir ()
{
	local dir=\$1
	touch \$dir/kv.\$\$
	if [ \$? -ne 0 ] ; then
		echo ""
		iam=\$(whoami)
		echo "Error: user \$iam does not have write permission in \$dir on $host."
		echo ""
		exit 1
	fi
	/bin/rm -f \$dir/kv.\$\$
	return 0
}


function make_dir ()
{
	local dir=\$1
	[ -d \$dir ] && test_dir \$dir && return 0
	/bin/mkdir -p \$dir > /dev/null 2>&1
	[ \$? -eq 0 ] && test_dir \$dir && return 0
	[ $have_sudo -eq 0 ] && /bin/mkdir -p \$dir && return 1
	sudo /bin/mkdir -p \$dir > /dev/null 2>&1
	[ \$? -ne 0 ] && /bin/mkdir -p \$dir && return 1
	sudo chmod 755 \$dir > /dev/null 2>&1
	iam=\$(whoami)
	[ "\$iam" != "" ] && sudo chown \$iam \$dir > /dev/null 2>&1
	test_dir \$dir
	return 0
}

function get_local_memgb ()
{
    memkb=\$(grep 'MemAvailable:' /proc/meminfo | awk '{print \$2}')
    # default: 16GB (this will be reduced automatically later)
    [ "\$memkb" = "" ] && memkb=16000000
    memgb=\$(expr \$memkb / 1000000)
    [ \$memgb -eq 0 ] && memgb=1
}


function adjust_paths ()
{
	# adjust datadirs based on local capacity
	local capacity=\$1

	dpaths=($all_data_paths)
	numdpaths=\${#dpaths[*]}
	if [ \$numdpaths -lt \$capacity ] ; then
		mult=\$(expr \$capacity / \$numdpaths)
		rem=\$(expr \$capacity % \$numdpaths)
		[ \$rem -gt 0 ] && mult=\$(expr \$mult + 1)
		all_data_paths=""
		for path in \${dpaths[*]} ; do
			i=1
			while [ \$i -le \$mult ] ; do
				all_data_paths="\${all_data_paths}\$path/\$i "
				i=\$(expr \$i + 1)
			done
		done
	else
		all_data_paths="$all_data_paths"
	fi

	# adjust logdirs based on rnsperhost (capacity)
	if [ "$all_log_paths" = "" ] ; then
		for path in \$all_data_paths ; do
			all_log_paths="\${all_log_paths}\$path/log "
		done
	else
		lpaths=($all_log_paths)
		numlpaths=\${#lpaths[*]}
		if [ \$numlpaths -lt \$capacity ] ; then
			mult=\$(expr \$capacity / \$numlpaths)
			rem=\$(expr \$capacity % \$numdpaths)
			[ \$rem -gt 0 ] && mult=\$(expr \$mult + 1)
			all_log_paths=""
			for path in \${lpaths[*]} ; do
				i=1
				while [ \$i -le \$mult ] ; do
					all_log_paths="\${all_log_paths}\$path/\$i "
					i=\$(expr \$i + 1)
				done
			done
		else
			all_log_paths="$all_log_paths"
		fi
	fi
}

echo "Unpacking $targzbase on $host..."
make_dir $installdir
[ \$? -ne 0 ] && echo "Can't create $installdir on $host" && exit 1
homedir=\`pwd\`
cd $installdir
[ \$? -ne 0 ] && exit 1
if [[ $targzbase =~ \.zip\$ ]] ; then
unzip -o \$homedir/$targzbase > /tmp/unpack.out.\$\$ 2>&1
else
gzip -dc \$homedir/$targzbase | tar xvf - > /tmp/unpack.out.\$\$ 2>&1
fi
if [ \$? -ne 0 ] ; then
cat /tmp/unpack.out.\$\$
echo "Error unpacking $targzbase on $host"
exit 1
fi

echo "Validating Oracle NoSQL installation..."
kvdir=$targztop
cd $installdir
[ -L kvstore ] && rm -f kvstore
ln -s \$kvdir kvstore
cd \$homedir
export KVHOME=$installdir/kvstore
export KVROOT=$installdir/kvroot
java -jar \$KVHOME/lib/kvstore.jar version
[ \$? -ne 0 ] && echo "Oracle NoSQL installation in $installdir is invalid" && exit 1

if [ $is_enterprise -eq 1 ] ; then
	java -jar \$KVHOME/lib/kvstore.jar version 2>&1 | grep Enterprise > /dev/null
	if [ \$? -ne 0 ] ; then
		echo ""
		echo "Error: the supplied file $targzfile indicates Enterprise Edition,"
		echo "but the actual version in the file is not Enterprise."
		echo ""
		exit 1
	fi
fi

echo ""
echo "Setting up \$KVROOT..."

# clean previous admin setup
/bin/rm -rf $installdir/kvroot/*
make_dir \$KVROOT/admin
[ \$? -ne 0 ] && echo "Can't create \$KVROOT/admin on $host" && exit 1
make_dir \$KVHOME/scripts
[ \$? -ne 0 ] && echo "Can't create \$KVHOME/scripts on $host" && exit 1

# determine capacity for this host
get_local_memgb
host_capacity=\$(expr \$memgb / 30)
[ \$host_capacity -lt $min_capacity ] && host_capacity=$min_capacity

# adjust data/log paths based on local capacity
adjust_paths \$host_capacity


# clean any previous data, create subdirs if needed
for path in \$all_data_paths ; do
make_dir \$path
[ \$? -ne 0 ] && echo "Can't create \$path on $host" && exit 1
/bin/rm -rf \$path/rg* > /dev/null 2>&1
done

for path in \$all_log_paths ; do
make_dir \$path
[ \$? -ne 0 ] && echo "Can't create \$path on $host" && exit 1
/bin/rm -rf \$path/*.log > /dev/null 2>&1
done

if [ $secure_store -eq 1 -a $isfirst -eq 0 -a -s \$homedir/SEC.tar.gz ] ; then
	echo ""
	echo "Unpacking security information..."
	cd \$KVROOT
	gzip -dc \$homedir/SEC.tar.gz | tar xvf -
	if [ \$? -ne 0 ] ; then
		echo "Error unpacking security info to \$KVROOT"
		echo ""
		exit 1
	fi
fi

STORAGEDIRS=
for path in \$all_data_paths ; do
	STORAGEDIRS="\$STORAGEDIRS -storagedir \$path"
done
RNLOGDIRS=
for path in \$all_log_paths ; do
	RNLOGDIRS="\$RNLOGDIRS -rnlogdir \$path"
done

function make_bootconfig ()
{
	local outfile=\$1
	local maxmem=\$2
	local port=\$3
	local capacity=\$4
	local harange="\$5"
	local servrange="\$6"
	local root=\$7

echo ""
echo "Creating server boot config..."

pwdmgr=pwdfile
[ $is_enterprise -eq 1 ] && pwdmgr=wallet
if [ $secure_store -eq 0 ] ; then
	SECURITY="-store-security none"
else
	if [ $isfirst -eq 1 -a \$capacity -gt 0 ] ; then
		SECURITY="-store-security configure -pwdmgr \$pwdmgr -kspwd \"$admpass\""
	else
		SECURITY="-store-security enable -pwdmgr \$pwdmgr"
	fi
fi

ALLDIRS="\$STORAGEDIRS \$RNLOGDIRS"
ADMINDIR="-admindir \$root/admin -admindirsize 5_gb"
[ \$capacity -eq 0 ] && ALLDIRS="" && ADMINDIR=""

if [ $verbose -eq 1 ] ; then
echo ""
echo "bootconfig parameters:"
echo "-root \$root "
echo "-host $ipaddr "
echo "-harange \$harange "
echo "-servicerange \$servrange "
echo "-port \$port "
echo "-config config.xml "
echo "\$SECURITY"
echo "\$ADMINDIR"
echo "\$ALLDIRS "
echo "-capacity \$capacity "
echo "-memory_mb \$maxmem"
echo ""
fi


java -Xmx64m -Xms64m \
-jar \$KVHOME/lib/kvstore.jar makebootconfig \
-verbose \
-root \$root \
-host $ipaddr \
-harange \$harange \
-servicerange \$servrange \
-port \$port \
-config config.xml \
\$SECURITY \
\$ADMINDIR \
\$ALLDIRS \
-capacity \$capacity \
-memory_mb \$maxmem > \$outfile 2>&1

return \$?
}

CFGOUT=/tmp/makebootconfig.out.\$\$
maxmem=\$(expr \$memgb \* 1000)
make_bootconfig \$CFGOUT \$maxmem $startport \$host_capacity "$harange" "$servicerange" \$KVROOT
ret=\$?

if [ \$ret -ne 0 ] ; then

	# mem setup: "memoryMB must be <= XXXXX (the total available memory in MB"
	maxmem=\$(grep 'memoryMB must be <= ' \$CFGOUT | sed -e 's/^.*be <= //' -e 's/ .*\$//')
	if [ "\$maxmem" != "" ] ; then
		# check for integer value
		if [[ \$((maxmem)) = \$maxmem ]] ; then
			maxmem=\$(expr \$maxmem - 10)
			# try again
			[ $verbose -eq 1 ] && echo "Trying again with \$maxmem MB"
			make_bootconfig \$CFGOUT \$maxmem $startport \$host_capacity "$harange" "$servicerange" \$KVROOT
			ret=\$?
		fi
	fi
fi

if [ \$ret -ne 0 ] ; then
	echo ""
	echo "Error setting up boot config:"
	uline=\$(grep -m 1 -n Usage \$CFGOUT | sed -e 's/:.*$//')
	[ "\$uline" = "" ] && uline=1000
	uline=\$(expr \$uline - 1)
	head -\$uline \$CFGOUT | grep -v 'and all volume free space'
	echo ""
	exit 1
fi
grep -v 'and all volume free space' \$CFGOUT
/bin/rm -f \$CFGOUT

# special for R=2: arbiter SNs
if [ $repfactor -eq 2 ] ; then
	# maxmem? maybe 100MB?
	make_dir \$KVROOT/arbroot
	if [ $secure_store -eq 1 ] ; then
		# copy security files to arbiter
		make_dir \$KVROOT/arbroot/security
		pushd \$KVROOT
		tar cvf \$homedir/SEC.tar security
		cd \$KVROOT/arbroot
		tar xvf \$homedir/SEC.tar
		/bin/rm -f \$homedir/SEC.tar
		popd
	fi
	make_bootconfig \$CFGOUT 100 $arbport 0 "$arbrange" "$arbservrange" \$KVROOT/arbroot
	[ \$? -ne 0 ] && cat \$CFGOUT && echo "Can't create arbiter config" && exit 1
fi

# if on OCI, make sure ntpd is running
if [ "$is_oci_instance" = "1" -a $have_sudo -eq 1 ] ; then
	echo "Verifying ntpd running..."
	sudo systemctl list-unit-files | grep 'ntpd.service.*enabled' > /dev/null 2>&1
	if [ \$? -ne 0 ] ; then
		# install ntp
		sudo yum -y install ntp > /dev/null 2>&1
		# configure ntp
		# force time sync on start
		sudo sed -i 's/^OPTIONS=\"-u/OPTIONS=\"-x -u/' /etc/sysconfig/ntpd
		# uncomment local config
		sudo sed -i 's/^.*192.168.1.0 mask.*$/restrict 192.168.1.0 mask 255.255.255.0 nomodify notrap/' /etc/ntp.conf
		# use us.pool servers
		sudo sed -i 's/^\(server [0-3]\).*$/\1.us.pool.ntp.org/g' /etc/ntp.conf

		# start ntp and add to boot config
		sudo service ntpd start > /dev/null 2>&1
		sudo chkconfig ntpd on > /dev/null 2>&1
	fi
fi

# if on OCI, make sure chronyd is running
if [ "$is_oci_instance" = "1" -a $have_sudo -eq 1 ] ; then
	echo "Verifying chronyd running..."
	sudo systemctl list-unit-files | grep 'chronyd.service.*enabled' > /dev/null 2>&1
	if [ \$? -ne 0 ] ; then
		# install chrony
		sudo yum -y install chrony > /dev/null 2>&1
		# configure chrony
		# force time sync on start
		sudo sed -i 's/^OPTIONS=\"-u/OPTIONS=\"-x -u/' /etc/sysconfig/chronyd
		# uncomment local config
		sudo sed -i 's/^.*192.168.1.0 mask.*$/restrict 192.168.1.0 mask 255.255.255.0 nomodify notrap/' /etc/chrony.conf
		# use us.pool servers
		sudo sed -i 's/^\(server [0-3]\).*$/\1.us.pool.ntp.org/g' /etc/chrony.conf
		# start chrony and add to boot config
		sudo service chronyd start > /dev/null 2>&1
		sudo chkconfig chronyd on > /dev/null 2>&1
	fi
fi

# Create start/stop/admin scripts, execute start script
SCR=\$KVHOME/scripts/start_kvstore.sh
echo "#!/bin/bash" > \$SCR
echo "export KVHOME=$installdir/kvstore" >> \$SCR
echo "export KVROOT=$installdir/kvroot" >> \$SCR

# stop script is the same, with 'start' replaced by 'stop' and no backgrounding
cp \$SCR \$KVHOME/scripts/stop_kvstore.sh
# admin script is the same, with 'start' replaced by 'runadmin' and no backgrounding
cp \$SCR \$KVHOME/scripts/kvstore_admin.sh
# same for ping script
cp \$SCR \$KVHOME/scripts/kvstore_ping.sh

# start
echo "cd \$KVROOT" >> \$SCR
echo "nohup java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar start -root \$KVROOT < /dev/null > kvstore.out 2>&1 &" >> \$SCR
if [ $repfactor -eq 2 ] ; then
	echo "cd \$KVROOT/arbroot" >> \$SCR
	echo "nohup java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar start -root \$KVROOT/arbroot < /dev/null > arbstore.out 2>&1 &" >> \$SCR
fi
chmod 755 \$SCR

#stop
echo "java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar stop -root \$KVROOT" >> \$KVHOME/scripts/stop_kvstore.sh
[ $repfactor -eq 2 ] && echo "java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar stop -root \$KVROOT/arbroot" >> \$KVHOME/scripts/stop_kvstore.sh
chmod 755 \$KVHOME/scripts/stop_kvstore.sh

# start SN
echo ""
echo "Starting SN on $host..."
\$KVHOME/scripts/start_kvstore.sh

sleep 5

echo ""
echo -n "Validating SN..."

failed=1
for i in {1..30} ; do
	sleep 2
	tail -10 \$KVROOT/snaboot_0.log | grep 'ProcessMonitor: ManagedServiceStarted: BootstrapAdmin' > /dev/null 2>&1
	[ \$? -eq 0 ] && failed=0 && break
	echo -n "."
done

if [ \$failed -eq 1 ] ; then
	echo ""
	tail -3 \$KVROOT/snaboot_0.log
	echo ""
	echo "SN on $host appears to have not started correctly. Check errors in"
	echo "log files in \$KVROOT on $host."
	echo ""
	exit 1
fi

echo ""
echo ""
echo "SN on $host started."
echo ""

if [ $secure_store -eq 0 ] ; then
	# create admin/ping scripts and exit
	echo "java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar runadmin -host $ipaddr -port $startport \"\\\$@\" " >> \$KVHOME/scripts/kvstore_admin.sh
	echo "java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar ping -host $ipaddr -port $startport \"\\\$@\" " >> \$KVHOME/scripts/kvstore_ping.sh
	chmod 755 \$KVHOME/scripts/*.sh
	exit 0
fi

if [ $isfirst -eq 0 ] ; then
	# no additional setup needed - maybe validate admin works?
	# TODO
	exit 0
fi

function create_user ()
{
	local username="\$1"
	local userpass="\$2"
	local isadmin=\$3

	local adm=""
	[ "\$isadmin" = "1" ] && adm="-admin"
	echo ""
	echo "Creating \$username user..."
	/bin/rm -f usersetup.txt
	[ "\$isadmin" = "1" ] && echo "configure -name $storename" >> usersetup.txt
	echo "plan create-user -name \$username \$adm -password \"\$userpass\" -wait" >> usersetup.txt
	echo "exit" >> usersetup.txt
	if [ "\$isadmin" = "1" ] ; then
		java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar runadmin -host $ipaddr -port $startport -security \$SECUREDIR/client.security load -file usersetup.txt > /tmp/setup.out.\$\$ 2>&1
	else
		java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar runadmin -host $ipaddr -port $startport -store $storename -security \$SECUREDIR/adminlogin.security load -file usersetup.txt > /tmp/setup.out.\$\$ 2>&1
	fi
	ret=\$?
	/bin/rm -f usersetup.txt
	if [ \$ret -ne 0 ] ; then
		echo ""
		cat /tmp/setup.out.\$\$
		echo ""
		echo "Error creating \$username user"
		echo ""
		exit 1
	fi

	if [ $is_enterprise -eq 1 ] ; then
		wallet="wallet"
		ptype="wallet"
		pdir="-dir \$SECUREDIR/\${username}login.wallet"
		pprop="wallet.dir=\$SECUREDIR/\${username}login.wallet"
	else
		wallet="password file"
		ptype="pwdfile"
		pdir="-file \$SECUREDIR/\${username}login.passwd"
		pprop="pwdfile.file=\$SECUREDIR/\${username}login.passwd"
	fi

	echo ""
	echo "Setting up \$username user \$wallet..."
	java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar securityconfig \
		\$ptype create \$pdir > /tmp/setup.out.\$\$ 2>&1
	java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar securityconfig \
		\$ptype secret \$pdir -set -alias \$username -secret "\$userpass" >> /tmp/setup.out.\$\$ 2>&1
	if [ \$? -ne 0 ] ; then
		echo ""
		cat /tmp/setup.out.\$\$
		echo ""
		echo "Failed to set up \$username user \$wallet"
		echo ""
		exit 1
	fi

	cat \$SECUREDIR/client.security | sed -e "s#=client.trust#=\$SECUREDIR/client.trust#" > \$SECUREDIR/\${username}login.security
	echo "oracle.kv.auth.username=\$username" >> \$SECUREDIR/\${username}login.security
	echo "oracle.kv.auth.\$pprop" >> \$SECUREDIR/\${username}login.security
}

SECUREDIR="\$KVROOT/security"

create_user admin "$admpass" 1
create_user nosql "$nosqlpass" 0
create_user proxy "$nosqlpass" 0

echo "java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar runadmin -host $ipaddr -port $startport -security \$SECUREDIR/adminlogin.security \"\\\$@\" " >> \$KVHOME/scripts/kvstore_admin.sh
echo "java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar ping -host $ipaddr -port $startport -security \$SECUREDIR/adminlogin.security \"\\\$@\" " >> \$KVHOME/scripts/kvstore_ping.sh
chmod 755 \$KVHOME/scripts/*.sh


# tar up security dir so it can be copied to other hosts
/bin/rm -f \$homedir/SEC.tar
cd \$KVROOT
tar cvf \$homedir/SEC.tar security
[ \$? -ne 0 ] && echo "Failed to create security directory" && exit 1
/bin/rm -f \$homedir/SEC.tar.gz
gzip \$homedir/SEC.tar


exit 0
EOT
	scp $sshopts $TMPDIR/nosql_setup.sh.$$ ${sshat}$host:nosql_setup.sh > /dev/null
	ssh $sshopts ${sshat}$host "chmod 755 ./nosql_setup.sh ; ./nosql_setup.sh ; ret=\$? ; rm -f ./nosql_setup.sh ; exit \$ret"
	[ $? -ne 0 ] && exit 1

	if [ $secure_store -eq 1 -a $isfirst -eq 1 ] ; then
		# scp the security info to local host so we can copy it out to others
		/bin/rm -f $TMPDIR/SEC.tar.gz.$$
		if [ $numhosts -gt 1 ] ; then
			echo ""
			echo "Copying security information to local host..."
			echo ""
			scp $sshopts ${sshat}$host:SEC.tar.gz $TMPDIR/SEC.tar.gz.$$
			if [ $? -ne 0 ] ; then
				echo ""
				echo "Error copying security information from first host to $TMPDIR/SEC.tar.gz"
				echo ""
				exit 1
			fi
		fi
	fi
}

function setup_kvstore_hosts ()
{
	clear_screen
	# use xargs to manage parallel executions
	write_words_to_file "$allhosts" $TMPDIR/hosts.$$
	cat $TMPDIR/hosts.$$ | xargs -P 30 -I HOST bash -c "copy_tarfile_to_host HOST $targzfile $sshuser \"$sshopts\""
	if [ $? -ne 0 ] ; then
		echo ""
		echo "Error copying release to host(s)"
		exit 1
	fi
	/bin/rm -f $TMPDIR/hosts.$$

	local first=1
	for host in ${hosts[*]} ; do
		setup_kvstore_host $host $first
		first=0
	done
}

function create_admin_script ()
{
	KVHOME=$installdir/kvstore

	# create script to run on first admin that creates topology/etc and fires up
	# full cluster store
	ADM=$TMPDIR/runadmin.txt.$$
	PARTITIONS=256
	arbiters=""
	[ $repfactor -eq 2 ] && arbiters="-arbiters"
	echo "configure -name $storename" > $ADM
	echo "plan deploy-zone -name ${storename}Zone -type primary $arbiters -rf $repfactor -wait" >> $ADM
	echo "pool create -name ${storename}Pool" >> $ADM
#echo "show topology -json" >> $ADM
	i=1
	for host in ${hosts[*]} ; do
		get_ipaddr_cached $host
		local ipaddr=$cached_ipaddr
		echo "plan deploy-sn -zn zn1 -host $ipaddr -port $startport -wait" >> $ADM
		echo "plan deploy-admin -sn sn$i -wait" >> $ADM
		echo "pool join -name ${storename}Pool -sn sn$i" >> $ADM
		i=$(expr $i + 1)
	done
	if [ $repfactor -eq 2 ] ; then
		for host in ${hosts[*]} ; do
			get_ipaddr_cached $host
			local ipaddr=$cached_ipaddr
			echo "plan deploy-sn -zn zn1 -host $ipaddr -port $arbport -wait" >> $ADM
			echo "pool join -name ${storename}Pool -sn sn$i" >> $ADM
			i=$(expr $i + 1)
		done
	fi
	echo "topology create -name ${storename}Topology -pool ${storename}Pool -partitions $PARTITIONS" >> $ADM
#echo "show topology -json" >> $ADM
	echo "plan deploy-topology -name ${storename}Topology -wait" >> $ADM
	echo "exit" >> $ADM

	# verbose: show script contents
	[ $verbose -eq 1 ] && cat $ADM | grep -v exit
}

function run_admin_script_on_host ()
{
	local host=$1
	local script=$2

	echo ""
	echo "Running topology setup script on $host..."
	echo ""
	local datapaths=($all_data_paths)
	if [ $numhosts -gt 1 -o ${#datapaths[*]} -gt 3 ] ; then
		echo "NOTE: this may take several minutes. Do not kill this program"
		echo "      while this step is running."
		echo ""
	fi
	get_ipaddr_cached $host
	local ipaddr=$cached_ipaddr
	scp $sshopts $script ${sshat}$host:topology_setup.txt > /dev/null
	ssh $sshopts ${sshat}$host "$KVHOME/scripts/kvstore_admin.sh load -file topology_setup.txt ; ret=\$? ; rm -f ./topology_setup.txt ; exit \$ret"
	if [ $? -ne 0 ] ; then
		echo ""
		echo "Topology setup failed. It may be possible to get more information by"
		echo "running $KVHOME/scripts/kvstore_admin.sh on $host"
		echo ""
		exit 1
	fi
}


function setup_user_privs_on_host ()
{
	local host=$1

	[ $secure_store -eq 0 ] && return

	echo ""
	echo "Setting up \"admin\" and \"nosql\" user permissions on $host..."
	echo ""

	get_ipaddr_cached $host
	local ipaddr=$cached_ipaddr
	ssh $sshopts ${sshat}$host "$KVHOME/scripts/kvstore_admin.sh -store $storename execute 'grant readwrite to user nosql' && $KVHOME/scripts/kvstore_admin.sh -store $storename execute 'grant dbadmin to user nosql'"
	if [ $? -ne 0 ] ; then
		echo ""
		echo "User permissions setup failed. It may be possible to get more information by"
		echo "running $KVHOME/scripts/kvstore_admin.sh on $host"
		echo ""
		exit 1
	fi
}

function verify_store_on_host ()
{
	local host=$1

	echo ""
	echo "sleeping for 5 seconds to allow cluster to start..."
	sleep 5
	echo ""
	get_ipaddr_cached $host
	local ipaddr=$cached_ipaddr
	echo "Pinging newly formed cluster..."
	ssh $sshopts ${sshat}$host "$KVHOME/scripts/kvstore_ping.sh"

	if [ $? -ne 0 ] ;  then
		echo ""
		echo "Cluster startup failed. Check $installdir/kvroot log files on $host."
		echo ""
		exit 1
	fi
	echo ""
	echo "Cluster startup succeeded. Running basic test to verify installation..."
	echo ""
}

function success_screen ()
{
	clear_screen
	echo ""
	echo "Cluster successfully created. Use the following parameters when"
	echo "connecting to the service via java client:"
	echo ""
	echo "   helperHosts=$helperhosts"
	echo "   storeName=$storename"
	if [ $secure_store -eq 1 ] ; then
		local pass="<not shown>"
		[ $autogen -eq 1 ] && pass="$nosqlpass"
		echo "   username=nosql"
		echo "   password=$pass"
		echo "   ssl trust file=$installdir/kvroot/security/client.trust"
		echo "   security proerties file=$installdir/kvroot/security/nosqllogin.security"
	fi
	echo ""
	echo "Start/stop scripts for NoSQL kvstore are located on host(s) at:"
	echo ""
	echo "   $installdir/kvstore/scripts/start_kvstore.sh"
	echo "   $installdir/kvstore/scripts/stop_kvstore.sh"
	echo ""
	if [ "$did_extended" = "1" ] ; then
		echo "Extended test script and TestClient.java example program are"
		echo "located at:"
		echo ""
		echo "   $installdir/kvstore/examples/TestClient"
		echo ""
	fi
}

function proxy_success_screen ()
{
	echo ""
	echo "httpproxy(s) successfully started. Use the following parameters when"
	echo "connecting to the proxy(s) using various language drivers:"
	echo ""
	http=http
	[ $secure_store -eq 1 ] && http=https
	for host in ${hosts[*]} ; do
		get_ipaddr_cached $host
		echo "   endpoint=$http://$cached_ipaddr:$proxyport"
	done
	if [ $secure_store -eq 1 ] ; then
		local pass="<not shown>"
		[ $autogen -eq 1 ] && pass="$nosqlpass"
		echo "   username=nosql"
		echo "   password=$pass"
		echo "   ssl trust file=$installdir/proxy/security/driver.trust"
		echo "   ssl certificate=$installdir/proxy/security/certificate.pem"
		echo "   certificate serverName=OracleNoSQLProxy"
	fi
	echo ""
	echo "Start/stop scripts for NoSQL httpproxy are located on host(s) at:"
	echo ""
	echo "   $installdir/proxy/scripts/start_proxy.sh"
	echo "   $installdir/proxy/scripts/stop_proxy.sh"
	echo ""
}

function run_smoke_test ()
{
	local host=$1

	echo "Running simple tests to verify cluster..."
	echo ""
	cat > $TMPDIR/nosql_smoke.sh.$$ << EOT
#!/bin/bash

# create kvstore_sql.sh on host
SQL=$installdir/kvstore/scripts/kvstore_sql.sh
echo "#!/bin/bash" > \$SQL
echo "export KVHOME=$installdir/kvstore" >> \$SQL
echo "export KVROOT=$installdir/kvroot" >> \$SQL
if [ $secure_store -eq 1 ] ; then
echo "java -jar $installdir/kvstore/lib/sql.jar -helper-hosts $helperhosts -store $storename -username nosql -security $installdir/kvroot/security/nosqllogin.security  \"\\\$@\" " >> \$SQL
else
echo "java -jar $installdir/kvstore/lib/sql.jar -helper-hosts $helperhosts -store $storename \"\\\$@\" " >> \$SQL
fi
chmod 755 \$SQL

# create simple put/get/etc
echo "CREATE TABLE IF NOT EXISTS test_data (id LONG, data JSON, PRIMARY KEY(id));" > cmds.sql
[ $is_old_community -eq 0 ] && echo "INSERT INTO test_data VALUES (12345, '{\"test\":\"data\"}');" >> cmds.sql
[ $is_old_community -eq 0 ] && echo "SELECT * FROM test_data;" >> cmds.sql
echo "DROP TABLE test_data;" >> cmds.sql
\$SQL -timeout 60000 load -file cmds.sql
ret=\$?
/bin/rm -f cmds.sql
exit \$ret
EOT
	scp $sshopts $TMPDIR/nosql_smoke.sh.$$ ${sshat}$host:nosql_smoke.sh > /dev/null
	ssh $sshopts ${sshat}$host "chmod 755 ./nosql_smoke.sh ; ./nosql_smoke.sh ; ret=\$? ; rm -f ./nosql_smoke.sh ; exit \$ret"
	if [ $? -ne 0 ] ; then
		echo ""
		echo "An error occurred while running simple SQL commands."
		echo ""
		exit 1
	fi
}

function get_proxy_port ()
{
	if [ "$is_old_community" = "1" ] ; then
		proxyport=0
		save_vals
		return
	fi

	clear_screen
	echo ""
	echo "Oracle recommends configuring a NoSQL httpproxy for use with various"
	echo "language drivers (python, go, Node.js, etc). This program can set up"
	echo "the proxy for you."
	echo ""
	echo -n "Would you like to set up an httpproxy? (y/n) [y]: "
	read yorn
	if [ "$yorn" != "" -a "$yorn" != "y" -a "$yorn" != "Y" ] ; then
		proxyport=0
		save_vals
		return
	fi
	echo ""
	echo "Enter the port number desired to run Oracle NoSQL httpproxy on. This"
	echo "port will be used by various language drivers to connect to the store."
	echo ""
	echo -n "httpproxy port"
	defprompt $proxyport
	read_integer_port $have_sudo $proxyport
	proxyport=$intport
	save_vals
}

function get_nosql_passwd ()
{
	clear_screen
	echo ""
	echo "Enter the password for the \"nosql\" user that was used in the previous"
	echo "invokation of this script to set up the cluster."
	echo ""
	echo -n "nosql user password: "
	read -s password
	[ "$password" = "" ] && exit 1
	nosqlpass="$password"
}


function setup_proxy_on_host ()
{
	local host=$1
	local isfirst=$2
	local sans="$3"

	if [ $numhosts -gt 1 -a $isfirst -eq 0 -a -s $TMPDIR/PSEC.tar.gz.$$ ] ; then
		echo ""
		echo "Copying local proxy security data to $host..."
		echo ""
		scp $sshopts $TMPDIR/PSEC.tar.gz.$$ ${sshat}$host:PSEC.tar.gz
	fi

	get_ipaddr_cached $host
	local ipaddr=$cached_ipaddr

	echo ""
	echo "Creating proxy setup script for $host..."
	echo ""
	cat > $TMPDIR/proxy_setup.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x

# autogenerated by cluster_setup

trap "/bin/rm -f /tmp/*.\$\$" exit


function test_dir ()
{
	local dir=\$1
	touch \$dir/kv.\$\$
	if [ \$? -ne 0 ] ; then
		echo ""
		iam=\$(whoami)
		echo "Error: user \$iam does not have write permission in \$dir on $host."
		echo ""
		exit 1
	fi
	/bin/rm -f \$dir/kv.\$\$
	return 0
}

function make_dir ()
{
	local dir=\$1
	[ -d \$dir ] && test_dir \$dir && return 0
	/bin/mkdir -p \$dir > /dev/null 2>&1
	[ \$? -eq 0 ] && test_dir \$dir && return 0
	[ $have_sudo -eq 0 ] && /bin/mkdir -p \$dir && return 1
	sudo /bin/mkdir -p \$dir > /dev/null 2>&1
	[ \$? -ne 0 ] && /bin/mkdir -p \$dir && return 1
	sudo chmod 755 \$dir > /dev/null 2>&1
	iam=\$(whoami)
	[ "\$iam" != "" ] && sudo chown \$iam \$dir > /dev/null 2>&1
	test_dir \$dir
	return 0
}

if [ $force_overwrite -eq 1 -a -s $installdir/proxy/scripts/stop_proxy.sh ] ; then
	$installdir/proxy/scripts/stop_proxy.sh
	sleep 1
fi

echo "Setting up httpproxy on $host..."
homedir=\`pwd\`
cd $installdir
[ \$? -ne 0 ] && exit 1
rm -rf proxy
make_dir proxy
[ \$? -ne 0 ] && echo "Can't create $installdir/proxy on $host" && exit 1
cd proxy
ln -s $installdir/kvstore/lib .
cd \$homedir
export PROXYHOME=$installdir/proxy
export KVHOME=$installdir/kvstore
export KVROOT=$installdir/kvroot
make_dir \$PROXYHOME/log
[ \$? -ne 0 ] && echo "Can't create \$PROXYHOME/log on $host" && exit 1
make_dir \$PROXYHOME/scripts
[ \$? -ne 0 ] && echo "Can't create \$PROXYHOME/scripts on $host" && exit 1

# create simple console-only logging file
PROPS=\$PROXYHOME/logging.properties
echo "handlers=java.util.logging.ConsoleHandler" > \$PROPS
echo "java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter" >> \$PROPS
echo "# ----- Define logger level -----" >> \$PROPS
echo "# By default, off" >> \$PROPS
echo "# To enable logging use one if these, in order of verbosity from" >> \$PROPS
echo "# least to most: SEVERE, WARNING, INFO, FINE, ALL" >> \$PROPS
echo "java.util.logging.ConsoleHandler.level=WARNING" >> \$PROPS
echo "oracle.nosql.level=WARNING" >> \$PROPS
echo "io.netty.level=WARNING" >> \$PROPS

if [ $secure_store -eq 1 -a $isfirst -eq 0 -a -s \$homedir/PSEC.tar.gz ] ; then
	echo ""
	echo "Unpacking security information..."
	cd \$PROXYHOME
	/bin/rm -rf security > /dev/null 2>&1
	gzip -dc \$homedir/PSEC.tar.gz | tar xvf -
	if [ \$? -ne 0 ] ; then
		echo "Error unpacking security info to \$PROXYHOME"
		echo ""
		exit 1
	fi
	PSECUREDIR=\$PROXYHOME/security
fi

if [ $secure_store -eq 1 -a $isfirst -eq 1 ] ; then
	# create proxy certificate files
	cd \$PROXYHOME
	/bin/rm -rf security > /dev/null 2>&1
	make_dir security
	[ \$? -ne 0 ] && echo "Can't create \$PROXYHOME/security on $host" && exit 1
	cd security
	cp \$KVROOT/security/client.trust .
	PSECUREDIR=\$PROXYHOME/security

	if [ $is_enterprise -eq 1 ] ; then
		wallet="wallet"
		ptype="wallet"
		pdir="-dir \$PSECUREDIR/proxylogin.wallet"
		pprop="wallet.dir=proxylogin.wallet"
	else
		wallet="password file"
		ptype="pwdfile"
		pdir="-file \$PSECUREDIR/proxylogin.passwd"
		pprop="pwdfile.file=proxylogin.passwd"
	fi

	echo ""
	echo "Setting up proxy user \$wallet..."
	echo ""
	java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar securityconfig \
		\$ptype create \$pdir > /tmp/setup.out.\$\$ 2>&1
	java -Xmx64m -Xms64m -jar \$KVHOME/lib/kvstore.jar securityconfig \
		\$ptype secret \$pdir -set -alias proxy -secret "$nosqlpass" >> /tmp/setup.out.\$\$ 2>&1
	if [ \$? -ne 0 ] ; then
		echo ""
		cat /tmp/setup.out.\$\$
		echo ""
		echo "Failed to set up proxy user \$wallet"
		echo ""
		exit 1
	fi

	cp \$KVROOT/security/client.security \$PSECUREDIR/proxylogin.security
	echo "oracle.kv.auth.username=proxy" >> \$PSECUREDIR/proxylogin.security
	echo "oracle.kv.auth.\$pprop" >> \$PSECUREDIR/proxylogin.security

	echo ""
	echo "Setting up proxy SSL certificate..."
	echo ""

	echo "$nosqlpass" > tmppass
	chmod 600 tmppass

	# get openssl default config file location: OPENSSLDIR
	source <(openssl version -d | sed -e 's/: /=/')
	[ \$OPENSSLDIR = "" ] && OPENSSLDIR="/etc/ssl"

	# create a self-signed cert. Add our internal IP addresses and hostnames to
	# a SAN field.
	openssl req -x509 -days 365 -newkey rsa:4096 -keyout key.pem -out certificate.pem -subj "/C=US/ST=CA/L=San/O=Oracle Corp./CN=OracleNoSQLProxy" -extensions NOSQLSAN -config <(cat \$OPENSSLDIR/openssl.cnf <(printf "[NOSQLSAN]\\nsubjectAltName=$sans")) -passout file:tmppass > /tmp/openssl.out.\$\$ 2>&1
	if [ \$? -ne 0 ] ; then
		echo ""
		cat /tmp/openssl.out.\$\$
		echo ""
		echo "Error setting up self-signed SSL certificate."
		echo ""
		/bin/rm -f tmppass
		exit 1
	fi

	cp tmppass tmppass2
	chmod 600 tmppass2
	openssl pkcs8 -topk8 -inform PEM -outform PEM -in key.pem -out key-pkcs8.pem -passin file:tmppass -passout file:tmppass2 > /tmp/openssl.out.\$\$ 2>&1
	if [ \$? -ne 0 ] ; then
		echo ""
		cat /tmp/openssl.out.\$\$
		echo ""
		echo "Error setting up certificate PEM key file."
		echo ""
		/bin/rm -f tmppass tmppass2
		exit 1
	fi

	/bin/rm -f tmppass tmppass2
	keytool -import -alias proxy -noprompt -trustcacerts -keystore driver.trust -file certificate.pem -storepass "$nosqlpass" > /tmp/keytool.out.\$\$ 2>&1
	if [ \$? -ne 0 ] ; then
		echo ""
		cat /tmp/keytool.out.\$\$
		echo ""
		echo "Error setting up driver.trust keystore."
		echo ""
		exit 1
	fi

	echo "storeSecurityFile=\$PSECUREDIR/proxylogin.security" > proxy.config
	echo "sslCertificate=\$PSECUREDIR/certificate.pem" >> proxy.config
	echo "sslPrivateKey=\$PSECUREDIR/key-pkcs8.pem" >> proxy.config
	echo "sslPrivateKeyPass=$nosqlpass" >> proxy.config
	echo "httpsPort=$proxyport" >> proxy.config
	chmod 600 proxy.config

	echo ""
	echo "Creating proxy security config data for other hosts..."
	echo ""
	cd \$PROXYHOME
	/bin/rm -f \$homedir/PSEC.tar
	tar cvf \$homedir/PSEC.tar security
	/bin/rm -f \$homedir/PSEC.tar.gz
	gzip \$homedir/PSEC.tar
fi

if [ $secure_store -eq 1 ] ; then
	SECARGS="-config \$PSECUREDIR/proxy.config"
else
	SECARGS="-httpPort $proxyport"
fi

# create proxy start/stop scripts
SCR=\$PROXYHOME/scripts/start_proxy.sh
echo "#!/bin/bash" > \$SCR
echo "export PROXYHOME=$installdir/proxy" >> \$SCR
echo "cd \$PROXYHOME/log" >> \$SCR
dosudo=""
[ $proxyport -le 1024 -a $have_sudo -eq 1 ] && dosudo="sudo"
echo "nohup \$dosudo java -Djava.util.logging.config.file=\$PROPS -jar \$PROXYHOME/lib/httpproxy.jar -hostname $ipaddr -storeName $storename -helperHosts "$helperhosts" -proxyType KVPROXY \$SECARGS < /dev/null > httpproxy.out 2>&1 &" >> \$SCR
chmod 755 \$SCR

SCR=\$PROXYHOME/scripts/stop_proxy.sh
echo "#!/bin/bash" > \$SCR
echo "export PROXYHOME=$installdir/proxy" >> \$SCR
echo "\$dosudo pkill -f 'java.*httpproxy.jar'" >> \$SCR
chmod 755 \$SCR

# start proxy
echo ""
echo "Starting proxy on $host..."
\$PROXYHOME/scripts/start_proxy.sh
sleep 5

echo ""
echo "Validating proxy..."
np=\$(ps auxww | grep 'java.*httpproxy' | grep KVPROXY | wc -l)
if [ \$np -eq 0 ] ; then
	tail -3 \$PROXYHOME/log/httpproxy.out
	echo ""
	echo "httpproxy did not start on $host. Check \$PROXYHOME/log/httpproxy.out for details."
	exit 1
fi

echo ""
echo "httpproxy on $host started."

# remove distributed runProxy script, superceded by start_proxy.sh
/bin/rm -f \$PROXYHOME/runProxy

exit 0
EOT
	scp $sshopts $TMPDIR/proxy_setup.sh.$$ ${sshat}$host:proxy_setup.sh > /dev/null
	ssh $sshopts ${sshat}$host "chmod 755 ./proxy_setup.sh ; ./proxy_setup.sh ; ret=\$? ; rm -f ./proxy_setup.sh ; exit \$ret"
	if [ $? -ne 0 ] ; then
		echo ""
		echo "httpproxy setup failed."
		if [ $secure_store -eq 1 ] ; then
			echo ""
			echo "See https://docs.oracle.com/en/database/other-databases/nosql-database/19.5/admin/secure-proxy.html for information on how to configure a secure proxy."
		fi
		echo ""
		exit 1
	fi

	if [ $numhosts -gt 1 -a $secure_store -eq 1 -a $isfirst -eq 1 ] ; then
		echo ""
		echo "Copying proxy security data to local host..."
		echo ""
		/bin/rm -f $TMPDIR/PSEC.tar.gz.$$
		scp $sshopts ${sshat}$host:PSEC.tar.gz $TMPDIR/PSEC.tar.gz.$$
	fi
}

function check_is_ipaddr ()
{
	# return 1 if the string is a dotted-quad IPv4 address
	if [[ $1 =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]] ; then
		return 1
	fi
	return 0
}

function setup_proxy_on_hosts ()
{
	# set up subjectAltNames strings based on hostnames/ips
	local sans="DNS:OracleNoSQLProxy,DNS:localhost,IP:127.0.0.1"
	for host in ${hosts[*]} ; do
		get_ipaddr_cached $host
		sans="${sans},IP:$cached_ipaddr"
		if [ "$host" != "$cached_ipaddr" ] ; then
			check_is_ipaddr $host
			if [ $? -eq 1 ] ; then
				sans="${sans},IP:$host"
			else
				sans="${sans},DNS:$host"
			fi
		fi
	done

	local first=1
	for host in ${hosts[*]} ; do
		setup_proxy_on_host $host $first "$sans"
		first=0
	done
}

function run_extended_test ()
{
	did_extended=0
	echo ""
	echo ""
	echo "Would you like to run an extended test of the cluster? This will"
	echo "take a few minutes, and give you some basic performance indicators."
	echo ""
	if [ $dotest -eq 0 ] ; then
		echo -n "Run extended test? (y/n) [y]: "
		read yorn
		[ "$yorn" = "n" -o "$yorn" = "N" ] && return
	fi

	echo ""
	echo "Unpacking test java files..."
cat > $TMPDIR/TC.b64 << CLIENTEOT
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
CLIENTEOT

	# determine correct options for base64
	b64opts="-D"
	echo Zm9vCg== | base64 $b64opts > /dev/null 2>&1
	if [ $? -ne 0 ] ; then
		# try "-d"
		b64opts="-d"
		echo Zm9vCg== | base64 $b64opts > /dev/null 2>&1
		if [ $? -ne 0 ] ; then
			echo "Error: can't unpack test java files: no viable 'base64' program available."
			echo "Skipping extended test."
			[ $dotest -eq 1 ] && exit 1
			return
		fi
	fi

	cat $TMPDIR/TC.b64 | base64 $b64opts > $TMPDIR/TC.tar.gz
	if [ $? -ne 0 ] ; then
		/bin/rm -f $TMPDIR/TC.b64
		echo "Error unpacking test files: skipping extended test."
		[ $dotest -eq 1 ] && exit 1
		return
	fi
	/bin/rm -f $TMPDIR/TC.b64

	echo ""
	host=${hosts[0]}
	echo "Copying test files to $host..."
	scp $sshopts $TMPDIR/TC.tar.gz ${sshat}$host:/tmp/TC.tar.gz
	if [ $? -ne 0 ] ; then
		/bin/rm -f $TMPDIR/TC.tar.gz
		echo "Error copying test files: skipping extended test."
		[ $dotest -eq 1 ] && exit 1
		return
	fi
	/bin/rm -f $TMPDIR/TC.tar.gz

	ssh $sshopts ${sshat}$host "cd $installdir/kvstore && mkdir -p examples/TestClient && cd examples/TestClient && gzip -dc /tmp/TC.tar.gz | tar xvf - && /bin/rm -f /tmp/TC.tar.gz"
	if [ $? -ne 0 ] ; then
		echo "Error copying test files: skipping extended test."
		[ $dotest -eq 1 ] && exit 1
		return
	fi

	local helpers=""
	for ihost in ${hosts[*]} ; do
		get_ipaddr_cached $ihost
		helpers="${helpers} -helper $cached_ipaddr:$startport"
	done
	local security=""
	if [ $secure_store -eq 1 ] ; then
		security="-security $installdir/kvroot/security/nosqllogin.security"
	fi

cat > $TMPDIR/run_test_client.sh.$$ << EOT
#!/bin/bash
[ $debug -eq 1 ] && set -x
[ -s ~/.bashrc ] && source ~/.bashrc > /dev/null 2>&1
[ -s ~/.bash_profile ] && source ~/.bash_profile > /dev/null 2>&1
cd $installdir/kvstore/examples/TestClient
java -cp .:$installdir/kvstore/lib/kvstore.jar TestClient -store $storename -samples 20000 -maxrecords 20000 $helpers $security
EOT
	scp $sshopts $TMPDIR/run_test_client.sh.$$ ${sshat}$host:$installdir/kvstore/examples/TestClient/run_test_client.sh
	if [ $? -ne 0 ] ; then
		/bin/rm -f $TMPDIR/run_test_client.sh.$$
		echo "Error copying test files: skipping extended test."
		[ $dotest -eq 1 ] && exit 1
		return
	fi
	/bin/rm -f $TMPDIR/run_test_client.sh.$$

	did_extended=1
	clear_screen
	echo ""
	echo "Running extended test on $host..."
	ssh $sshopts ${sshat}$host "chmod 755 $installdir/kvstore/examples/TestClient/run_test_client.sh ; $installdir/kvstore/examples/TestClient/run_test_client.sh"
	if [ $? -ne 0 ] ; then
		echo "Error running extended test on $host"
		[ $dotest -eq 1 ] && exit 1
		return
	fi
	echo ""
	echo "Extended test ran successfully."
	echo ""
	if [ $dotest -eq 0 ] ; then
		echo -n "Hit <enter> to continue: "
		read notused
	fi
}


function cleanup_temp_files ()
{
	echo ""
	echo "Cleaning up temporary files..."
	/bin/rm -f $TMPDIR/*.$$
	for host in ${hosts[*]} ; do
		ssh $sshopts ${sshat}$host "/bin/rm -f myip SEC.tar.gz PSEC.tar.gz"
	done
}

function setup_helper_hosts ()
{
	helperhosts=""
	for host in ${hosts[*]} ; do
		get_ipaddr_cached $host
		helperhosts="${helperhosts}$cached_ipaddr:$startport,"
	done
}


# Program Main starts here


will_overwrite=0
if [ $dotest -eq 1 ] ; then
	source $test_vars
	hosts=($allhosts)
	numhosts=${#hosts[*]}
	sshat=
	[ "$sshuser" != "" ] && sshat="${sshuser}@"
	[ $debug -eq 1 ] && set -x
	get_targzfile
else
	splash_screen
	get_prev_settings
	get_targzfile
	get_hosts
	get_installdir
	get_storename
	setup_raw_oci_drives
	get_data_paths
	get_log_paths
	get_start_port
	get_rep_factor
fi
max_host_capacity=0
set_min_capacity
collect_all_hosts_data
get_secure_passwds
calculate_port_ranges
setup_helper_hosts
if [ $proxy_only -eq 0 ] ; then
check_network_connectivity
show_parameters
setup_kvstore_hosts
create_admin_script
run_admin_script_on_host ${hosts[0]} $TMPDIR/runadmin.txt.$$
setup_user_privs_on_host ${hosts[0]}
verify_store_on_host ${hosts[0]}
run_smoke_test ${hosts[0]}
askcontinue any
fi

# proxy setup
[ $dotest -eq 0 ] && get_proxy_port
if [ "$proxyport" = "" -o "$proxyport" = "0" ] ; then
	run_extended_test
	cleanup_temp_files
	success_screen
	exit 0
fi
[ $proxy_only -eq 1 -a "$nosqlpass" = "" -a $dotest -eq 0 ] && get_nosql_passwd
setup_proxy_on_hosts
run_extended_test
cleanup_temp_files
success_screen
proxy_success_screen

