#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvclient.jar

java -jar $KVHOME/lib/kvstore.jar makebootconfig \
-root $KVROOT \
-port 5000 \
-host $KVHOST \
-harange 5010,5020 \
-servicerange 5021,5049 \
-admin-web-port 5999 \
-store-security none \
-mgmt jmx \
-capacity 1 \
-storagedir ${KVDATA}/disk1 \
-storagedirsize 5500-MB

nohup java -jar $KVHOME/lib/kvstore.jar start -root $KVROOT >/dev/null 2>&1 </dev/null &
sleep 5
sudo netstat -ntpl | grep 50

