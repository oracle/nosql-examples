#!/bin/sh

java -jar $KVHOME/lib/kvstore.jar stop -root /tmp/data/sn1/kvroot
java -jar $KVHOME/lib/kvstore.jar stop -root /tmp/data/sn2/kvroot
java -jar $KVHOME/lib/kvstore.jar stop -root /tmp/data/sn3/kvroot
java -jar $KVHOME/lib/kvstore.jar stop -root /tmp/data/sn4/kvroot
