rm -rf /tmp/data

# Cleanup all the security related files
rm -rf /tmp/data/sn1/kvroot/security
rm -rf /tmp/data/sn2/kvroot/security
rm -rf /tmp/data/sn3/kvroot/security
rm -rf /tmp/data/sn4/kvroot/security

rm nohup.out

# cleanup wallet

cd /u01
rm -rf clientWallet

