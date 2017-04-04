# Run the admin with the security file containing property settings for the login

java -jar $KVHOME/lib/kvstore.jar runadmin -port 5000 -host kvhost01 -store kvstore -security /tmp/data/sn1/kvroot/security/client.security

