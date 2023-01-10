#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvclient.jar
nohup java -jar $KVHOME/lib/kvstore.jar restart -disable-services -root $KVROOT >/dev/null 2>&1 </dev/null &
sleep 5
sudo netstat -ntpl | grep 50
