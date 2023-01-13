#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

echo "Generating files in $PROXYHOME"
echo "Generating password"
openssl rand -out $PROXYHOME/pwd -base64 14
cp $PROXYHOME/pwd $PROXYHOME/pwdin
cp $PROXYHOME/pwd $PROXYHOME/pwdout

echo "Generating certificate"
openssl req -x509 -days 365 -newkey rsa:4096 -keyout $PROXYHOME/key.pem -out $PROXYHOME/certificate.pem -subj "/CN=${HOSTNAME}" -passin file:$PROXYHOME/pwdin -passout file:$PROXYHOME/pwdout \
-extensions san -config \
 <(echo "[req]"; 
    echo distinguished_name=req; 
    echo "[san]"; 
    echo subjectAltName=DNS:${HOSTNAME},DNS:localhost,DNS:$(hostname --fqdn)
 ) \
-subj "/C=US/ST=CA/L=San/CN=proxy-nosql"

openssl x509 -noout -text -in  $PROXYHOME/certificate.pem | grep -e CN -e Issuer -e DNS

cat $PROXYHOME/pwdout
openssl pkcs8 -topk8 -inform PEM -outform PEM -in $PROXYHOME/key.pem -out $PROXYHOME/key-pkcs8.pem -passin file:$PROXYHOME/pwdin -passout file:$PROXYHOME/pwdout -v1 PBE-SHA1-3DES 
rm -f $PROXYHOME/driver.trust
keytool -import -alias example -keystore $PROXYHOME/driver.trust -file $PROXYHOME/certificate.pem  -storepass `cat $PROXYHOME/pwd`  -noprompt
#keytool -import -alias example -keystore $PROXYHOME/driver.trust -file $PROXYHOME/certificate.pem  -storepass:file $PROXYHOME/pwd -noprompt

echo "Generated files"
rm $PROXYHOME/pwdin $PROXYHOME/pwdout
ls -lrt $PROXYHOME
