#First create schema definitions in the database

java -cp ../bin:../lib/kvclient.jar com.oracle.fleet.init.CreateSchema localhost:5000 kvstore

./runScript.sh importData.kvs
