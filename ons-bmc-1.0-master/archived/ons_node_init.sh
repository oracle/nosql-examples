#!/bin/bash

#  ons_node_init.sh
#  Oracle NoSQL Database on Oracle Bare Metal Cloud
#
#  Created by Rick George 2016-2017
#  All rights reserved

# update ulimits

sudo su -c 'printf "opc\t\tsoft\tnofile\t10240\n" >> /etc/security/limits.conf'
sudo su -c 'printf "opc\t\thard\tnofile\t10240\n" >> /etc/security/limits.conf'
sudo su -c 'printf "opc\t\tsoft\tnproc\t16384\n" >> /etc/security/limits.conf'
sudo su -c 'printf "opc\t\thard\tnproc\t16384\n" >> /etc/security/limits.conf'

exit 0