#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

KVNODES=(node1-nosql node2-nosql node3-nosql node4-nosql node5-nosql node6-nosql arbiter-nosql)
KVDATA=(${KVDATA}/disk1 ${KVDATA}/disk2 ${KVDATA}/disk3)

CONFIG_FILE=${1-config1x3}
GEN_CONFIG_FILE=${CONFIG_FILE}

echo "Generating file ${GEN_CONFIG_FILE}.kvs from template_${CONFIG_FILE}.kvs"

cp template_${CONFIG_FILE}.kvs ${GEN_CONFIG_FILE}.kvs
sed -i "s/<HERE>/$KVSTORE/g" ${GEN_CONFIG_FILE}.kvs
sed -i "s/<KVSTORE>/$KVSTORE/g" ${GEN_CONFIG_FILE}.kvs
for index in "${!KVNODES[@]}"; 
do 
  echo "Modifying <HOST_$(($index + 1))> with ${KVNODES[$index]}"
  sed -i "s~<KVNODE_$(($index + 1))>~${KVNODES[$index]}~g" ${GEN_CONFIG_FILE}.kvs
done 

for index in "${!KVDATA[@]}"; 
do
  echo "Modifying <KVDATA_$(($index + 1))> with ${KVDATA[$index]}"
  sed -i "s~<KVDATA_$(($index + 1))>~${KVDATA[$index]}~g" ${GEN_CONFIG_FILE}.kvs
done

