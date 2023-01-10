#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

java -jar $KVHOME/lib/httpproxy.jar \
-storeName $KVSTORE \
-helperHosts $KVHOST:5000 \
-httpsPort 3000 \
-storeSecurityFile $PROXYHOME/proxy.login \
-sslCertificate $PROXYHOME/certificate.pem \
-sslPrivateKey $PROXYHOME/key-pkcs8.pem \
-sslPrivateKeyPass `cat $PROXYHOME/pwd` \
-verbose true
