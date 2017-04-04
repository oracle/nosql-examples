#!/bin/sh

## Killing java processes ##
echo "Killing Java processes ..."
jps | grep Launcher -v | awk '{print $1}' | xargs -i kill -9 {}

echo "Deleting old directories..."
## Delete Directories ##
./deleteDir.sh
./security/cleanup.sh

source /etc/profile.d/kvhost.sh

echo "Creating new directories..."
## Create Directories
./createDir.sh


