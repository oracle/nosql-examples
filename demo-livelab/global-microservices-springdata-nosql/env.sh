#!/bin/bash
#
# Copyright (c) 2024 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

CMP_ID=`oci iam compartment list --name  demonosql  --compartment-id $OCI_TENANCY | jq -r '."data"[].id'`
if [ -z "$CMP_ID" ];  then unset CMP_ID  ; fi;
# Advanced user, if you deploy in a compartment other than root or root/demonosql, change the following line with the good compartment_ocid and unconmment
#CMP_ID="ocid1.compartment.oc1..xxxxxxxxxxxxxxxxxx"

export NOSQL_SERVICETYPE="useDelegationToken"
export NOSQL_REGION=$OCI_REGION
export OCI_NOSQL_COMPID=${CMP_ID-$OCI_TENANCY}

echo "OCI_TENANCY: $OCI_TENANCY"
echo "NOSQL_REGION: $NOSQL_REGION"
echo "OCI_NOSQL_COMPID: $OCI_NOSQL_COMPID"
