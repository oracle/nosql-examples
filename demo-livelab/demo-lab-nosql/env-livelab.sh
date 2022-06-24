# Please modify the following to variables
export CMP_ID=your_compartment_ocid
export NOSQL_USER_ID=your_user_ocid
export NOSQL_FINGERPRINT=your_fingerprint

export COMP_ID=${CMP_ID}
export NOSQL_COMP_ID=${CMP_ID}
export NOSQL_PRIVKEY_FILE=~/NoSQLLabPrivateKey.pem


if [ $CMP_ID == "your_compartment_ocid" ] || [ $NOSQL_USER_ID == "your_user_ocid" ] || [ $NOSQL_FINGERPRINT == "your_fingerprint"  ]
then
  echo "ERROR: Please review the configuration - missing API key parameters"
  return
fi

if  [ ! -e $NOSQL_PRIVKEY_FILE ]
then
  echo "ERROR: Please review the configuration - missing NOSQL_PRIVKEY_FILE file"
  return
fi

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

oci nosql table list --compartment-id $NOSQL_COMP_ID > /dev/null
