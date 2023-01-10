#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

nohup java -Xms256m -Xmx2048m -jar $KVHOME/lib/kvstore.jar xrstart -config $KVXRS/json.config  > $KVXRS/nohup.out &
