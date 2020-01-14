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
	[ "$yorn" != "y" ] && exit 0
}

function save_vals ()
{
  echo "targzfile=$targzfile" > $SVARS
  echo "proxygzfile=$proxygzfile" >> $SVARS
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
	[ "$yorn" != "y" ] && /bin/rm -f $SVARS && return
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
	echo " 3) disk/nvme data drives set up on host machine(s)"
	echo " 4) Java 8+ installed on host machine(s)"
	echo ""
	echo " Optional:"
	echo " 5) a downloaded Oracle NoSQL httpproxy .tar.gz or .zipfile"
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
	is_community=0
	[[ $targzfile =~ kv-ee- ]] && is_enterprise=1
	[[ $targzfile =~ kv-ce- ]] && is_community=1
	save_vals
}

function get_proxygzfile () 
{
	clear_screen
	if [ "$proxygzfile" = "" ] ; then
		echo ""
		echo "Optional: hit <enter> to skip this step"
	fi
	echo ""
	echo "Enter path to the locally downloaded Oracle NoSQL httpproxy "
	echo "tar.gz or zip file (ex: oracle-nosql-proxy-5.1.10.tar.gz)"
	echo ""
	echo -n "tar.gz/.zip file"
	defprompt $proxygzfile
	if [ $dotest -eq 0 ] ; then
		read -e pfile
		[ "$pfile" = "" ] && pfile=$proxygzfile
		proxygzfile=$pfile
	fi
	if [ "$proxygzfile" != "" ] ; then
		save_vals
		# expand "~", etc
		source $SVARS
		get_topdir "$proxygzfile" httpproxy.jar
		proxygztop=$topdir
	fi
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
	local host=\$1

	# note local vs remote substitution here
	[ "$javahome" != "" ] && javahome=$javahome && return 0

	echo "Determining JAVA_HOME on $host..."

	javahome=\$(java -XshowSettings:properties -version 2>&1 | grep 'java.home = ' | awk '{print \$3}')
	[ "\$javahome" != "" ] && return 0

	local jp=""
	which java >/dev/null 2>&1
	if [ \$? -eq 0 ] ; then
		jp=\$(which java)
	fi
	for i in \$jp /usr/local/bin/java /opt/bin/java /usr/bin/java /bin/java ; do
		javahome=\$(readlink -e \$i 2>/dev/null)
		[ "\$javahome" != "" ] && break
	done
	if [ "\$javahome" = "" ] ; then
		echo ""
		echo -n "Enter the path to JAVA_HOME on $host: "
		read javahome
	else
		javahome=\$(echo \$jhome | sed -e 's#/bin/java##')
	fi
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
if [ \$version -lt 8 -o \$version -gt 13 ] ; then
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
			[ "$yorn" != "y" ] && exit 1
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

	clear_screen
	echo ""
	echo "This program will set up a secure store. If you do not want a secure"
	echo "store, enter \"n\" at the prompt below."
	echo ""
	echo -n "Set up secure store (y/n) [y]: "
	read yorn
	if [ "$yorn" != "" -a "$yorn" != "y" ] ; then
		echo ""
		echo "Are you sure you want to set up an unrestricted, insecure store?"
		echo ""
		echo -n "Set up insecure store? (y/n) [n]: "
		read yorn
		if [ "$yorn" = "y" ] ; then
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
	if [ "$yorn" != "n" ] ; then
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

function set_min_capcity ()
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

function check_network_connectivity ()
{
	[ $numhosts -le 1 ] && return
	[ $force_overwrite -eq 1 ] && return
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
		[ "$yorn" != "y" ] && return
	fi
	[ "$do_network" = "no" ] && return

	if [ $have_nc -eq 0 ] ; then
		for host in ${hosts[*]} ; do
			verify_nc_installed $host
			[ $? -ne 0 ] && return
		done
	fi

	# Method 1: check each connection one by one in series
	#for host in ${hosts[*]} ; do
		#check_network_host $host
	#done

	#return

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

function show_parameters ()
{
	echo ""
	echo "Parameters that will be used for cluster setup:"
	echo ""
	echo "  hosts:      ${hosts[*]}"
	echo "  ipaddrs:    ${ipaddrs[*]}"
	echo "  installdir: $installdir"
	echo "  mainport:   $startport"
	echo "  port_range: ${startport}-$arbservhigh"
	echo "  datapaths:  $all_data_paths"
	echo "  logpaths:   $all_log_paths"
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
export JAVA_HOME=$javahome
export PATH=$javahome/bin:\$PATH
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


# Create start/stop/admin scripts, execute start script
SCR=\$KVHOME/scripts/start_kvstore.sh
echo "#!/bin/bash" > \$SCR
echo "export JAVA_HOME=$javahome" >> \$SCR
echo "export PATH=$javahome/bin:\$PATH" >> \$SCR
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
	if [ $numhosts -gt 1 ] ; then
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
echo "export JAVA_HOME=$javahome" >> \$SQL
echo "export PATH=$javahome/bin:\$PATH" >> \$SQL
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
[ $is_community -eq 0 ] && echo "INSERT INTO test_data VALUES (12345, '{\"test\":\"data\"}');" >> cmds.sql
[ $is_community -eq 0 ] && echo "SELECT * FROM test_data;" >> cmds.sql
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
	clear_screen
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
	proxygzbase=$(basename $proxygzfile)

	
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

echo "Unpacking $proxygzbase on $host..."
make_dir $installdir
[ \$? -ne 0 ] && echo "Can't create $installdir on $host" && exit 1
homedir=\`pwd\`
cd $installdir
[ \$? -ne 0 ] && exit 1
if [[ $proxygzfile =~ \.zip\$ ]] ; then
unzip -o \$homedir/$proxygzbase > /tmp/unpack.out.\$\$ 2>&1
else
gzip -dc \$homedir/$proxygzbase | tar xvf - > /tmp/unpack.out.\$\$ 2>&1
fi
if [ \$? -ne 0 ] ; then
cat /tmp/unpack.out.\$\$
echo "Error unpacking $proxygzbase on $host"
exit 1
fi

if [ $force_overwrite -eq 1 -a -s $installdir/proxy/scripts/stop_proxy.sh ] ; then
	$installdir/proxy/scripts/stop_proxy.sh
	sleep 1
fi

echo "Validating Oracle NoSQL installation..."
proxydir=$proxygztop
cd $installdir
[ -L proxy ] && rm -f proxy
ln -s \$proxydir proxy
cd \$homedir
export JAVA_HOME=$javahome
export PATH=$javahome/bin:\$PATH
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
echo "export JAVA_HOME=$javahome" >> \$SCR
echo "export PATH=$javahome/bin:\$PATH" >> \$SCR
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
	# use xargs to manage parallel executions
	write_words_to_file "$allhosts" $TMPDIR/hosts.$$
	cat $TMPDIR/hosts.$$ | xargs -P 30 -I HOST bash -c "copy_tarfile_to_host HOST $proxygzfile $sshuser \"$sshopts\""
	if [ $? -ne 0 ] ; then
		echo ""
		echo "Error copying httpproxy release to host(s)"
		exit 1
	fi
	/bin/rm -f $TMPDIR/hosts.$$

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
		[ "$yorn" = "n" ] && return
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
	get_proxygzfile
else
	splash_screen
	get_prev_settings
	get_targzfile
	get_proxygzfile
	get_hosts
	get_installdir
	get_storename
	get_data_paths
	get_log_paths
	get_start_port
	get_rep_factor
fi
max_host_capacity=0
set_min_capcity
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
fi

if [ "$proxygzfile" = "" ] ; then
	run_extended_test
	cleanup_temp_files
	success_screen
	exit 0
fi

echo "Next: httpproxy setup"
echo ""
askcontinue y

# proxy setup
[ $dotest -eq 0 ] && get_proxy_port
setup_proxy_on_hosts
run_extended_test
cleanup_temp_files
success_screen
proxy_success_screen

