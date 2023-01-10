# Replacing a Failed Disk 

You can replace a disk that is either in the process of failing, or has already failed. 
Disk replacement procedures are necessary to keep the store running. 
These are the steps required to replace a failed disk to preserve data availability. 

The replication data itself is stored by each distinct Replication Node service on separate, physical media as well. 
Storing data in this way provides failure isolation and will typically make disk replacement less complicated and time consuming

To replace a failed disk: 



1. Determine which disk has failed. To do this, you can use standard system monitoring and management mechanisms.
2. Then given a directory structure, determine which Replication Node service to stop.
3. Use the plan stop-service command to stop the affected service (rg2-rn3) so that any attempts by the system to communicate with it are no longer made
`kv-> plan stop-service -service rg2-rn3`
4. Remove the failed disk (disk2) using whatever procedure is dictated by the operating system, disk manufacturer, and/or hardware platform.
5. Install a new disk using any appropriate procedures.
6. Format the disk to have the same storage directory as before
7. With the new disk in place, use the plan start-service command to start
`kv-> plan stop-service -service rg2-rn3`

# HDD Failure simulation

````
kv-> plan stop -service rg1-rn3
rm -rf ${KVDATA}/disk1/rg1-rn3
kv-> plan start -service rg1-rn3
````

