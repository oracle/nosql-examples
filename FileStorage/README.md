Objective:

The objective of this demo is to show the ability of Oracle NoSQL database 
being used to store Files(PDF) in Binary Format. This demo showcases:

1. Usage of the Binary Datatype in Oracle NoSQL Table.
2. Retrieve a Binary Type data and convert back to the file.

Following are some of the features built in this demo

1. The ability to read files from a specific location.
2. convert the file to binary and store it along with its File Attributes like date, owner in a configured table.
3. Given the id of the table row retrieve the binary data for the file and convert it back to a filt type.

--------------------------------------------------------------------------------
To Run File Storage demo application 
--------------------------------------------------------------------------------

1. Download the source and eclipse project from github.

2. Import the project into Eclipse
   
3. Before you start looking at running the application you need to create the table.

4. Ensure you have Oracle NoSQL up and running and can connect to the Cluster using Admin Cli.

5. Once you are in your Oracle NoSQL Store, run the following command to create the table.

#Create fileinfo table
execute 'CREATE TABLE fileinfo (id STRING,date STRING,owner STRING,file BINARY, PRIMARY KEY (id))'

6. Modify config.properties in src/main/resources to update the 

storeName=mystore
#store config should be specified as series of host:port for eg xyz:5000,abc:6000
storeconfig=localhost:5000
#Name of the table which should be already created in KV.
tableName=fileinfo
		
7. This project is build using Maven. So you need to ensure that in eclipse you have Maven plugin installed and configured.
		
8. Right Click on the project and select Run As -> Maven -> New Configuration. In Goal enter "Clean Install".

9. Once the build is successfull Right Client on Project and select Run As -> Run Configurations.9

a. Make sure you have the right entries in Project and Main Class.
b. Click on Arguments Tabs
c. As part of Program Arguments enter the location of the Directory where all the files that you want to store in the table using "-i" option.

-i /home/user1/files
       
10. This should run the application, read all the files in the specified directory, convert them to Binary and store them in the table.
   ~~~~~~~~ Have fun playing with the application. ~~~~~~~~~~~
   
