
# Prepare Your Environment

## Introduction

This lab walks you through the steps necessary to prepare your NoSQL environment.

Estimated Time: 2 minutes

### Objectives

In this lab you will:
* Download Oracle NoSQL CE
* Start KVLite and Oracle NoSQL Database Proxy
* Download the scripts and data needed for the lab

### Prerequisites

This lab assumes you have:

* JDK Installed
* unzip


## Task 1: Download Oracle NoSQL CE

In this lab you will be using the 24.3.9 version of the Community Edition of Oracle NoSQL Database. You download the file and unzip and extract the contents.
````
<copy>
KV_VERSION=24.3.9
rm -rf kv-$KV_VERSION
DOWNLOAD_ROOT=http://download.oracle.com/otn-pub/otn_software/nosql-database
DOWNLOAD_FILE="kv-ce-${KV_VERSION}.zip"
DOWNLOAD_LINK="${DOWNLOAD_ROOT}/${DOWNLOAD_FILE}"
curl -OLs $DOWNLOAD_LINK
jar tf $DOWNLOAD_FILE | grep "kv-$KV_VERSION/lib" > extract.libs
jar xf $DOWNLOAD_FILE @extract.libs
rm -f $DOWNLOAD_FILE extract.libs
KVHOME=$PWD/kv-$KV_VERSION
</copy>
````
## Task 2: Start KVLite and Oracle NoSQL Database Proxy
KVLite is a simplified version of the Oracle NoSQL Database. It provides a single storage node, single shard store, that is not replicated.
````
<copy>
java -jar $KVHOME/lib/kvstore.jar kvlite -secure-config disable -root $KVHOME/kvroot -host "$HOSTNAME"  &
while java -jar $KVHOME/lib/kvstore.jar ping -host $HOSTNAME -port 5000  >/dev/null 2>&1 ; [ $? -ne 0 ];do
    echo "Waiting for kvstore to start..."
    sleep 1
done
</copy>
````
The Oracle NoSQL Database Proxy is a middle-tier component that lets the Oracle NoSQL Database SDK communicate with the Oracle NoSQL Database(kvlite configuration).

````
<copy>
java -jar $KVHOME/lib/httpproxy.jar -helperHosts "$HOSTNAME:5000" -storeName kvstore -hostname localhost -httpPort 8080 -verbose true &
</copy>
````
## Task 3: Download the scripts and data needed for the lab
Download the file [demo-lab-nosql-kvlite-main.zip](./files/demo-lab-nosql-kvlite-main.zip). Unzip the files in your home directory. Invoke the `data.sh` script as shown below.
````
<copy>
cd ~
unzip demo-lab-nosql-kvlite-main.zip
mv demo-lab-nosql-kvlite-main demo-lab-nosql-kvlite
sh ~/demo-lab-nosql-kvlite/data.sh
</copy>
````

In this lab you have successfully installed the 20.3.19 version of the Community edition of Oracle NoSQL Database. You have also downloaded the scripts and data needed for the subsequent labs.

You may now **proceed to the next lab.**

## Learn More

* [Oracle NoSQL](https://www.oracle.com/database/nosql/)
* [About NoSQL Documentation](https://docs.oracle.com/en/database/other-databases/nosql-database/index.html)


## Acknowledgements
* **Author** - Dario Vega, Product Manager, NoSQL Product Management and Michael Brey, Director, NoSQL Product Development
* **Last Updated By/Date** - Vandana Rajamani, Principal UA Developer, Oracle NoSQL Database, May 2022
