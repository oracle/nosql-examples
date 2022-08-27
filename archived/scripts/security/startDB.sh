#!/bin/sh


nohup java -jar $KVHOME/lib/kvstore.jar start -root /tmp/data/sn1/kvroot &
nohup java -jar $KVHOME/lib/kvstore.jar start -root /tmp/data/sn2/kvroot &
nohup java -jar $KVHOME/lib/kvstore.jar start -root /tmp/data/sn3/kvroot &
nohup java -jar $KVHOME/lib/kvstore.jar start -root /tmp/data/sn4/kvroot &

