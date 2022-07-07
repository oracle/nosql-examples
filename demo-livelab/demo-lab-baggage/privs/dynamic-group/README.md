## Examples - OCI commands to create dynamic-group and policies

To enable a function to access another Oracle Cloud Infrastructure resource, you have to include the function in a dynamic group, and then create a policy to grant 
the dynamic group access to that resource. 

Having set up the policy and the dynamic group, you can then include a call to a 'resource principal provider' in your function code. The resource principal provider uses a 
resource provider session token (RPST) that enables the function to authenticate itself with other Oracle Cloud Infrastructure services. The token is only valid for the 
resources to which the dynamic group has been granted access. 

0) Retrive the OCID for the compartment demonosql
````
CMP_ID=`oci iam compartment list --name  demonosql | jq -r '."data"[].id'`
COMP_ID=${CMP_ID-$OCI_TENANCY}
PREFIX_POLICY=` [ -z "$CMP_ID" ] && echo "new_" `
echo $COMP_ID
echo $PREFIX_POLICY
````

1) Create the dynamic group

````
cd ~/demo-lab-baggage/privs/dynamic-group
export DYN_GROUP_NAME=nosql_demos
cp  example_dyn_group_rules.txt  dyn_group_rules.txt
sed -i "s/<here>/$COMP_ID/g"  dyn_group_rules.txt
RULES=$(cat dyn_group_rules.txt)
oci iam dynamic-group create --description "$DYN_GROUP_NAME" --name "$DYN_GROUP_NAME" --matching-rule "$RULES" 
````
2) set up the policies

````
cd ~/demo-lab-baggage/privs/dynamic-group
export POLICY_NAME=nosql_demos_faas
STREAM_OCID=`oci streaming admin stream list --compartment-id $COMP_ID --name nosql_demos --lifecycle-state ACTIVE | jq -r '."data"[].id'`
echo ${STREAM_OCID-"STREAM_OCID variable is empty. Review if you will execute the Advanced Labs - Streaming"}
````
If it returns **STREAM_OCID variable is empty. Review if you will execute the Advanced Labs - Streaming**, please review the Troubleshooting section ci-below

````
ls -lrt  ${PREFIX_POLICY}example_policy_demo.json
cp  ${PREFIX_POLICY}example_policy_demo.json  policy_demo.json
sed -i "s/<here>/$COMP_ID/g"  policy_demo.json
sed -i "s/<streamid>/$STREAM_OCID/g"  policy_demo.json
oci iam policy create  --compartment-id $COMP_ID --name $POLICY_NAME --description $POLICY_NAME \
--statements file://policy_demo.json 
````


**Troubleshooting** 

You need to create the dynamic groups and privileges from your HOME region to avoid the following error :

````
{
    "code": "NotAllowed",
    "message": "Please go to your home region IAD to execute CREATE, UPDATE and DELETE operations.",
    "opc-request-id": "351C608A5CFA4C9CA7F03CC1BA6A49E3/F9E31D486B9C9BB5EA973E0EFEB23C9B/F2EB27857DD51C9AEDA24A1453792066",
    "status": 403
}
````

If you decided to deploy in another region other than you HOME region, please case copy/paste your STREAM_OCID by run this command in your deployment region
````
STREAM_OCID=`oci streaming admin stream list --compartment-id $COMP_ID --name nosql_demos --lifecycle-state ACTIVE | jq -r '."data"[].id'`
echo "STREAM_OCID="${STREAM_OCID}
````



## Examples - OCI commands to get the dynamic-group and policies resources

````
oci iam dynamic-group get --dynamic-group-id "ocid1.dynamicgroup.oc1..aaaaaaaam5pmum7yojr6pmm26f4zfeq32awhvaiemfqwfgrxctl2y4uvvuaq"\
| jq -r '."data"."matching-rule"'
````
````
oci iam policy list --compartment-id $COMP_ID  | jq -r '."data"[].statements' 
````
