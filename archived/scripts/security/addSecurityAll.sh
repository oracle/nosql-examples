#!/bin/sh

java -jar $KVHOME/lib/kvstore.jar securityconfig<<EOF
config add-security -root /tmp/data/sn1/kvroot -secdir security -config config.xml
config add-security -root /tmp/data/sn2/kvroot -secdir security -config config.xml
config add-security -root /tmp/data/sn3/kvroot -secdir security -config config.xml
config add-security -root /tmp/data/sn4/kvroot -secdir security -config config.xml
exit

EOF

