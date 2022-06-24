CMP_ID=`oci iam compartment list --name  demonosql  --compartment-id $OCI_TENANCY | jq -r '."data"[].id'`
if [ -z "$CMP_ID" ];  then unset CMP_ID  ; fi;
# Advanced user, if you deploy in a compartment other than root or root/demonosql, change the following line with the good compartment_ocid and unconmment
# CMP_ID=<your_compartment ocid>

export COMP_ID=${CMP_ID-$OCI_TENANCY}
export NOSQL_COMP_ID=${CMP_ID-$OCI_TENANCY}
export NOSQL_USER_ID=`cat ~/info.json | jq -r '."data"."user-id"'`
export NOSQL_FINGERPRINT=`cat ~/info.json | jq -r '."data"."fingerprint"'`
export NOSQL_PRIVKEY_FILE=~/NoSQLLabPrivateKey.pem

if [ $OCI_REGION == 'us-phoenix-1' ]
then
  export NOSQL_ALWAYS_FREE=true
else
  export NOSQL_ALWAYS_FREE=false
fi

echo "OCI_REGION: $OCI_REGION"
echo "OCI_TENANCY: $OCI_TENANCY"
echo "NOSQL_USER_ID: $NOSQL_USER_ID"
echo "NOSQL_FINGERPRINT: $NOSQL_FINGERPRINT"
echo "NOSQL_PRIVKEY_FILE: $NOSQL_PRIVKEY_FILE"
echo "NOSQL_COMP_ID: $NOSQL_COMP_ID"
echo "NOSQL_ALWAYS_FREE: $NOSQL_ALWAYS_FREE"

