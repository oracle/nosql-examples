#!/bin/bash
#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

java -Xmx64m -Xms64m -jar $KVHOME/lib/kvclient.jar

STORESEC=${1-configure}

if [[ $STORESEC == "configure" ]]
then
  echo "Creating password"
  openssl rand -out $KVROOT/kspwd -base64 14
  KSPWD=`cat $KVROOT/kspwd`
else
  KSPWD=`cat $KVROOT/security/kspwd`
  STORESEC=enable
fi


echo $KSPWD
echo "Creating boot store-security $STORESEC -kstype PKCS12"

java -jar $KVHOME/lib/kvstore.jar makebootconfig \
-root $KVROOT \
-port 5000 \
-host $KVHOST \
-harange 5010,5020 \
-servicerange 5021,5049 \
-admin-web-port 5999 \
-store-security $STORESEC \
-pwdmgr wallet  \
-kstype PKCS12 \
-kspwd $KSPWD \
-mgmt jmx \
-capacity 0

if [[ $STORESEC == "configure" ]]
then
  echo "moving password file to $KVROOT/security"
  echo mv $KVROOT/kspwd $KVROOT/security/kspwd
fi
