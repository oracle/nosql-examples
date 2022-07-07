# Explore Data and Run Queries

## Introduction

This lab picks up where lab 3 left off.   We are going to explore in more detail the tables we created, load data with functions, and execute queries a python application.  

Estimated Time: 25 minutes

### Objectives

* Understand the different tables
* Load data with functions
* Read data with REST api
* Read data with a python application

### Prerequisites

* An Oracle Free Tier, Always Free, or Paid Cloud Account
* Connection to the Oracle NoSQL Database Cloud Service
* Working knowledge of bash shell
* Working knowledge of vi, emacs
* Working knowledge of Python


## Task 1: Restart the Cloud Shell

1. Lets get back into the Cloud Shell.  From the previous lab, you may have minimized it in which case you need to enlarge it.  It is possible it may have become disconnected and/or timed out.   In that case, restart it.

![](./images/cloud-shell-phoenix.png)

2. Execute the following environment setup shell script in the Cloud Shell to set up your environment. If you close/open the Cloud Shell Console, please re-execute it.

```

source ~/serverless-with-nosql-database/env.sh

```

## Task 2: Load Data and Examine It

The goal of this task is to understand the difference between the 2 data models used. The demoKeyVal table is a schema-less table, sometimes referred to as a JSON document that contains a primary key and a JSON column.  The demo table contains the primary key, several fixed columns and a JSON column.  Sometimes referred to as a fixed-schema. These tables are logically equivalent. Which data model you use depends on your business model.   Oracle NoSQL Database Cloud Service is extremely flexible in how you can model your data.   It is a true multi-model database service.

![](./images/capturemultimodel.png)

1. We will use functions that we created in Lab 2 to add rows into the table demoKeyVal.  We will load 5 additional rows.  The initial invocation of functions can take 30-45 seconds because components are getting loaded into your environment.  Execute in Cloud Shell.

```

cd ~/serverless-with-nosql-database/functions-fn
cd load/demo-keyval-load
cat ~/BaggageData/baggage_data_file99.json | fn invoke $APP_NAME demo-keyval-load
cat ~/BaggageData/baggage_data_file9.json  | fn invoke $APP_NAME demo-keyval-load
cat ~/BaggageData/baggage_data_file103.json  | fn invoke  $APP_NAME demo-keyval-load
cat ~/BaggageData/baggage_data_file2.json  | fn invoke $APP_NAME demo-keyval-load
cat ~/BaggageData/baggage_data_file84.json  | fn invoke  $APP_NAME demo-keyval-load

```

2. Use the steps in the previous Lab 3 to read the data for the demo-keyval-load table from the OCI console.  

![](./images/capturenosql-query-keyval.png)

3. Next, we will use a function to load the table demo with the same 5 rows.  Execute in Cloud Shell.

```

cd ~/serverless-with-nosql-database/functions-fn
cd load/demo-load
cat ~/BaggageData/baggage_data_file99.json | fn invoke  $APP_NAME demo-load
cat ~/BaggageData/baggage_data_file9.json | fn invoke  $APP_NAME demo-load
cat ~/BaggageData/baggage_data_file103.json  | fn invoke  $APP_NAME demo-load
cat ~/BaggageData/baggage_data_file2.json | fn invoke  $APP_NAME demo-load
cat ~/BaggageData/baggage_data_file84.json  | fn invoke  $APP_NAME demo-load

```

4. Use the steps in the previous Lab 3 to read the data for the demo-load table from the OCI console.

![](./images/capturenosql-query.png)


## Task 3: Read Data Using a Node.js Application

In this Task, we will review the code and trigger the function manually using the `fn invoke` CLI command.

1. Let's look at the function we will be invoking.    By setting up different endpoints, you can cause different operations to happen.   In this node.js function, we have 3 different endpoints setup in advance. Execute in Cloud Shell.

```

cd ~/serverless-with-nosql-database/functions-fn
cd api/demo-api
vi func.js

```

2. Next, we will call `fn invoke` manually, passing it the getBagInfoByTicketNumber endpoint.   Execute in Cloud Shell one at a time so you can see the results.

```

echo '{"ticketNo":"1762386738153", "endPoint":"getBagInfoByTicketNumber"}' | fn invoke $APP_NAME demo-api | jq

```
```

echo '{"endPoint":"getBagInfoByTicketNumber"}' | fn invoke $APP_NAME demo-api | jq

```
```

echo '{"endPoint":"getBagInfoByTicketNumber"}' | fn invoke $APP_NAME demo-api | jq '. | length'

```

  Each of these produced slightly different results.   The first one display the document with a specific ticket number, the second displayed all the records and the third gave a count of the records.

3. Now, lets test another one of the endpoints in the function. Execute in Cloud Shell.

```

echo '{"endPoint":"getPassengersAffectedByFlight"}' | fn invoke $APP_NAME demo-api | jq

```

  As you can see the field "message" the getPassengersAffectedByFlight endpoint is still under construction.  In other words the code for that endpoint has not been completed yet.

4. The result can be simulated by using this call. Execute in Cloud Shell.

```

echo '{"endPoint":"getPassengersAffectedByFlight"}' | fn invoke $APP_NAME demo-api | fn invoke $APP_NAME demo-api | jq

```

5. In fact, you can run SQL queries using the endpoint executeSQL.   This endpoint is coded to use the executeQuery(sql) API call. This will grab a sql query that has already been written and stored in your Cloud Shell.   Execute in Cloud Shell.

````

SQL_STATEMENT=$(cat ~/serverless-with-nosql-database/objects/query1.sql | tr '\n' ' ')
echo "$SQL_STATEMENT"

````
```


echo "{\"sql\":\"$SQL_STATEMENT\",\""endPoint\"": \""executeSQL\"" }"  | fn invoke $APP_NAME demo-api

````
This displayed the entire record for passenger 'Clemencia Frame' where as the query before just displayed some basic information.

6. Let's say you didnt want to use functions.   You can also execute the same sql statement using OCI CLI commands.  Going this route, you will be querying the data over REST.  Execute in Cloud Shell.

````

oci nosql query execute -c  $COMP_ID --statement "$SQL_STATEMENT"

````

  In this case, the data is formatted as a nice JSON document.


## Task 4: Load Data Using Streaming Input

In this task, we are going to load a record using a python function.  This uses the Oracle NoSQL Python SDK which we call Borneo.  We can take a look at the application. At the bottom of the file is the authentication which uses resource principals.

1. Let's look at the file.  Execute in Cloud Shell.

```

cd ~/serverless-with-nosql-database/functions-fn
cd streaming/load-target
vi func.py

```

2. Deploy this function which take about 2 min and 30 sec.  Execute in Cloud Shell.

```

fn -v deploy --app $APP_NAME

```

3. Run this function.  The first time running this function takes about 1 min because it has to populate the cache. Execute in Cloud Shell.

 ```
 
 cd ~/serverless-with-nosql-database/functions-fn
 cd streaming/load-target
 var1=`base64 -w 0 ~/BaggageData/baggage_data_file99.json`
 cp test_templ.json stream_baggage_data_file99.json
 sed -i "s/<here>/$var1/g"  stream_baggage_data_file99.json
 
 ```
 ```
 
 fn invoke $APP_NAME load-target < stream_baggage_data_file99.json
 
 ```
4. Remove the function now that we are done with it.  Execute in Cloud Shell.

```

fn delete function $APP_NAME load-target

```

## Task 5: Read Data Using a Python CLI Application

1. Create the python CLI application in the Cloud shell.  Execute in Cloud Shell.

 ```
 
 cd ~/serverless-with-nosql-database/
 source ~/serverless-with-nosql-database/env.sh
 pip3 install borneo
 pip3 install cmd2

 
 ```
 ```
 
 python3 nosql.py -s cloud -t $OCI_TENANCY -u $NOSQL_USER_ID -f $NOSQL_FINGERPRINT -k ~/NoSQLLabPrivateKey.pem -e https://nosql.${OCI_REGION}.oci.oraclecloud.com
 
 ```
2.  This will create a Pyhton NoSQL shell that you can execute queries in.

![](./images/capturepython.png)

3. Execute the following queries.  Execute in Cloud Shell.

````

SELECT *
FROM demo d
WHERE d.bagInfo.flightLegs.flightNo =ANY 'BM715';

````

````

SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
FROM demo d
WHERE d.bagInfo.flightLegs.flightNo =ANY 'BM715';

````

````

SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
FROM demo d
WHERE d.bagInfo.flightLegs.flightNo =ANY "BM715"
AND d.bagInfo.flightLegs.flightNo =ANY "BM204";

````

````

SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
FROM   demo d
WHERE  d.bagInfo.flightLegs.flightNo =ANY "BM715"
AND    d.bagInfo.flightLegs.flightNo =ANY "BM204"
AND    size(d.bagInfo.flightLegs) = 2;

````
4. Minimize the Cloud Shell by hitting the minimize key.

## Task 6: Clean Up

This task deletes the tables that got created.

1. On the top left, go to menu, then Databases, then under Oracle NoSQL Database, hit 'Tables'
Set your compartment to 'demonosql'
Click on the freeTest table, which will bring up the table details screen.  Hit Delete.

![](./images/delete-freetable.png)

Deleting tables is an async operation, so you will not immediately see the results on the OCI console.  Eventually the status of the tables will get changed to deleted.  

2.  Clean up from the deployment.   In the top left corner, hit the OCI drop down menu, then go to 'Developer Services' and then Stacks under Resource manager.

![](./images/stacks-select.png)

3.  In the Stacks screen, click on the stack with the name main.zip-xxxxxx.

![](./images/main-zip.png)

4.  This will bring you to the stacks detail page.  On that screen hit the 'Destroy' button.  This will then pop up another window where you will have to hit 'Destroy' again.    This process takes 4-5 minutes to run and clean everything up.  

![](./images/destroy-stack.png)


## Learn More


* [Oracle NoSQL Database Cloud Service page](https://www.oracle.com/database/nosql-cloud.html)
* [About Oracle NoSQL Database Cloud Service](https://docs.oracle.com/pls/topic/lookup?ctx=cloud&id=CSNSD-GUID-88373C12-018E-4628-B241-2DFCB7B16DE8)
* [Java API Reference Guide](https://docs.oracle.com/en/cloud/paas/nosql-cloud/csnjv/index.html)
* [Node API Reference Guide](https://oracle.github.io/nosql-node-sdk/)
* [Python API Reference Guide](https://nosql-python-sdk.readthedocs.io/en/latest/index.html)
* [About Functions](https://docs.oracle.com/en-us/iaas/Content/Functions/Concepts/functionsoverview.htm)
* [About Cloud Shell](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cloudshellintro.htm)


## Acknowledgements
* **Author** - Dario Vega, Product Manager, NoSQL Product Management and Michael Brey, Director, NoSQL Product Development
* **Last Updated By/Date** - Michael Brey, Director, NoSQL Product Development, September 2021
