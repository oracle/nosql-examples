#!/bin/bash

SN5_DATA_HOME=/tmp/data/sn5

########### Bootstrap Storage Node 5    #######
java -jar $KVHOME/lib/kvstore.jar makebootconfig \
        -root $SN5_DATA_HOME/kvroot \
	-store-security none \
        -capacity 4 \
        -harange 4010,4030 \
        -port 4000 \
        -memory_mb 200\
        -host kvhost05 \
        -storagedir $SN5_DATA_HOME/u01 \
        -storagedir $SN5_DATA_HOME/u02 \
        -storagedir $SN5_DATA_HOME/u03 \
        -storagedir $SN5_DATA_HOME/u04 \

echo " Done bootstrapping storage-node 5"
################################################

