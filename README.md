# Oracle NoSQL Database

![Oracle NoSQL](./demo-livelab/NoSQL-Database.png)

**Oracle NoSQL Database** is designed for today’s most demanding applications that 
require low latency responses, flexible data models, and elastic scaling for dynamic workloads. 
It supports JSON, Table and Key-Value datatypes running on-premise, or as a cloud 
service with on-demand throughput and storage based provisioning.

**Oracle NoSQL Database Cloud Service** is now a fully managed database service running 
on Gen 2 Oracle Cloud Infrastructure hardware.

Oracle NoSQL Database Cloud Service makes it easy for developers to build applications, 
delivering predictable single digit millisecond response times with data replication for high availability. 
The service offers ACID transactions, serverless scaling, comprehensive security, and low pay-per-use pricing 
for both on-demand and provisioned capacity modes, including 100% compatibility with on-premises Oracle NoSQL Database. 

# Oracle Nosql Examples
This repository stores a variety of examples demonstrating how to use the Oracle NoSQL Database. 

| Repo/Folder name  | Description |
| ------------- | ------------- |
| [cluster_setup](./cluster_setup) | Deploying Oracle NoSQL Database on the Oracle Cloud Infrastructure `deploy-nosql` |
| [examples-nosql-cluster-deployment](./examples-nosql-cluster-deployment) | Examples allowing you to learn how to deploy a NoSQL cluster/NoSQL store - the most popular topologies |
| [examples-nosql-java-direct-driver](./examples-nosql-java-direct-driver) | Examples using the `nosql-java-direct-driver` |
| [examples-nosql-java-sdk](./examples-nosql-java-sdk) | Examples using the `nosql-java-sdk` |
| [examples-nosql-node-sdk](./examples-nosql-node-sdk) | Examples using the `nosql-node-sdk` |
| [examples-nosql-spring-sdk](./examples-nosql-spring-sdk) | Examples using the `nosql-spring-sdk` |
| [demo-livelab](./demo-livelab) | you can find the code used in our NoSQL LiveLabs workshops in this directory and more information [here](#oracle-nosql-livelabs) |
| [demo-events](./demo-events) | The NoSQL team is delivering content in Webinars and Events around the world. You can also find the instructions for workshops showcased in NoSQL events and have more information [here](#oracle-nosql-livelabs)|
| [SQL for Oracle NoSQL Database tutorial](https://docs.oracle.com/en/database/other-databases/nosql-database/22.3/nsdev/getting-started-sql-oracle-nosql-database1.html) | Getting started with SQL for Oracle NoSQL Database `sql-for-nosql`|
| [terraform Provider for Oracle NoSQL Database tutorial](https://github.com/oracle/terraform-provider-oci/tree/master/examples/nosql)| This is a Terraform configuration that creates the NoSQL service on Oracle Cloud Infrastructure. `nosql-cloud-devops`|


We also collected in this repository exciting content about How to use the Oracle NoSQL database with other OCI services - more information  [here](#other-examples-and-resources). 
**Source:** `Architecture Center` **Source:** `GitHub`

---

NoSQL Developers 👨‍💻 👩‍💻

**Tags:** `enhancement` `help wanted` `question` `idea` `show-and-tell`

In the development world, practice makes the master. That is why you must find as many ways to practice as possible. Never stop learning. 

We’ve enabled [GitHub Discussions](https://github.com/oracle/nosql-examples/discussions) to provide a way for you to connect with other community members. We hope that you:

    Ask questions.
    Share ideas.
    Engage with other community members.
    Welcome others and are open-minded. Remember that this is a community we build together
# Oracle NoSQL LiveLabs



![NoSQL LiveLabs](./demo-livelab/LiveLabs.png)

**Oracle LiveLabs** gives you access to Oracle's tools and technologies to run 
a wide variety of labs and workshops.

In the development world, practice makes master. That is why you must find as many 
ways to practice as possible. Never stop learning. Follow our LiveLabs

* [Get started with tables in Oracle NoSQL Database Cloud Service](https://apexapps.oracle.com/pls/apex/r/dbpm/livelabs/view-workshop?wid=642) `sql-for-nosql` `nosql-java-sdk` `nosql-node-sdk` `nosql-dotnet-sdk` `nosql-go-sdk` `nosql-python-sdk`
* [Discover serverless apps using Oracle NoSQL Database Cloud Service — beginner level](https://apexapps.oracle.com/pls/apex/r/dbpm/livelabs/view-workshop?wid=879) `sql-for-nosql`  `nosql-node-sdk` 

> Those labs were specialled designed for Application Developers, Architects, Administrators
> and DevOps Engineers.

---

In the directory **demo-livelab** you can find the **code used in our NoSQL LiveLabs workshops**.
* [demo-lab-nosql](./demo-livelab/demo-lab-nosql) contains the code for Discover serverless apps using Oracle NoSQL Database Cloud Service — beginner level `sql-for-nosql`  `nosql-node-sdk` 


## Oracle LiveLabs 

Oracle LiveLabs gives you access to Oracle's tools and technologies to run a wide variety of labs and workshops.

Experience Oracle's best technology, live!

[https://developer.oracle.com/livelabs](https://developer.oracle.com/livelabs)

# Oracle NoSQL Workshops and Examples

![NoSQL Ideas](./demo-livelab/Ideas.png)

##  Oracle NoSQL Workshops

The NoSQL team is delivering content in Webinars and Events around the world.

**You can find the code of workshops showcased in NoSQL events** around the World (*LiveLabs coming soon*)
* [serverless-with-nosql-database](./demo-livelab/serverless-with-nosql-database) contains the code for Discover serverless apps using 
Oracle NoSQL Database Cloud Service — intermediate level. Intermediate level LiveLab will feature functions `sql-for-nosql`  `nosql-node-sdk` 
`nosql-python-sdk` `oci-functions` `nosql-cloud-devops`
* [demo-lab-baggage](./demo-livelab/demo-lab-baggage) contains the code for Discover serverless apps using Oracle NoSQL Database Cloud Service — expert level. 
Advanced level Livelab will feature Streaming and API gateway 
`sql-for-nosql`  `nosql-node-sdk` `nosql-python-sdk` `oci-functions` `nosql-cloud-devops` `oci-streaming` `oci-api-gateway` `oci-connector-hub`


**You can find also the instructions of workshops showcased in NoSQL events**

- [Discover Serverless Apps Using Oracle NoSQL Database Cloud Service - intermediate level](http://oracle.github.io/nosql-examples/demo-events/oug/serverless-with-nosql-database-and-functions/workshops/freetier/index.html) *demo-events > oug > serverless-with-nosql-database-and-functions*
`preview version` `sql-for-nosql` `nosql-node-sdk` `nosql-python-sdk` `oci-functions` `nosql-cloud-devops` 
- [Discover Oracle NoSQL Database](http://oracle.github.io/nosql-examples/demo-events/webinar/demo-nosql-lab-with-kvlite/workshops/livelabs/index.html) *demo-events > webinar > demo-nosql-lab-with-kvlite* `preview version` `sql-for-nosql` `nosql-node-sdk` 
- [Writing and optimizing NoSQL queries](http://oracle.github.io/nosql-examples/demo-events/webinar/tv-streaming-service-queries/workshops/livelabs/index.html) *demo-events > webinar > demo-nosql-lab-with-kvlite* `preview version` `sql-for-nosql` 

## Deploying Oracle NoSQL Database on the Oracle Cloud Infrastructure

**Tags:** `deploy-nosql` 

See this [script](./cluster_setup) that simplifies the installation of Oracle NoSQL Database on the Oracle Cloud Infrastructure (OCI). 
This script lets a user set up a small cluster (1-10 machines) quickly, for use in proof-of-concepts, small on premise installations, 
and cluster installations in cloud environments (OCI, AWS, Azure). It's easy to BYOL to the cloud environment of your choosing.

Read this [whitepaper](https://www.oracle.com/a/otn/docs/database/oracle-nosql-cluster-setup-oci.pdf) which explains how to easily install Oracle NoSQL Database on the Oracle Cloud Infrastructure (OCI).

## Getting started with SQL for Oracle NoSQL Database 

**Tags:** `sql-for-nosql` 

Welcome to [SQL for Oracle NoSQL Database tutorial](https://docs.oracle.com/en/database/other-databases/nosql-database/22.3/nsdev/getting-started-sql-oracle-nosql-database1.html). 


The SQL for Oracle NoSQL Database data model supports flat relational data, hierarchical typed (schema-full) data, and schema-less JSON data. 
SQL for Oracle NoSQL Database is designed to handle all such data seamlessly without any impedance mismatch among the different sub-models. 
Impedance mismatch is the problem that occurs due to differences between the database model and the programming language model.

You have two different schemas ( with real-time scenarios) for learning various SQL concepts. 
These two schemas will include various data types that can be used in the Oracle NoSQL database.

**Schema 1: BaggageInfo schema**
Using this schema you can handle a use case wherein passengers traveling on a flight can track the progress of their checked-in bags or 
luggage along the route to the final destination. This functionality can be made available as part of the airline's mobile application. 
Once the passenger logs into the mobile application, the ticket number or reservation code of the current flight is displayed on the screen. 
Passengers can use this information to search for their baggage information. The mobile application is using NoSQL Database to store all the data 
related to the baggage. In the backend, the mobile application logic performs SQL queries to retrieve the required data.

**Schema 2: Streaming Media Service - Persistent User Profile Store**
Consider a TV streaming application. It streams various shows that are watched by customers across the globe. 
Every show has a number of seasons and every season has multiple episodes. You need a persistent meta-data store that keeps track of the 
current activity of the customers using the TV streaming application. Using this schema you can provide useful information to the customer 
such as episodes they watched, the watch time per episode, the total number of seasons of the show they watched, etc. 
The data is stored in the NoSQL Database and the application performs SQL queries to retrieve the required data and make it available to the user.

The scripts allowing to run this tutorial are hosted in this Repository ( `AcctStreamSchema` and `BaggageSchema` ) but follow the instructions provided
in the [SQL for Oracle NoSQL Database tutorial](https://docs.oracle.com/en/database/other-databases/nosql-database/22.3/nsdev/getting-started-sql-oracle-nosql-database1.html)

## Terraform Provider for Oracle Cloud Infrastructure

**Tags:** `nosql-cloud-devops` 

The Oracle Cloud Infrastructure (OCI) provider allows you to use Terraform to interact with Oracle Cloud Infrastructure resources. 
Wherever you use a Terraform distribution you can use the OCI Terraform provider, including Terraform Cloud and the OCI Resource Manager.

[https://github.com/oracle/terraform-provider-oci](https://github.com/oracle/terraform-provider-oci)

This is a Terraform configuration that creates the NoSQL service on Oracle Cloud Infrastructure.

[https://github.com/oracle/terraform-provider-oci/tree/master/examples/nosql](https://github.com/oracle/terraform-provider-oci/tree/master/examples/nosql)

# Cloud Learning

NoSQL Developers 👨‍💻 👩‍💻

**Tags:** `nosql-java-sdk` `nosql-node-sdk` `nosql-dotnet-sdk` `nosql-go-sdk` `nosql-python-sdk`  `nosql-spring-sdk` `nosql-cloud-devops` `oci-functions` `oci-oke` and `more`

Learn how to Develop Applications Fast and Effortlessly using our resources and videos in the **Cloud Learning** page

[https://www.oracle.com/database/nosql/#rc30-cloud-learning](https://www.oracle.com/database/nosql/#rc30-cloud-learning)


# Other Examples and Resources

![NoSQL Ideas](./demo-livelab/Ideas.png)

## Process media by using serverless job management and ephemeral compute workers

**Source:** `Architecture Center`

**Tags:** `nosql-python-sdk` `oci-functions` 

Processing large media files can be a resource intensive operation requiring large compute shapes for timely and efficient processing. 
In scenarios where media processing requests might be ad-hoc and on-demand, leaving instances idle while waiting for new work is not cost effective.

By utilizing Oracle Cloud Infrastructure's (OCI) server-less capabilities, including OCI Functions and OCI NoSQL, 
we can quickly create a management system for processing media content using ephemeral OCI Compute workers.

[https://docs.oracle.com/en/solutions/process-media-using-oci-services/index.html](https://docs.oracle.com/en/solutions/process-media-using-oci-services/index.html)

Note: You can deploy this pattern using downloadable code or automated provisioning, as described in the Download or Deploy section in the link above.

## oci wearable health app

**Source:** `GitHub`

**Tags:** `nosql` `oci` 


The sample application is an IoT use-case, in which an application capturing health parameters running on a wearable device is sending health statistics to a device gateway (backend) 
hosted on OCI on regular intervals. The complete use-case can be split into 3 different parts:
- On-boarding and Administration
- Health data capturing & Real time Analytics
- Batch Analytics

[https://github.com/oracle-devrel/oci-wearable-health-app](https://github.com/oracle-devrel/oci-wearable-health-app)

## Oracle Cloud Infrastructure Data Flow Samples

**Source:** `GitHub`

**Tags:** `nosql-python-sdk` `oci-data flow` 


Oracle Cloud Infrastructure (OCI) Data Flow is a cloud-based serverless platform with a rich user interface. It allows Spark developers 
and data scientists to create, edit, and run Spark jobs at any scale without the need for clusters, an operations team, or highly specialized 
Spark knowledge. Being serverless means there is no infrastructure for you to deploy or manage. It is entirely driven by REST APIs, 
giving you easy integration with applications or workflows

Oracle NoSQL Database cloud service : This application shows how to interface with Oracle NoSQL Database cloud service.

[https://github.com/oracle-samples/oracle-dataflow-samples/](https://github.com/oracle-samples/oracle-dataflow-samples/)

## Train and deploy models from massive data sets: fraud detection use case

**Source:** `Architecture Center`

As your business goes through digital transformation and increasingly accepts online payment, effective methods to detect 
and eventually prevent credit card fraud are necessary to avoid losses. Since fraud is expected to account for a small 
fraction of all transactions, massive amounts of data are typically needed to build a robust and accurate model capable 
of alerting of fraud with minimal false positives.

[https://docs.oracle.com/en/solutions/models-credit-card-fraud-detection/index.html](https://docs.oracle.com/en/solutions/models-credit-card-fraud-detection/index.html)

## Employ anomaly detection for managing assets and predictive maintenance

**Source:** `Architecture Center`

Anomaly detection is the identification of rare items, events, or observations in data that greatly differ from expectations. This has uses in many industries for asset monitoring and maintenance.

Anomaly Detection Service helps you detect anomalies in time series data without the need for statisticians or machine learning experts. 
It provides prebuilt algorithms, and it addresses data issues automatically. It is a cloud-native service accessible over REST APIs and 
can connect to many data sources. The OCI Console, CLI, and SDK make it easy for use in end-to-end solutions.

[https://docs.oracle.com/en/solutions/anomaly-detection/index.html](https://docs.oracle.com/en/solutions/anomaly-detection/index.html)

##  Oracle Architecture Center

**Reference architectures and best practices**

Leverage knowledge from Oracle experts. Use our reference architectures, solution playbooks, and customer stories to build and deploy your cloud, 
hybrid, and on-premises workloads.

[Explore Oracle Architecture Center](https://docs.oracle.com/pls/topic/lookup?ctx=en/solutions&id=solutions-home)

