# Create Oracle NoSQL Database tables and Load data
## Introduction

This lab walks you through the steps to create and load NoSQL tables. You have different approaches/ways to create and load tables in Oracle NoSQL Database. In this lab you will learn two different ways to do it, one from a Node.js application using cloud shell and second using Command Line Interface (CLI). Oracle NoSQL Database supports both schema-based and schema-less (JSON) modeling  and you will create examples of both types of tables.

Estimated Time: 15 minutes

### Objectives

* Create 2 tables in Oracle NoSQL database
* Load the tables with sample data

## Different data models used in Oracle NoSQL Database
The goal of this task is to understand the difference between the 2 data models used. The `demoKeyVal` table is a schema-less table, sometimes referred to as a **JSON document** that contains a primary key and a JSON column.  The `demo` table contains the primary key, several fixed columns and a JSON column, sometimes referred to as a **fixed-schema**. These tables are logically equivalent.

Which data model you use depends on your business model.   Oracle NoSQL Database Cloud Service is extremely flexible in how you can model your data.   It is a true multi-model database service.

  ![](./images/capturemultimodel.png)

## Option 1: Create an Oracle NoSQL Table from CLI:

1. Execute the following in your Shell.
    ```
    <copy>
    KV_VERSION=25.1.13
    KVHOME=$HOME/kv-$KV_VERSION
    cd ~/demo-lab-nosql-kvlite
    </copy>
    ```

    ````
    <copy>
    java -jar $KVHOME/lib/sql.jar -helper-hosts localhost:5000 -store kvstore
    </copy>
    ````
2. You create NoSQL tables using the CLI command.  You will create two different tables.  The table `demo`is a fixed schema table. It has static fields and a JSON column. The table `demoKeyVal` is a JSON document table. It has  akey and a value which is a JSON document.

    ```
    <copy>
    CREATE TABLE if not exists demo(
      fullName     STRING,
      contactPhone STRING,
      ticketNo     STRING,
      confNo       STRING,
      gender       STRING,
      bagInfo      JSON,
     PRIMARY KEY ( ticketNo )
     );
     </copy>
    ```
    ```
    <copy>
    CREATE TABLE IF NOT EXISTS demoKeyVal (
      key INTEGER GENERATED ALWAYS AS IDENTITY
      (START WITH 1
       INCREMENT BY 1
       NO CYCLE
	   ),
     value JSON,
     PRIMARY KEY (key));
    </copy>
    ```
  3. View the description of both the tables created.

     ```
     <copy>
     show tables
     </copy>
     ```
     ```
     <copy>
     describe table demo;
     </copy>
     </copy>
     ```
     ```
     <copy>
     describe table demoKeyVal;
     </copy>
     ```

4. Load data into the `demo` table using the data file `baggage_data_file0.json`.

    ```
    <copy>
    put -table  demo -file objects/baggage_data_file0.json
    </copy>
    ```
    This successfully loads one row into the `demo` table.
    Load another row into the `demo` table using the data file `baggage_data_file0-value.json`.
    ```
    <copy>
    put -table  demo -file objects/baggage_data_file0-value.json
    </copy>
    ```
    When you execute the above command, you get the following error.
    ```
    Error handling command put -table demo -file objects/baggage_data_file0-value.json: Failed to import JSON row at line 57 of file, objects/baggage_data_file0-value.json: Primary key is empty
    ```
    This is because the structure of the data files `baggage_data_file0-value.json` and `baggage_data_file0.json` are very different. The data file `baggage_data_file0-value.json` has the JSON document inside the **value** field. But the demo table expects the ticketNo which is the primary key as a separate field. Thats why you get the error that the primary key is empty.
5. Load data into the `demoKeyVal` table using the data file `baggage_data_file0.json`.

    ```
    <copy>
     put -table  demoKeyVal -file objects/baggage_data_file0.json
    </copy>
    ```
    You get a success message saying one row is inserted. If you view the data that got inserted , you will see that there was a NULL inserted into the JSON document.
    ```
    Loaded 1 row to table demoKeyVal
    <copy>
    select * from demoKeyVal;
    </copy>
    {"key":1,"value":null}
    ```
    The reason is that the data file `baggage_data_file0.json` has the static field and then a JSON column but the `demoKeyVal` table expects all information inside a "Value" field. Since this is a JSON document, the put statement does not fail , rather it succeeds and inserts a NULL JSON.
    Now load another row into the `demoKeyVal` table using the data file `baggage_data_file0-value.json`.
    ```
    <copy>
     put -table  demoKeyVal -file objects/baggage_data_file0-value.json
    </copy>
    ```
   This is successful and the JSON document gets inserted as there is a perfect match between the format in the datafile and what the `demoKeyVal` table expects.

6. View the data that you populated in both the tables. Fetch the data from `demoKeyVal` table when it is NULL and NOT NULL. This explains that both NULL and NOT NULL JSON documents gor inserted into the `demoKeyVal` table.

    ```
    <copy>
    mode json -pretty;
    SELECT * FROM demo;
    </copy>
    ```
    ```
    <copy>
    SELECT * FROM demoKeyVal;
    </copy>
    ```
    ```
    <copy>
    SELECT * FROM demoKeyVal WHERE value IS NULL;
    </copy>
    ```
    ```
    <copy>
    SELECT * FROM demoKeyVal WHERE value IS NOT NULL;  
    </copy>
    ```
## Option 2: Create an Oracle NoSQL Table from your Node.js application:
1. Install the Node.js application.  Execute in the your Shell.

    ```
    <copy>
    cd ~/demo-lab-nosql-kvlite/express-nosql
    npm install
    node express-oracle-nosql.js &
    </copy>
    ```
    **Note:** This will start the "express-oracle-nosql" application in the background.

2. After you complete step 1, you will see a message in the shell saying 'Application running'

    ![](./images/appl-running.png)

    Hit the **'Enter' key** on your keypad to get the command line prompt back.   

3. Insert data into the demo table.   

  This will be done using a curl command to transfer data over the network to the NoSQL backend using the "express-oracle-nosql" application.  Execute in your Shell.

    ```
    <copy>
    cd ~/demo-lab-nosql-kvlite
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file99.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demo
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file9.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demo
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file103.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demo
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file2.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demo
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file84.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demo
    </copy>
    ```
4.  Insert data into the demoKeyVal table.  Execute in your Shell.

    ````
    <copy>
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file99.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demoKeyVal
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file9.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demoKeyVal
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file103.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demoKeyVal
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file2.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demoKeyVal
    FILE_NAME=`ls -1 ~/BaggageData/baggage_data_file84.json`
    curl -X POST -H "Content-Type: application/json" -d @$FILE_NAME http://localhost:3000/demoKeyVal
    </copy>
    ````
5.  Read back the data that you just entered.  Execute in your Shell.

    ````
    <copy>
    curl -X GET http://localhost:3000/demo  | jq
    #curl -X GET http://localhost:3000/demoKeyVal  | jq
    </copy>
    ````
In this lab you have successfully created Oracle NoSQL Database tables and populated them with data. You have also fetched the data that you populated.

You may now **proceed to the next lab.**


## Acknowledgements
* **Author** - Dario Vega, Product Manager, NoSQL Product Management and Michael Brey, Director, NoSQL Product Development
* **Last Updated By/Date** - Dario Vega, Product Manager, NoSQL Product Management, June 2025
