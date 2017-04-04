#!/bin/bash

SN4_DATA_HOME=/tmp/data/sn4

########### Bootstrap Storage Node 4 ##########
java -jar $KVHOME/lib/kvstore.jar makebootconfig \
        -root $SN4_DATA_HOME/kvroot \
	-store-security none \
        -capacity 3 \
        -harange 8010,8030 \
        -port 8000 \
        -memory_mb 200\
        -host kvhost04 \
        -storagedir $SN4_DATA_HOME/u01 \
        -storagedir $SN4_DATA_HOME/u02 \
        -storagedir $SN4_DATA_HOME/u03 \

echo " Done bootstrapping storage-node 4"
################################################

