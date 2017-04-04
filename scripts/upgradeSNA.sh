#~/bin/bash

SN=$1

### Run makebootconfig command ### 
if [ -z "$SN" ]
then 
  echo "Error: Storage Node ID is not passed as an argument."
  echo "Action: Please rerun the upgrade script by passing storage node id, for example sn1, sn2 etc"
  exit 1;
else
  ####   Stopping the storage node agent running on given SN ##
  echo "Stopping Storage Node: " $SN 
  java -jar $KVHOME/lib/kvstore.jar stop -root /tmp/data/$SN/kvroot &
  #### Sleeping for 5 seconds
  sleep 10

  ###   Starting the storage node agent from new kvhome ######
  echo "Starting Storage Node from NEW_KVHOME: " $NEW_KVHOME
  java -jar $NEW_KVHOME/lib/kvstore.jar start -root /tmp/data/$SN/kvroot &
  #### Sleeping for 10 seconds to give enough time for services to come online ##
  sleep 30

  ####  Display the new upgrade order
  ./showUpgradeOrder.sh

fi

