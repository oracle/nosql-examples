# Create Oracle NoSQL Database tables and Load data
## Introduction

This lab walks you through the steps to create and load NoSQL tables.
You have different approaches/ways to create and load tables in Oracle NoSQL
Database.
In this lab you will learn two different ways to do it, one from a Java
application  and second using Command Line Interface (CLI).

Estimated Time: 15 minutes

### Objectives

* Create a tables in Oracle NoSQL database
* Load the tables with sample data

## Option 1: Create an Oracle NoSQL Table from CLI:

1. Execute the following in your Shell.
```
<copy>
KV_VERSION=24.4.9
KVHOME=$HOME/kv-$KV_VERSION
</copy>
```
````
<copy>
cd ~/oracle-nosql-arrays
java -jar $KVHOME/lib/sql.jar -helper-hosts localhost:5000 -store kvstore
</copy>
````

2. You create NoSQL tables using the CLI command.  

```
<copy>
    create table users(
        acct_id integer,
        user_id integer,
        info JSON,
        primary key(acct_id, user_id));
 </copy>
```

The **info** column stores the information about the shows watched by each user.
The information is stored as a JSON document. Four sample rows are given in the
attached .json files.


3. Next, let’s create some indexes over the table:
```
<copy>
create index idx_country_showid_date on users(
    info.country as string,
    info.shows[].showId as integer,
    info.shows[].seriesInfo[].episodes[].date as string);

create index idx_country_genre on users(
    info.country as string,
    info.shows[].genres[] as string);

create index idx_showid on users(
    info.shows[].showId as integer)
    with unique keys per row;

create index idx_showid_minWatched on users(
    info.shows[].showId as integer,
    info.shows[].seriesInfo[].episodes[].minWatched as integer,
    info.shows[].seriesInfo[].episodes[].episodeID as integer)
    with unique keys per row;

create index idx_showid_seasonNum_minWatched on users(
    info.shows[].showId as integer,
    info.shows[].seriesInfo[].seasonNum as integer,
    info.shows[].seriesInfo[].episodes[].minWatched as integer,
    info.shows[].seriesInfo[].episodes[].episodeID as integer)
    with unique keys per row;

</copy>
 ```

The contents of these indexes for the 4 sample rows are shown in the .idx files in
this repository. As shown in these create index statements, the indexes index
fields that appear at different levels of data paths that contain nested arrays.

For example, the `idx_country_showid_date` index indexes the `showId` field inside
the top-level shows array and also the `date` field inside the episodes arrays,
 which are nested 2 levels deep under the show array.
````
 create index idx_country_showid_date on users(
     info.country as string,
     info.shows[].showId as integer,
     info.shows[].seriesInfo[].episodes[].date as string);
````

3. View the description of  the tables created.
```
<copy>
show tables
</copy>
```
```
<copy>
describe table users;
</copy>
```
```
<copy>
show indexes on users;
show as JSON indexes on users;
</copy>
```

```
<copy>
desc index idx_country_showid_date on users;
desc as JSON index idx_country_showid_date on users;
</copy>
```


4. Load data into the `users` table using the data file `baggage_data_file0.json`.
```
<copy>
put -table  users -file doc1.json;
put -table  users -file doc2.json;
put -table  users -file doc3.json;
put -table  users -file doc4.json;
</copy>
```
This successfully loads one row into the `users` table.

6. View the data that you populated in both the tables.

```
<copy>
SELECT * FROM users;
</copy>
```
```
<copy>
mode json -pretty;
SELECT * FROM users;
</copy>
```
```
<copy>
mode json -pretty;
SELECT * FROM users LIMIT 1;
</copy>
```

6. Drop the table.

We will recreate the table and indexes using a Java program in the next section

```
<copy>
DROP TABLE users;
</copy>
```
7. Exit.


## Option 2: Create an Oracle NoSQL Table from your Java application:


The java driver can be downloaded from [here](https://github.com/oracle/nosql-java-sdk)

```
<copy>
cd ~/oracle-nosql-arrays
curl -OL https://github.com/oracle/nosql-java-sdk/releases/download/v5.4.16/oracle-nosql-java-sdk-5.4.16.zip
unzip oracle-nosql-java-sdk-5.4.16.zip
</copy>
```

Use the following commands to compile and run the program:

```
<copy>
cd ~/oracle-nosql-arrays
javac -cp ./oracle-nosql-java-sdk/lib/nosqldriver.jar NestedArraysDemo.java
java -cp .:./oracle-nosql-java-sdk/lib/nosqldriver.jar NestedArraysDemo http://localhost:8080
</copy>
```

In this lab you have successfully created Oracle NoSQL Database tables and
populated them with data. You have also fetched the data that you populated.

You may now **proceed to the next lab.**


## Acknowledgements
* **Author** - Markos Zaharioudakis, Software Architect, NoSQL Product Development
* **Last Updated By/Date** - Dario Vega, Product Manager, NoSQL Product Management , June 2022
