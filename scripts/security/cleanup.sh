#!/bin/sh

# java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host bigdatalite.us.oracle.com -security $KVROOT/kvroot1/security/client.security -username root<<EOF

# plan drop-user -name jsmith -wait
# exit

if [ -d "/tmp/data/sn1/kvroot/security" ]; then

java -jar $KVHOME/lib/kvstore.jar securityconfig<<EOF

config remove-security -root /tmp/data/sn1/kvroot
config remove-security -root /tmp/data/sn2/kvroot
config remove-security -root /tmp/data/sn3/kvroot
config remove-security -root /tmp/data/sn4/kvroot
exit
EOF

rm -rf /tmp/data/sn1/kvroot/security
rm -rf /tmp/data/sn2/kvroot/security
rm -rf /tmp/data/sn3/kvroot/security
rm -rf /tmp/data/sn4/kvroot/security

fi


cd /u01
rm -rf clientWallet

