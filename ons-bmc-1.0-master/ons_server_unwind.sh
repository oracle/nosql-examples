#!/bin/bash

#  ons_server_unwind.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

TMP=/tmp/ons
mkdir -p $TMP

# uninstall nosql

INSTALL_DIR=/var/ons
sudo rm -rf $INSTALL_DIR

rm -rf ~/ons

sed -i '/KVHOME/d' ~/.bashrc
sed -i '/KVROOT/d' ~/.bashrc
sed -i '/sql.jar/d' ~/.bashrc

# undo changes to /etc/hosts

sudo sed -i.bak '/#@dbhost/d' /etc/hosts

# cleanup files

rm -f ~/.jline-oracle.kv*

exit 0