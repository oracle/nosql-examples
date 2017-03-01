clear

## Killing java processes ##
echo "Killing Java processes ..."
jps | grep -v eclipse | awk '{print $1}' | xargs -i kill -9 {}
ps -ef | grep FeedLoader | awk '{print $2}' | xargs -i kill -9 {}

#Delete old KVROOT and kvlite.out file from /tmp folder
echo "Delete old KVROOT and kvlite.out file from /tmp folder..."
rm -rf /tmp/kvroot
rm /tmp/kvlite.out

#Start KVLite from the kvroot
echo "Starting KVLite ..."
startKVLite.sh
jps -m

sleep 20

#Create Schemas
echo "Creating tables now"
createTables.sh

sleep 10

#Start the workload
echo "Starting workload process to run in the background ..."
startWorkload.sh &
