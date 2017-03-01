for i in {1..1}
do
 # your-unix-command-here
 echo "Re-initializing the workload"
 $FLEET_HOME/scripts/runScript.sh $FLEET_HOME/scripts/deleteMileage.kvs
 java -cp ../bin:../lib/kvstore.jar  com.oracle.fleet.util.FeedLoader
 done

