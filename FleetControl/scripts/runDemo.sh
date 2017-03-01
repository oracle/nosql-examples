clear


#Start the workload
echo "Starting workload process to run in the background ..."
startWorkload.sh &

sleep 10

echo "Starting Graphs ..."
startGraphs.sh
