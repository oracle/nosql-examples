# use External Certificates to secure an Oracle NoSQL cluster
Work in Progress 👷

There are 2 ways to configure a NoSQL cluser, one using a **secure** configuration and another one using a **non-secure** configuration. 
The primary difference is in the way access is performed to NoSQL store. We recommend using the secure setup, albeit additional steps are needed during set up. 

It is possible to configure and use External Certificates to secure an Oracle NoSQL cluster. We already provided all steps needed for the default configuration, which uses a self-signed certificate.

Our current configuration only supports one pair of private key and certificate, so you only need one certificate with a common name to be generic or wild card.

> Prepare `dnmatch` expression using a distinguished name. Oracle NoSQL Database verifies identities of server and client while establishing SSL connection between the server components.
> The verification is performed by checking if principal names on each side match the specified dnmatch expressions, which uses regular expressions as specified by java.util.regex.Pattern.
> The principal names represent the identities, which are specified by the subject name attribute of the certificate, represented as a distinguished name in RFC 1779 format, 
> using the exact order, capitalization, and spaces of the attribute value. RFC 1779 defines well-known attributes for distinguished names, including CN, L, ST O, OU, C and STREET. 
> If the distinguished name of the external certificate contains non-standard attributes, for example, EMAILADDRESS, then the expression used for dnmatch must replace these attribute names with an OID that is valid 
> in RFC 1779 form, or use special constructs of regular expression to skip checking these attributes.

As the `dnmatch` description says, our hostname verification is not checking the hostname of remote server but the subject name of the certificate. 
Basically, they don't have to match the host name with the CN in the certificate.

For example, You can have one of the following Subject Name for your unique external certificate:
- `CN=*.example.com, OU=IT, O=My Company Example, L=Paris, ST=NA, C=FR`
- `CN=myNosqlCluster, OU=IT, O=My Company Example, L=Paris, ST=NA, C=FR`
- `CN=myNosqlCluster.example.com,  OU=IT, O=My Company Example, L=Paris, ST=NA, C=FR`


If your initial request coming from the usage of scan vulnerability. We think that the best solution could be the first one, use the domainname - subject CommonName does match Server FQDN.
But we have also customer that prefer to have the other options and add SAN for validate the FQDN of severs hosting NoSQL Cluster.

At a high level, configuring your store to use a certificate external requires these steps:
- Create a private key and create a certificate signing request (CSR)
- Execute the `makebootconfig` tool with a dnmatch expression using a distinguished name to configure the cluster in the first node
- Validate the files provided by the CA
- Replace the keystore and truststore files with the certificate provided by the CA

In a multi-host store environment, the basic store configuration with security will configured in the first node (`-store-security configure`). 
Then the security directory and all files contained in it should be copied from the first node to each server that will host a Storage Node, to setup internal cluster authentication.

A certificate authority (CA) is an entity that signs digital certificates. Many websites need to let their customers know that the connection is secure, so they pay an internationally 
trusted CA (eg, VeriSign, DigiCert) to sign a certificate for their domain. Sometimes, acting as your own CA may make more sense than paying a CA like DigiCert. 

In this example, We will work as a third party for the CA authority. So we will add a section at the end showing how to Generate the certificate.


## Create a private key and create a certificate signing request (CSR)

Execute the following commands only in the first node e.g. `node1-nosql`

```bash

cd $HOME/examples-nosql-cluster-deployment/script
source env.sh

mkdir $HOME/genCA
cd  $HOME/genCA
rm -rf *

echo "Creating password"
openssl rand -out kspwd -base64 14

keytool -genkeypair -keystore store.keys \
-alias shared -keyAlg RSA -keySize 2048 \
-validity 365 -dname  "CN=*.example.com, OU=IT, O=My Company Example, L=Paris, ST=NA, C=FR" \
-storepass `cat kspwd` -storetype pkcs12

keytool -certreq -keystore store.keys -alias shared -file mynosqlcluster.csr -storepass `cat kspwd` -storetype pkcs12

openssl req -noout -text -in mynosqlcluster.csr
````

## Execute the `makebootconfig` tool with a dnmatch expression using a distinguished name to configure the cluster 

Execute the following commands only in the first node e.g. `node1-nosql`

```bash
cd $HOME/examples-nosql-cluster-deployment/script
cp $HOME/genCA/kspwd $KVROOT/kspwd 
bash boot-external-sec.sh configure
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
openssl x509 -noout -text -in mynosqlcluster.cert.pem
if [ -f ca-chain.cert.pem ]; then
  echo "CA is providing the ca-chain.cert.pem"
else
  echo "Using intermediate.cert.pem ca.cert.pem to build ca-chain.cert.pem"
  cat intermediate.cert.pem ca.cert.pem > ca-chain.cert.pem
  chmod 444 ca-chain.cert.pem  
fi
openssl verify -CAfile ca-chain.cert.pem  mynosqlcluster.cert.pem
````

## Replace the keystore and truststore files with the certificate provided by the CA

Execute the following commands only in the first node e.g. `node1-nosql`

Import certificates that are part of a certificate chain in order

```bash
cd  $HOME/genCA
keytool -import -file ca.cert.pem -keystore store.keys -alias root -storepass `cat kspwd` -storetype pkcs12 -noprompt
keytool -import -file intermediate.cert.pem -keystore store.keys -alias intermediate -storepass `cat kspwd` -storetype pkcs12 -noprompt
keytool -import -file mynosqlcluster.cert.pem -keystore store.keys -alias shared -storepass `cat kspwd` -storetype pkcs12 -noprompt
```

Verify the installation by checking the certificate content in store.keys: 

```bash
cd  $HOME/genCA
keytool -list -v -keystore store.keys -alias shared -storepass `cat kspwd` -storetype pkcs12  -noprompt
```

Build server truststore (store.trust)

```bash
cd  $HOME/genCA
keytool -export -file store.tmp -keystore store.keys -alias shared -storetype pkcs12 -storepass `cat kspwd` -noprompt
keytool -import -keystore store.trust -file store.tmp -alias shared -storetype pkcs12 -storepass `cat kspwd` -noprompt
keytool -import -keystore store.trust -file ca.cert.pem -alias root -storetype pkcs12 -storepass `cat kspwd` -noprompt
keytool -import -keystore store.trust -file intermediate.cert.pem -alias intermediate -storetype pkcs12 -storepass `cat kspwd` -noprompt
```

Copy the truststore (store.trust and store.keys)

```bash

cp store.keys $KVROOT/security/ 
cp store.trust $KVROOT/security/ 
```

```bash
keytool -import -keystore $KVROOT/security/client.trust -file ca.cert.pem -alias root  -storetype pkcs12  -storepass `cat kspwd`  -noprompt
keytool -import -keystore $KVROOT/security/client.trust -file intermediate.cert.pem -alias intermediate   -storetype pkcs12   -storepass `cat kspwd`  -noprompt
```

Zip and copy `$KVROOT/security` to other nodes

```bash
cd ; zip -r $HOME/security.zip $KVROOT/security; cd -
copy $HOME/security.zip from `node1-nosql` to other nodes except proxy node		
```

Execute the following commands in all other nodes e.g `node2-nosql` `node3-nosql` 

```bash
cd $HOME/examples-nosql-cluster-deployment/script
bash boot-external-sec.sh enable
```

## Start the storage Nodes

Execute the following commands in all Storage nodes

```bash
cd $HOME/examples-nosql-cluster-deployment/script
bash start.sh
```

Now you are ready to continue configuring your store 
- Deploy YOUR topology
- Create users
- Configure and start Oracle NoSQL Database Proxy

# BONUS: Generate the certificate - acting as third party for the CA authority

Read this link to configure openssl as act as CA - https://jamielinux.com/docs/openssl-certificate-authority/index.html

**Sign server and client certificates**

We will be signing certificates using our intermediate CA. You can use these signed certificates in a variety of situations, such as to secure connections to a web server or to authenticate clients connecting to a service.

```bash

sudo su - 
cd /root/ca
openssl ca -config intermediate/openssl.cnf \
      -extensions server_cert -days 375 -notext -md sha256 \
      -in /home/opc/genCA/mynosqlcluster.csr \
      -out intermediate/certs/mynosqlcluster.cert.pem 
	  
chmod 444 intermediate/certs/mynosqlcluster.cert.pem

openssl x509 -noout -text -in intermediate/certs/mynosqlcluster.cert.pem

openssl verify -CAfile intermediate/certs/ca-chain.cert.pem  intermediate/certs/mynosqlcluster.cert.pem


cp ./intermediate/certs/mynosqlcluster.cert.pem /home/opc/genCA/
cp ./intermediate/certs/intermediate.cert.pem /home/opc/genCA/
cp ./certs/ca.cert.pem /home/opc/genCA/ 
cp ./intermediate/certs/ca-chain.cert.pem /home/opc/genCA/

chown opc:opc /home/opc/genCA/*.pem

```

