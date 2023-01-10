# use External Certificates to secure an Oracle NoSQL  Database Proxy

At a high level, configuring your Oracle NoSQL  Database Proxy to use a certificate external requires these steps:
- Create a private key and create a certificate signing request (CSR)
- Validate the files provided by the CA
- Replace the keystore and truststore files with the certificate provided by the CA

In a multi-host store environment, the basic store configuration with security will configured in the first node (`-store-security configure`). 
Then the security directory and all files contained in it should be copied from the first node to each server that will host a Storage Node, to setup internal cluster authentication.

A certificate authority (CA) is an entity that signs digital certificates. Many websites need to let their customers know that the connection is secure, so they pay an internationally 
trusted CA (eg, VeriSign, DigiCert) to sign a certificate for their domain. Sometimes, acting as your own CA may make more sense than paying a CA like DigiCert. 

In this example, We will work as a third party for the CA authority. So we will add a section at the end showing how to Generate the certificate.


## Create a private key and create a certificate signing request (CSR)

Execute the following commands in the host running the Oracle NoSQL  Database Proxy

```bash

echo "Generating files in $PROXYHOME"
echo "Generating password"
openssl rand -out $PROXYHOME/pwd -base64 14
cp $PROXYHOME/pwd $PROXYHOME/pwdin
cp $PROXYHOME/pwd $PROXYHOME/pwdout

echo "Generating the key"
openssl genrsa -out $PROXYHOME/key.pem 2048 -passin file:$PROXYHOME/pwdin -passout file:$PROXYHOME/pwdout 
openssl pkcs8 -topk8 -inform PEM -outform PEM -in $PROXYHOME/key.pem -out $PROXYHOME/key-pkcs8.pem -passin file:$PROXYHOME/pwdin -passout file:$PROXYHOME/pwdout -v1 PBE-SHA1-3DES

echo "Generating the CSR"

openssl req  -new  \
-key key.pem  -out request.csr -config \
<(echo "[req]"; 
echo distinguished_name=req;
echo req_extensions=req_ext
echo "[req_ext]";
echo subjectAltName=@alt_names
echo "[alt_names]"; 
echo DNS.1=${HOSTNAME}
echo DNS.2=$(hostname --fqdn)
echo DNS.3=localhost
)  \
-subj "/C=FR/ST=NA/L=Paris/O=My Company Example/OU=IT/CN=proxy-nosql" -passin file:$PROXYHOME/pwdin -passout file:$PROXYHOME/pwdout

openssl req -noout -text -in $PROXYHOME/request.csr


````


## Validate the files provided by the CA 

Execute the following commands only in the first node e.g. `node1-nosql`

Generally, when deploying a certificate to a server, the CA authority needs to make the following files available:
1.	`ca-chain.cert.pem`
2.	`server.cert.pem` in our case `mynosqlcluster.cert.pem`

In the documentation, we are talking about intermediate.cert.pem and ca.cert.pem. In fact, the ca-chain.cert.pem is just a concat of those files. By the way, you can have 0 or multiple intermediate certficates.

We prefer to have the files separated.

**Note** : (1) and (2) can be stored in other formats like pkcs7. If it is the case, please open a GitHub issue, We can provide the appropriate instructions for this case.

```bash
openssl x509 -noout -text -in proxy-nosql.cert.pem
if [ -f ca-chain.cert.pem ]; then
  echo "CA is providing the ca-chain.cert.pem"
else
  echo "Using intermediate.cert.pem ca.cert.pem to build ca-chain.cert.pem"
  cat intermediate.cert.pem ca.cert.pem > ca-chain.cert.pem
  chmod 444 ca-chain.cert.pem  
fi
openssl verify -CAfile ca-chain.cert.pem  proxy-nosql.cert.pem

````


```bash
cat proxy-nosql.cert.pem > certificate.pem
cat ca-chain.cert.pem >> certificate.pem
rm -f $PROXYHOME/driver.trust
keytool -import -alias example -keystore $PROXYHOME/driver.trust -file $PROXYHOME/certificate.pem  -storepass `cat $PROXYHOME/pwd`  -noprompt

openssl x509 -noout -text -in  $PROXYHOME/certificate.pem | grep -e CN -e Issuer -e DNS

```


```bash
cp $KVROOT/security/proxy.zip $PROXYHOME
unzip $PROXYHOME/proxy.zip -d $PROXYHOME
```

```
kv_proxy_sec &
```

# BONUS: Generate the certificate - acting as third party for the CA authority

Read this link to configure openssl as act as CA - https://jamielinux.com/docs/openssl-certificate-authority/index.html

**Sign server and client certificates**

We will be signing certificates using our intermediate CA. You can use these signed certificates in a variety of situations, such as to secure connections to a web server or to authenticate clients connecting to a service.

```bash

sudo su - 
cd /root/ca

#https://www.openssl.org/docs/man1.0.2/man1/x509.html
# BUGS: Extensions in certificates are not transferred to certificate requests and vice versa.
cat > server_cert.cnf <<EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = req_ext
prompt = no

[req_distinguished_name]
C   = FR
ST  = NA
L   = Paris
O   = My Company Example
OU  = IT
CN  = proxy-nosql

[req_ext]
subjectAltName = @alt_names

[alt_names]
DNS.1 = node1-nosql
DNS.2 = node1-nosql.sub04291027430.vcnnosqldemos.oraclevcn.com
DNS.3 = localhost

EOF

openssl ca -config intermediate/openssl.cnf \
      -extensions server_cert -days 375 -notext -md sha256 \
      -in /home/opc/proxy/request.csr \
      -out intermediate/certs/proxy-nosql.cert.pem  \
	  -extensions req_ext -extfile server_cert.cnf	  

chmod 444 intermediate/certs/proxy-nosql.cert.pem

openssl x509 -noout -text -in intermediate/certs/proxy-nosql.cert.pem


openssl verify -CAfile intermediate/certs/ca-chain.cert.pem  intermediate/certs/proxy-nosql.cert.pem


cp ./intermediate/certs/proxy-nosql.cert.pem /home/opc/proxy/
cp ./intermediate/certs/intermediate.cert.pem /home/opc/proxy/
cp ./certs/ca.cert.pem /home/opc/proxy/ 
cp ./intermediate/certs/ca-chain.cert.pem /home/opc/proxy/

chown opc:opc /home/opc/proxy/*.pem

```

