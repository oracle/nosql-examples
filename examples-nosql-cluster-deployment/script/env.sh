#!/bin/bash
#
# Copyright (c) 2023, 2024 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

export KVHOME=$HOME/nosql/kv-24.3.9

export KVROOT=$HOME/nosql/kvroot
export KVDATA=$HOME/nosql/data
export KVXRS=$HOME/xrshome
export PROXYHOME=$HOME/proxy

export KVHOST=`hostname`
export KVSTORE=OUG
export PROXYPORT=8080
export PROXYPORTSEC=3000

alias kv_sql="java -jar $KVHOME/lib/sql.jar -helper-hosts $KVHOST:5000 -store $KVSTORE "
alias kv_admin="java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host $KVHOST"
alias kv_ping="java -jar $KVHOME/lib/kvstore.jar ping  -port 5000 -host $KVHOST"
alias kv_proxy="java -jar $KVHOME/lib/httpproxy.jar -helperHosts $KVHOST:5000 -storeName $KVSTORE -httpPort $PROXYPORT -verbose true"
alias kv_proxy_sec="java -jar $KVHOME/lib/httpproxy.jar -storeName $KVSTORE -helperHosts $KVHOST:5000 -httpsPort $PROXYPORTSEC -storeSecurityFile $PROXYHOME/proxy.login -sslCertificate $PROXYHOME/certificate.pem  -sslPrivateKey $PROXYHOME/key-pkcs8.pem  -sslPrivateKeyPass \`cat $PROXYHOME/pwd\` -verbose true"


echo "Directories paths"
echo "- \$KVHOME=$KVHOME"
echo "- \$KVROOT=$KVROOT"
echo "- \$KVDATA=$KVDATA"
echo "     We are simulating multiple drivers but using the same mount point (\${KVDATA}/disk1, \${KVDATA}/disk2, \${KVDATA}/disk3)"
echo "- \$PROXYHOME=$PROXYHOME"
echo "- \$KVXRS=$KVXRS"

echo "KVStore information"
echo "- \$KVHOST=$KVHOST"
echo "- \$KVSTORE=$KVSTORE"
echo "- \$PROXY_URL=http://$HOSTNAME:$PROXYPORT"
echo "- \$PROXY_SEC_URL=https://$HOSTNAME:$PROXYPORTSEC"
