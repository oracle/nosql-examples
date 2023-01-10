#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

SCRIPTDEPLOY=${1-multi-zone-with-arb.kvs}

java -Xmx64m -Xms64m -jar  $KVHOME/lib/kvstore.jar runadmin -port 5000 -host $KVHOST -security $KVROOT/security/client.security << EOF
load -file $SCRIPTDEPLOY;
EOF

echo "Generation password for user root"
openssl rand -out /tmp/tempwd -base64 14
TMPPWD=`cat /tmp/tempwd`


java -Xmx64m -Xms64m -jar  $KVHOME/lib/kvstore.jar runadmin -port 5000 -host $KVHOST -security $KVROOT/security/client.security << EOF
execute 'CREATE USER root IDENTIFIED BY \"${TMPPWD}12aB@@\" ADMIN';
EOF

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvstore.jar securityconfig wallet create -dir $KVROOT/security/root.passwd
java -Xmx64m -Xms64m -jar $KVHOME/lib/kvstore.jar securityconfig wallet secret -dir $KVROOT/security/root.passwd -set -alias root -secret "${TMPPWD}12aB@@"

cp $KVROOT/security/client.security $KVROOT/security/root.login

echo "oracle.kv.auth.username=root" >> $KVROOT/security/root.login
#echo "oracle.kv.auth.pwdfile.file=root.passwd" >> $KVROOT/security/root.login
echo "oracle.kv.auth.wallet.dir=root.passwd" >> $KVROOT/security/root.login

cd $KVROOT/security; zip -r root.zip root.* client.trust ; cd -; 

echo "Generation password for user proxy_user"
openssl rand -out /tmp/tempwd -base64 14
TMPPWD=`cat /tmp/tempwd`

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host localhost -security $KVROOT/security/root.login -store $KVSTORE << EOF
execute 'CREATE USER proxy_user IDENTIFIED BY "${TMPPWD}12aB@@"';
EOF

java -jar $KVHOME/lib/kvstore.jar securityconfig wallet create -dir $KVROOT/security/proxy.passwd
java -jar $KVHOME/lib/kvstore.jar securityconfig wallet secret -dir $KVROOT/security/proxy.passwd -set -alias proxy_user -secret "${TMPPWD}12aB@@"

cp $KVROOT/security/client.security  $KVROOT/security/proxy.login
echo "oracle.kv.auth.username=proxy_user" >> $KVROOT/security/proxy.login
#echo "oracle.kv.auth.pwdfile.file=proxy.passwd" >> $KVROOT/security/proxy.login
echo "oracle.kv.auth.wallet.dir=proxy.passwd" >> $KVROOT/security/proxy.login

cd $KVROOT/security; zip -r proxy.zip proxy.* client.trust ; cd -;

java -Xmx64m -Xms64m -jar $KVHOME/lib/sql.jar  -helper-hosts localhost:5000 -security $KVROOT/security/root.login -store $KVSTORE << EOF
CREATE USER application_user IDENTIFIED BY "${KV_APPLICATION_USER_PWD-DriverPass@@123}";
GRANT DBADMIN  TO USER application_user;
EOF
