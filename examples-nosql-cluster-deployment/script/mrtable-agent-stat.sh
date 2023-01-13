#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

echo "connect store -name $KVSTORE -host localhost -port 5000" > mrtable-agent.kvs
echo "show mrtable-agent-statistics -agent 0 -json" >> mrtable-agent.kvs
java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host localhost  load -file  mrtable-agent.kvs | grep -v Connected | jq '. "returnValue"[]."statistics"."regionStat"[]."laggingMs"."max"'

