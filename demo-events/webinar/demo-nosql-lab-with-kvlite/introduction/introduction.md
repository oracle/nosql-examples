# Introduction

## About this Workshop

Modern applications benefit from predictable low latency, flexibility, and horizontal scale-out of NoSQL databases. Join us to learn how effortless it is to develop a modern application using Oracle Cloud Infrastructure and Oracle NoSQL Database Cloud Service. This lab is based on data from an airline baggage tracking application. This lab walks you through the steps to create tables in Oracle NoSQL Database Cloud Service (NDCS), load data into the database, and perform basic queries. In addition, it lets you use an application that was developed by the Oracle NoSQL team which has information found in an airline baggage tracking application.

_Estimated Time:_ 30 Minutes

<if type="odbw">If you would like to watch us do the workshop, click [here](https://youtu.be/MmJHAg1F0AQ).</if>

### About NoSQL database
Modern application developers have many choices when faced with deciding when and how to persist a piece of data. In recent years, NoSQL databases have become increasingly popular and are now seen as one of the necessary tools every application developer must have at their disposal. While 'tried and true' relational databases are great at solving classic application problems like data normalization, strict consistency, and arbitrarily complex queries to access that data, NoSQL databases take a different approach.

Many of the recent applications have been designed to personalize the user experience to the individual, ingest huge volumes of machine generated data, deliver blazingly fast, crisp user interface experiences, and deliver these experiences to large populations of concurrent users. In addition, these applications must always be operational, with zero down-time, and with zero tolerance for failure. The approach taken by Oracle NoSQL Database is to offer extreme availability and exceptionally predictable, single digit millisecond response times to simple queries at scale. The Oracle NoSQL Database Cloud Service is designed from the ground up for high availability, predictably fast responses, resiliency to failure, all while operating at extreme scale. Largely, this is due to Oracle NoSQL Database’s shared nothing, replicated, horizontal scale-out architecture. Also, by using the Oracle NoSQL Database Cloud Service, Oracle manages the scale out, monitoring, tuning, and hardware/software maintenance.

Once you are authenticated with your Oracle Cloud account, you can create a NoSQL table, and specify the throughput and storage requirements for the table. Oracle reserves and manages the resources to meet your requirements, and provisions capacity for you. Capacity is specified using read and write units for throughput and GB for storage units.

As a developer, you can connect to NDCS and work with NoSQL tables using the NoSQL SDKs available in many languages.


### Objectives

In this workshop you will:
  * Work with a live demo application
  * Set up your environment
  * Create a table with provisioned reads/sec, writes/sec, and GB storage and write data to the table and read data from the table
  * Run a sample NoSQL application
  * Execute queries against NoSQL tables
  * Write SQL queries to solve business problems




### Prerequisites

This workshop assumes you have:
  * An Oracle Free Tier, Paid Account or Green Button
  * Access to a personal cellphone
  * Programming knowledge in java, python or node.js


## Learn More

* [Oracle NoSQL Database Cloud Service page](https://www.oracle.com/database/nosql-cloud.html)
* [About Cloud Shell](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cloudshellintro.htm)

## Acknowledgements
* **Author** - Dario Vega, Product Manager, NoSQL Product Management and Michael Brey, Director, NoSQL Product Development
* **Last Updated By/Date** - Michael Brey, Director, NoSQL Product Development, September 2021

*Note: If you have a **Free Trial** account, when your Free Trial expires your account will be converted to an **Always Free** account. You will not be able to conduct Free Tier workshops unless the Always Free environment is available. **[Free Tier FAQ](https://www.oracle.com/cloud/free/faq.html)***
