# OUG : Oracle User Groups
Instruction for labs showcased to OUG

## About these Workshops

Modern applications benefit from predictable low latency, flexibility, and horizontal scale-out of NoSQL databases. Join us to learn how effortless it is to develop a 
modern application using Oracle Cloud Infrastructure and Oracle NoSQL Database Cloud Service. This lab is based on data from an airline baggage tracking application. 
This lab walks you through the steps to create tables in Oracle NoSQL Database Cloud Service (NDCS), load data into the database, and perform basic queries.  
In addition, it lets you use an application that was developed by the Oracle NoSQL team which contains information found in an airline baggage tracking application.

Estimated Time: 50 Minutes

## serverless-with-nosql-database 

### Objectives

In this workshop you will:
  * Deploy an application using Resource Manager - Terraform script
  * Create a table with provisioned reads/sec, writes/sec, and GB storage and write data to the table and read data from the table
  * Run the Sample NoSQL Application and review the code created using Functions (Node.js and Python)
  * Execute queries against NoSQL tables using a Python application - cli command

### Lab

* [setup-environment](./serverless-with-nosql-database/setup-environment/setup-environment.md) - 10 min
* [create-populate-tables](./serverless-with-nosql-database/create-populate-tables/create-populate-tables.md)  - 20 min
* [explore-run-queries](./serverless-with-nosql-database/explore-run-queries/explore-run-queries.md) - 20 min

Estimated Time: 50 Minutes

### Additional steps will be provided by the instructor
https://github.com/oracle/nosql-examples/blob/master/demo-livelab/demo-lab-baggage/Instructions.md#Step10.CreatetheStream
* Create the Stream
* Create and configure the Service Connector
* Create and configure the API Gateway
* Execute the API and Streaming tests


## Learn More

* [Oracle NoSQL Database Cloud Service page](https://www.oracle.com/database/nosql-cloud.html)
* [About Oracle NoSQL Database Cloud Service](https://docs.oracle.com/pls/topic/lookup?ctx=cloud&id=CSNSD-GUID-88373C12-018E-4628-B241-2DFCB7B16DE8)
* [About Cloud Shell](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cloudshellintro.htm)
* [About Resource Manager](https://docs.oracle.com/en-us/iaas/Content/ResourceManager/Concepts/resourcemanager.htm)
* [About Functions](https://docs.oracle.com/en-us/iaas/Content/Functions/Concepts/functionsoverview.htm)

