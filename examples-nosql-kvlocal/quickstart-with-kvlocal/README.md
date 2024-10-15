# quickstart-with-kvlocal

## Introduction

KVLocal is an embedded Oracle NoSQL Database that can be embedded in data-rich applications to process and present live data from large datasets.
KVLocal provides a single-node store that is not replicated.

It runs as a separate child process in the application JVM and requires minimal administration. KVLocal is a robust database and handles failures efficiently.
You can start and stop KVLocal using APIs.

KVLocal runs in a single instance of Oracle NoSQL Database by including `kvstore.jar` in the application's classpath, with APIs to start a JVM
and initialize the database. KVLocal is accessed using the Java Direct Driver API.

KVLocal uses either TCP/IP sockets or Unix domain sockets for communication between the client APIs and KVLocal.
If you configure KVLocal to use TCP/IP sockets, it runs by default in secure mode, however you can configure it explicitly to run non-securely.
If you configure KVLocal to use Unix domain sockets, it is inherently secure because it is not accessible over the network.
The security depends on file protections on the socket files used for communication.


## Preparation

Clone this repository

Download and unzip the binaries including `kvstore.jar`. You should download CE or EE;.

Download the [Oracle NoSQL Database bundle](https://www.oracle.com/database/technologies/nosql-database-server-downloads.html)
- Community Edition: Oracle NoSQL Database Community Edition (CE) software is licensed pursuant to the Apache 2.0 License (Apache 2.0).
- Enterprise Edition: Oracle NoSQL Database Enterprise Edition (EE) software is licensed pursuant to the Oracle commercial license

In this demo, we will use the Oracle NoSQL Database bundle - Enterprise Edition (e.g 22.3.32)

For more information about difference between versions (CE vs EE) and other topics, visit the [FAQ](https://www.oracle.com/database/technologies/nosqldb-learnmore-nosqldb-faq.html)

```bash
unzip kv-ee-24.3.9.zip -d nosql
````

## Compile and Run the application

Compile the App code:
```shell
javac -cp .:$HOME/nosql/kv-24.3.9/lib/kvstore.jar Quickstart.java 
```

Running the App code:
```shell
java -cp .:$HOME/nosql/kv-22.3.32/lib/kvstore.jar Quickstart
```
