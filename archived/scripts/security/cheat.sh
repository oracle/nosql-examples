#!/bin/sh


./setUpSecurity.sh

#Creating a new user called jsmith

java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host kvhost01 -security /tmp/data/sn1/kvroot/security/client.security<<EOF
root
ABcd__1234
plan create-user -name jsmith -wait
ABcd__1234
ABcd__1234
exit
EOF

#define new user also in the client wallet
java -jar $KVHOME/lib/kvstore.jar securityconfig wallet secret -dir /u01/clientWallet/store.wallet -set -alias jsmith<<EOF
ABcd__1234
ABcd__1234
exit
EOF

#display content of myLogin.txt
cat /tmp/data/sn1/kvroot/security/myLogin.txt

java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host kvhost01 -security /tmp/data/sn1/kvroot/security/myLogin.txt<<EOF
exit
EOF
