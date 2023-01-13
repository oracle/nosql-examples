# Backing Up the Store

To make backups of your KVStore, use the CLI snapshot command to copy nodes in the store. 
To maintain consistency, no topology changes should be in process when you create a snapshot. 
Restoring a snapshot relies on the system configuration having exactly the same topology that was in effect when you created the snapshot. 

Due to the distributed nature and scale of Oracle NoSQL Database, it is unlikely that a single machine has the resources to contain snapshots for the entire store. 

## Managing Snapshots

When you create a snapshot, the utility collects data from every Replication Node in the system, including Masters and replicas. 
If the operation does not succeed for any one node in a shard, the entire snapshot fails.

The command “snapshot create” provided the backup name when it runs successfully.
````
kv_admin snapshot create -name BACKUP
Created data snapshot named 210705-101307-BACKUP on all 11 components
Successfully backup configurations on sn1, sn2, sn3
````

The command “snapshot create” is not providing the backup name if something goes wrong.
````
kv_admin snapshot create -name BACKUP
Create data snapshot succeeded but not on all components
Successfully backup configurations on sn1, sn2, sn3
````

AS YOU CAN SEE HERE, There is no warning or information if all replication nodes are unavailable for a replication group

````
kv_admin snapshot create -name BACKUP
Successfully backup configurations on sn1, sn2, sn3
````

use JSON output that shows more information and allow to see what exactly happened it. (same tests)
````
kv_admin snapshot create -name BACKUP -json 2>/dev/null
{
 "operation" : "snapshot operation",
 "returnCode" : 5000,
 "description" : "Operation ends successfully",
 "returnValue" : {
   "snapshotName" : "210705-133631-BACKUP",
   "successSnapshots" : [ "admin1", "admin2", "rg1-rn1", "rg1-rn2", "rg1-rn3", "rg2-rn1", "rg2-rn2", "rg2-rn3", "rg3-rn1", "rg3-rn2", "rg3-rn3" ],
   "failureSnapshots" : [ ],
   "successSnapshotConfigs" : [ "sn1", "sn2", "sn3" ],
   "failureSnapshotConfigs" : [ ]
 }
}
````
````
kv_admin snapshot create -name BACKUP -json 2>/dev/null
{
 "operation" : "snapshot operation",
 "returnCode" : 5500,
 "description" : "Operation ends successfully",
 "returnValue" : {
   "snapshotName" : "210705-133737-BACKUP",
   "successSnapshots" : [ "admin1", "admin2", "rg1-rn1", "rg1-rn2", "rg1-rn3", "rg2-rn1", "rg2-rn2", "rg2-rn3", "rg3-rn1", "rg3-rn2" ],
   "failureSnapshots" : [ "rg3-rn3" ],
   "successSnapshotConfigs" : [ "sn1", "sn2", "sn3" ],
   "failureSnapshotConfigs" : [ ]
 }
}
````
````
kv_admin snapshot create -name BACKUP -json 2>/dev/null
{
 "operation" : "snapshot operation",
 "returnCode" : 5500,
 "description" : "Operation ends successfully",
 "returnValue" : {
   "snapshotName" : "210705-133846-BACKUP",
   "successSnapshots" : [ "admin1", "admin2", "rg1-rn1", "rg1-rn2", "rg1-rn3", "rg2-rn1", "rg2-rn2", "rg2-rn3" ],
   "failureSnapshots" : [ "rg3-rn1", "rg3-rn2", "rg3-rn3" ],
   "successSnapshotConfigs" : [ "sn1", "sn2", "sn3" ],
   "failureSnapshotConfigs" : [ ]
 }
}
````
You can use the command `show topology` to have the backup path at each Storage Node (sn) :
* { rootDirPath }/snapshots/
* {storageDirEnvPath[]}/../snapsthots
* {adminDirsPath}/*/snapshots

````
kv_admin show topology -verbose -json | jq -r '.returnValue.sns[] | select (.resourceId == "sn1")|[{name:.resourceId,host:.hostname,rootDir:.rootDirPath,rns:.rns[]}]'
[
 {
   "name": "sn1",
   "host": "node1-nosql",
   "rootDir": "/home/opc/nosql/kvroot",
   "rns": {
     "resourceId": "rg1-rn1",
     "storageDirPath": "/home/opc/nosql/data/disk1",
     "storageDirEnvPath": "/home/opc/nosql/data/disk1/rg1-rn1/env",
     "storageDirSize": 524288000
   }
 },
 {
   "name": "sn1",
   "host": "node1-nosql",
   "rootDir": "/home/opc/nosql/kvroot",
   "rns": {
     "resourceId": "rg2-rn1",
     "storageDirPath": "/home/opc/nosql/data/disk2",
     "storageDirEnvPath": "/home/opc/nosql/data/disk2/rg2-rn1/env",
     "storageDirSize": 524288000
   }
 },
 {
   "name": "sn1",
   "host": "node1-nosql",
   "rootDir": "/home/opc/nosql/kvroot",
   "rns": {
     "resourceId": "rg3-rn1",
     "storageDirPath": "/home/opc/nosql/data/disk3",
     "storageDirEnvPath": "/home/opc/nosql/data/disk3/rg3-rn1/env",
     "storageDirSize": 524288000
   }
 }
]
````
NB: Currently the adminDirsPath is not shown. An enhacement request was filled. In the meantime, please use the following command :

````
kv_admin show parameter -service sn1 -json | jq -r -c '.returnValue.adminDirs[].path'
/home/opc/nosql/admin
````

