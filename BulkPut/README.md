NoSQL

Oracle NoSQL bulk put example

usage: bulkput.BulkPutExample -store -host -port [-load <# records to load>] (default: 1000) [-streamParallelism ] (default: 1) [-perShardParallelism ] (default: 2) [-heapPercent ] (default: 70)] [-useTable]

The "-useTable" flag is used to indicate to load rows a table, then internally it will create a table "users" and load records to it. If not use "-useTable" then the key/value entries are loaded to store. Please see the details for the record format for key/value record and table "users" schema in the java doc of BulkPutExample.

e.g. To load 2000 key/value entries to stores with 3 streams

java -cp $KVHOME/lib/kvclient.jar:$KVHOME/../build/examples/classes bulkput.BulkPutExample -store kvstore -host localhost -port 5000 -streamParallelism 3 -load 2000 host:localhost port:5000 store:kvstore numToload:2,000 useTable:false bulkWriteOptions: streamParallelism:3 perShardParallelism:2 heapPercent:70
Loading KVs... LoadKVStream0- [0, 667) completed, loaded: 667 LoadKVStream1- [667, 1334) completed, loaded: 667 LoadKVStream2- [1334, 2000) completed, loaded: 666 Loaded 2,000 records, 0 pre-existing.

e.g. To load 2000 rows to table users with 3 streams

java -cp $KVHOME/lib/kvclient.jar:$KVHOME/../build/examples/classes bulkput.BulkPutExample -store kvstore -host localhost -port 5000 -streamParallelism 3 -load 2000 -useTable host:localhost port:5000 store:kvstore numToload:2,000 useTable:true bulkWriteOptions: streamParallelism:3 perShardParallelism:2 heapPercent:70
Created table users. Loading rows to users... LoadRowStream0- [0, 667) completed, loaded: 667 LoadRowStream1- [667, 1334) completed, loaded: 667 LoadRowStream2- [1334, 2000) completed, loaded: 666 Loaded 2,000 rows to users, 0 pre-existing.
