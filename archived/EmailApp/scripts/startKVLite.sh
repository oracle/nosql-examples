
./resetEnv.sh

#Start KVLite from the kvroot
echo "Starting KVLite ..."
nohup java -jar $KVHOME/lib/kvstore.jar kvlite -root $KVROOT -host localhost -secure-config disable 2>&1 > /tmp/kvlite.out &
