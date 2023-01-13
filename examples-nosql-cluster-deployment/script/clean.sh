#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvclient.jar

echo
echo "Deleting all information in the following directories - \$KVROOT, \$KVDATA, \$KVXRS, \$PROXYHOME "
rm -rf $KVROOT
rm -rf $KVDATA
rm -rf $KVXRS
rm -rf $PROXYHOME

echo
echo "Recreating the directories - \$KVROOT, \$KVDATA, \$KVXRS, \$PROXYHOME "
echo "     We are simulating multiple drivers but using the same mount point (\${KVDATA}/disk1, \${KVDATA}/disk2, \${KVDATA}/disk3)"
mkdir -p ${KVROOT}
mkdir -p ${KVDATA}
mkdir -p ${KVDATA}/disk1
mkdir -p ${KVDATA}/disk2
mkdir -p ${KVDATA}/disk3
mkdir -p $KVXRS
mkdir -p ${PROXYHOME}

mkdir -p $HOME/kvstore_export
