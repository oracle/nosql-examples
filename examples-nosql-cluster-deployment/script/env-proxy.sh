#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

export KVHOME=/home/opc/nosql/kv-22.2.13
export KVHOST=`hostname`
export KVSTORE=OUG
export PROXYHOME=/home/opc/proxy
export PROXYPORT=8080
export PROXYPORTSEC=3000


echo $KVHOME
echo $KVHOST
echo $KVSTORE

alias kv_proxy="java -jar $KVHOME/lib/httpproxy.jar -helperHosts $KVHOST:5000 -storeName $KVSTORE -httpPort $PROXYPORT -verbose true"
alias kv_proxy_sec="java -jar $KVHOME/lib/httpproxy.jar -storeName $KVSTORE -helperHosts $KVHOST:5000 -httpsPort $PROXYPORTSEC -storeSecurityFile $PROXYHOME/proxy.login -sslCertificate $PROXYHOME/certificate.pem  -sslPrivateKey $PROXYHOME/key-pkcs8.pem  -sslPrivateKeyPass \`cat $PROXYHOME/pwd\` -verbose true"


