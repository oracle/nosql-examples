Objective:

The objective of this demo is to show the ability of Oracle NoSQL database 
being used to demonstrate the JSON datatype, Parent-Child Modelling and Bulk Put.
This demo showcases:

1. Usage of the JSON Datatype in Oracle NoSQL Database Table.
2. Retrieve a Json Type data.
3. Model Data as Parent and Child.
4. Usking Bulk Put

Following are some of the features built in this demo

1. the ability to read data from a ";" seperated file.
2. Read the JSON string and store in an Oracle NoSQL Database table column of type JSON.
3. Given the id (Primary Key) of the table row retrieve the data from Parent and Child table.
4. Use Bulk Put to insert data.

--------------------------------------------------------------------------------
To Run Quotes demo application 
--------------------------------------------------------------------------------

1. Download the source and eclipse project from github.

2. Import the project into Eclipse
   
3. Before you start looking at running the application you need to create the table.

4. Ensure you have Oracle NoSQL up and running and can connect to the Cluster using Admin Cli.

5. Once you are in your Oracle NoSQL Store, run the following command to create the table.

execute 'CREATE TABLE customer (id LONG, email STRING, phone STRING,age INTEGER, PRIMARY KEY(id))'
execute 'CREATE TABLE customer.quotes (qid LONG, quotes JSON, PRIMARY KEY(qid))'

6. Modify config.properties in src/main/resources to update the 

storeName=mystore
hostName=localhost
hostPort=9000
tableName=customer
columns=id,email,phone,age
childTableName=customer.quotes
childColumns=qid,quotes
noOfRecords=1000000
noOfStreams=10
		
7. This project is build using Maven. So you need to ensure that in eclipse you have Maven plugin installed and configured.
		
8. Right Click on the project and select Run As -> Maven -> New Configuration. In Goal enter "Clean Install".

9. Once the build is successfull Right Client on Project and select Run As -> Run Configurations.9

a. Make sure you have the right entries in Project and Main Class.
b. Click on Arguments Tabs
c. As part of Program Arguments enter the location of the Directory where all data files that you want to store in the table using "-i" option.

-i /home/user1/files
       
10. This should run the application, read all the files in the specified directory, convert them to Binary and store them in the table.
   ~~~~~~~~ Have fun playing with the application. ~~~~~~~~~~~
   
