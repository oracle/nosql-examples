#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvclient.jar

mkdir $KVROOT/${1-50}
mkdir ${KVDATA}/disk1/${1-50}

java -jar $KVHOME/lib/kvstore.jar makebootconfig \
-root $KVROOT/${1-50} \
-port ${1-50}00 \
-host $KVHOST \
-harange ${1-50}10,${1-50}20 \
-servicerange ${1-50}21,${1-50}49 \
-store-security none \
-mgmt jmx \
-capacity 1 \
-memory_mb 1024 \
-storagedir ${KVDATA}/disk1/${1-50} \
-storagedirsize 5500-MB


nohup java -jar $KVHOME/lib/kvstore.jar start -root $KVROOT/${1-50}  >/dev/null 2>&1 </dev/null &
sleep 5
netstat -ntpl | grep ${1-50}
