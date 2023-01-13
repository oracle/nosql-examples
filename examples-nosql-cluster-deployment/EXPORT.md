# Export/Import

## Using the Import and Export Utilities

Oracle NoSQL Database contains an import/export utility to extract and load table based data, raw key/value based data, and large object data. 
You can use the import/export utility, available through kvtool.jar, to:
* Export table data from Oracle NoSQL Database and store the data as JSON formatted files on a local (or network mounted) file system.
* Import ad-hoc JSON data generated from a relational database or other sources, and JSON data generated via MongoDB strict export.
* Export data and metadata from one or more existing Oracle NoSQL Database tables, raw key/value based data, and large object data to a compact binary format.
* Read data from, or write data to files in the file system.
* Import one or more tables into an Oracle NoSQL Database.
* Restart from a checkpoint if an import or export fails before completion.

### Export the entire contents of Oracle NoSQL Database data store

1. Export the entire contents of Oracle NoSQL Database data store
````
cd ~/examples-nosql-cluster-deployment/script
mkdir -p ~/kvstore_export
cat export_config
# modify the path if necessary
java -jar $KVHOME/lib/kvtool.jar export -export-all -store OUG -helper-hosts node1-nosql:5000  -config export_config -format JSON
````

2. Import all data from the export package created in 1 into a different Oracle NoSQL Database data store
For demo purpose, we will use the same data store - we need drop the tables before execute it

````
cd ~/examples-nosql-cluster-deployment/script
cat import_config
# modify the path if necessary
java -jar $KVHOME/lib/kvtool.jar import -import-all -store OUG -helper-hosts node1-nosql:5000  -config import_config -status /home/opc/checkpoint_dir -format JSON	
````


## Oracle NoSQL Data Migrator Vs. Import/Export Utility 

The Oracle NoSQL Data Migrator is created to replace and enhance the existing on-premise-only import/export utility. 
It moves the NoSQL table data and schema definition between a source and a sink or target. 
It supports multiple sources and sinks as listed in Supported Sources and Sinks. 
However, the import/export utility lets you import into or export from Oracle NoSQL Database (on-premise) only. 
That is, using the import/export utility, you can either import data into the Oracle NoSQL Database or export data from Oracle NoSQL Database. 
When you export, the source type is always Oracle NoSQL Database (where you extract data from) and the sink is the recipient of that data. 
When you import, the source type is currently limited to a file and the sink is always Oracle NoSQL Database

see example here

