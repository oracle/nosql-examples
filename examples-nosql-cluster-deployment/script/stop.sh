#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvclient.jar
java -jar $KVHOME/lib/kvstore.jar stop -root $KVROOT
