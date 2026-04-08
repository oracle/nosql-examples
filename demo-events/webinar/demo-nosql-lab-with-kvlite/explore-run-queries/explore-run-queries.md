# Explore Data and Run Queries

## Introduction

This lab picks up where Lab 2 left off.   You are going to explore in more detail the tables you created, and execute queries using SQL cli Shell and a Node.js application.  

Estimated Time: 15 minutes

### Objectives

* Read data from NoSQL tables using REST API
* Read data from NoSQL tables from a python application

## Task 1: Run queries and read data using REST API

1.  Fetch back the data that you entered earlier.  Execute in your Shell.  In the two queries, you use a limit clause which limits the number of rows returned.  You also use an order by clause to sort the results.

    ````
    <copy>
    curl  "http://localhost:3000/demo?limit=3&orderby=ticketNo"  | jq
    </copy>
    ````

    ````
    <copy>
    curl  "http://localhost:3000/demo?limit=12&orderby=fullName"  | jq
    </copy>
    ````
2. Read Data for a specific ticket number using GET command.  Execute in your Shell.

    ````
    <copy>
    curl -X GET http://localhost:3000/demo/1762322446040  | jq
    </copy>
    ````
3. In the baggage tracking demo from Lab 1, which is running live in all the regions, a Node.js application was running on the background.   You can install that application, and run it on your data.  It uses a different port number than the previous application you installed.  It also runs in the background, so **hit 'Enter'** to get the prompt back.  Execute in your Shell.

    ````
    <copy>
    cd ~/demo-lab-nosql-kvlite/express-nosql
    npm install
    node express-baggage-demo-nosql.js &
    </copy>
    ````

4. You can run a query by ticket number and passengers on a flight.  Execute in your Shell.   Each of these produced slightly different results.

  * Displays the document with a specific ticket number

    ````
    <copy>
    curl -X GET http://localhost:3500/getBagInfoByTicketNumber?ticketNo=1762322446040  | jq
    </copy>
    ````
  * Displays all the records

    ````
    <copy>
    curl -X GET http://localhost:3500/getBagInfoByTicketNumber  | jq
    </copy>
    ````
  *  Displays a count of the records.

    ````
    <copy>
    curl -X GET http://localhost:3500/getBagInfoByTicketNumber | jq '. | length'
    </copy>
    ````
  * For the below command, you can see in the field "message" the getPassengersAffectedByFlight endpoint is still under construction. In other words the code for that endpoint has not been completed yet.

    ````
    <copy>
    curl -X GET http://localhost:3500/getPassengersAffectedByFlight?flightNo=BM715  | jq
    </copy>
    ````

## Task 2: Read Data Using CLI commands

1. Execute the following in your Shell. You are invoking the sql command line to execute your queries.

    ````
    <copy>
    KV_VERSION=25.1.13
    KVHOME=$HOME/kv-$KV_VERSION
    cd ~/demo-lab-nosql-kvlite/
    java -jar $KVHOME/lib/sql.jar -helper-hosts localhost:5000 -store kvstore
    </copy>
    ````

2. Load additional data so you can run some queries.  Execute in your Shell.

    ````
    <copy>
    import -table demo -file ../BaggageData/load_multi_line.json
    </copy>
    ````

3. Execute the following queries.  Execute in your Shell.
    * Print the results in JSON format.
    ````
    <copy>
    mode JSON -pretty
    </copy>
    ````
    * Fetch all the details for a particular flight number.
    ````
    <copy>
    SELECT *
    FROM demo d
    WHERE d.bagInfo.flightLegs.flightNo =ANY 'BM715';
    </copy>
    ````
    * Select some passenger information using a particular flight BM715.
    ````
    <copy>
    SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
    FROM demo d
    WHERE d.bagInfo.flightLegs.flightNo =ANY 'BM715';
    </copy>
    ````
    * Select some passenger information using a combination of flights BM715 and BM204.
    ````
    <copy>
    SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
    FROM demo d
    WHERE d.bagInfo.flightLegs.flightNo =ANY "BM715"
    AND d.bagInfo.flightLegs.flightNo =ANY "BM204";
    </copy>
    ````
    * Select the passenger information using a combination of flights BM715 and BM204 and having only 2 flight legs.
    ````
    <copy>
    SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
    FROM   demo d
    WHERE  d.bagInfo.flightLegs.flightNo =ANY "BM715"
    AND    d.bagInfo.flightLegs.flightNo =ANY "BM204"
    AND    size(d.bagInfo.flightLegs) = 2;
    </copy>
    ````

4. Write queries to answer the following questions.

    * Retrieve the names and phone numbers for passengers that had a bag with any action on any flight leg that occurred at the Sydney Airport(SYD).  Hint: Every record has an actions array at: bagInfo.flightLegs.actions
    * Find the number of bags on flight BM715.  Hint: The size of the bagInfo array represents the number of bags a passenger has checked.

    **Note:** The Learn More contains a link to the SQL Reference Guide.  Lab 2 contains an example of the JSON record to look at.

5. Type in **exit** to exit from the CLI application.


## Acknowledgements
* **Author** - Dario Vega, Product Manager, NoSQL Product Management and Michael Brey, Director, NoSQL Product Development
* **Last Updated By/Date** - Dario Vega, Product Manager, NoSQL Product Management June 2025
