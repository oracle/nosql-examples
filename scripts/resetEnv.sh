#!/bin/sh

## Killing java processes ##
echo "Killing Java processes ..."
java -jar $KVHOME/lib/kvstore.jar stop -root $KVROOT

echo "Deleting old directories..."
## Delete Directories ##
./deleteDir.sh
./security/cleanup.sh

echo "Creating new directories..."
## Create Directories
./createDir.sh


