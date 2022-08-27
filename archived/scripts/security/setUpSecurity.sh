#!/bin/sh

#shut down the KVStore instance

./stopDB.sh

#Run the securityconfig utility to set up the basic store configuration with security
#Use the config create command with the -pwdmgr option to specify the mechanism used to hold passwords that is needed for accessing the stores. In this case, Oracle Wallet is used. 
# Here welcome1 is the password for the store.
# This configuration tool will automatically generate the security related files

java -jar $KVHOME/lib/kvstore.jar securityconfig<<EOF
config create -pwdmgr wallet -root /tmp/data/sn1/kvroot
ABcd__1234
ABcd__1234
config add-security -root /tmp/data/sn1/kvroot -secdir security -config config.xml
exit

EOF

#Since this a multi-host store enviornment.The security directory and all the files contained in that should be copied to each server that will host the storage node.
#In a multi-host store environment, the security directory and all files contained in it should be copied from the first node to each server that will host a Storage Node, to setup internal cluster authentication.

./copySecurityFolder.sh
./addSecurityAll.sh

#Once done start the KVStore

./startDB.sh

# Wait for all the process to come up

while [[ `jps -m | grep kvroot | wc -l` < 26 ]] 
do 
usleep 100000000
done

usleep 100000000

# Start runadmin in security mode pointing to the security directory
# Create the admin user, in this case it is root


java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host kvhost01 -security /tmp/data/sn1/kvroot/security/client.security<<EOF
plan create-user -name root -admin -wait
ABcd__1234
ABcd__1234
exit
EOF

#Create a new wallet file to store the credentails needed to allow clients to login

cd /u01
mkdir clientWallet

java -jar $KVHOME/lib/kvstore.jar securityconfig wallet create -dir /u01/clientWallet/store.wallet

#The file mylogin.txt should be a copy of the client.security file with additional properties settings for authentication.This will allow to login without being prompted for password. 

cp /tmp/data/sn1/kvroot/security/client.security /tmp/data/sn1/kvroot/security/myLogin.txt
echo "oracle.kv.auth.username=jsmith" >> /tmp/data/sn1/kvroot/security/myLogin.txt
echo "oracle.kv.auth.wallet.dir=/u01/clientWallet/store.wallet" >> /tmp/data/sn1/kvroot/security/myLogin.txt
echo "oracle.kv.ssl.trustStore=/tmp/data/sn1/kvroot/security/client.trust" >> /tmp/data/sn1/kvroot/security/myLogin.txt
