#!/bin/bash

######## Reset the Environment ###############
./resetEnv.sh

# We are simulating a KVStore with 3 storage nodes each having capacity of 3
# In this example we are mapping a folder to a storage node, it is best if you we use seperate disk for each storage node.

SN1_DATA_HOME=/tmp/data/sn1
SN2_DATA_HOME=/tmp/data/sn2
SN3_DATA_HOME=/tmp/data/sn3

#Use the makebootconfig utility to generate the configuration. We do this for each storage Node
# -root the KVROOT directory
# -host node hostname or IP address
# -port specifies the registry port used by the Storage Node Agent
# -admin specifies the port used by Administration Service to listen for HTTP connections
# -harange specifies the range port used by the replication nodes and Administration services
# -capacity specifies number of a Replication Nodes supported by Storage Node
# -num_cpus used when multiple Replication Nodes are configured on a Storage Node. If the value is 0, the system will attempt to query the Storage Node to determine the number of processors on the machine.
# -memory_mb total memory available for the cache and heap sizes. If the value is 0, the store will attempt to determine the amount of memory on the machine, but that value is only available when the JVM used is the Oracle Hotspot JVM.
# -store-security specifies if security will be used or not. In this example no security is used

########### Bootstrap Storage Node 1 ##########
java -jar $KVHOME/lib/kvstore.jar makebootconfig \
	-root $SN1_DATA_HOME/kvroot \
	-store-security none \
	-capacity 3 \
	-harange 5010,5030 \
	-port 5000 \
	-memory_mb 200\
	-host kvhost01 \
	-storagedir $SN1_DATA_HOME/u01 \
	-storagedir $SN1_DATA_HOME/u02 \
	-storagedir $SN1_DATA_HOME/u03 \

echo " Done bootstrapping storage-node 1"
################################################

########### Bootstrap Storage Node 2 ###########
java -jar $KVHOME/lib/kvstore.jar makebootconfig \
        -root $SN2_DATA_HOME/kvroot \
	-store-security none \
        -capacity 3 \
        -harange 6010,6030 \
	-port 6000 \
	-memory_mb 200\
        -host kvhost02 \
        -storagedir $SN2_DATA_HOME/u01 \
        -storagedir $SN2_DATA_HOME/u02 \
        -storagedir $SN2_DATA_HOME/u03 \

echo " Done bootstrapping storage-node 2"
################################################

############ Bootstrap Storage Node 3 ##########
java -jar $KVHOME/lib/kvstore.jar makebootconfig \
        -root $SN3_DATA_HOME/kvroot \
	-store-security none \
        -capacity 3 \
        -harange 7010,7030 \
        -port 7000 \
	-memory_mb 200\
        -host kvhost03 \
        -storagedir $SN3_DATA_HOME/u01 \
        -storagedir $SN3_DATA_HOME/u02 \
        -storagedir $SN3_DATA_HOME/u03 \

echo " Done bootstrapping storage-node 3"
################################################
