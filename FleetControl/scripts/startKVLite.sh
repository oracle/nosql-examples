#Start KVLite from the kvroot
echo "Starting KVLite ..."
nohup java -jar $KVHOME/lib/kvstore.jar kvlite -root /tmp/kvroot -host localhost 2>&1 > /tmp/kvlite.out &

