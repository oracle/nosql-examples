#!/bin/bash

TMP=/tmp/ons
mkdir -p $TMP/log

export MALLOC_ARENA_MAX=1
nohup java -Xmx256m -Xms256m -jar $KVHOME/lib/kvstore.jar start -root $KVROOT >$TMP/log/kvstore.log 2>&1 </dev/null &

exit 0
