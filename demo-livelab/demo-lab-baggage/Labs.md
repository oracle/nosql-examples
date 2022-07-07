# WORKSHOP OUTLINE
1. Setup - 10 minutes
2. NoSQL - 20 minutes
3. Execute and Review Code Functions - 20 minutes
4. Execute Streaming tests - 5 minutes
5. Execute API tests - 5 minutes
6. Review Streaming/API/Functions configuration 


## LAB1 - Setup - 10 minutes

### Step 1. Create a compartment 
Go to OCI console -> Identity & Security -> Compartments.

Click on Create Compartment. This opens up a new window.

Choose **demonosql** as compartment name, choose a description and add it.

### Step 2. Create an API Key and Auth Token for your user

Login to OCI console.

Click on your Profile -> User Settings. On the bottom left, click on Auth Tokens. Click on Generate Token.

Provide a description and then hit Generate Token. This will generate a token. **Make sure to copy the token and save it for future steps**.

### Step 3. Deploy the PoC application

To deploy the application, we will use a terraform scripts provided for this Lab. 


[![Deploy to Oracle Cloud - home](https://oci-resourcemanager-plugin.plugins.oci.oraclecloud.com/latest/deploy-to-oracle-cloud.svg)](https://cloud.oracle.com/resourcemanager/stacks/create?region=home&zipUrl=https://github.com/dario-vega/demo-lab-baggage/archive/refs/heads/main.zip)

Note: Instead of Clicking in the image Deploy to Oracle Cloud directly - Click on button right in order to Open Link in New Tab 


Provide your **Cloud Account Name** and click on Next

Login in your account using your credentials

Click on the box "I have reviewed and accept the Oracle Terms of Use."

Click on Next

Configure the variables for the infrastructure resources that this stack will create when you run the apply job for this execution plan.
Choose demonosql as _Compartment_, Provide your username in the text box _OCIR username_ then the token copied in step2 _OCIR user password_

Click on Next

Review and Click on Create

A job will run automatically. It takes 5 minutes. Wait still State becomes Succeeded


### Step 4. Understand the dynamic group and the policies 


**Oracle NoSQL Database Cloud Service uses Oracle Cloud Infrastructure Identity and Access Management to provide secure access to Oracle Cloud.** Oracle Cloud 
Infrastructure Identity and Access Management enables you to create user accounts and give users permission to inspect, read, use, or manage tables. 

The Oracle NoSQL Database SDKs allow you to provide the credentials to an application in multiple ways. 
The credentials that are used for connecting your application are associated with a specific user. You can create a user for your application.

In this snippet, we will use the API Key created in Step 2.

````
       return new NoSQLClient({
            region: Region.EU_FRANKFURT_1,
			compartment:'ocid1.compartment.oc1..aaaaaaaamg....y3hsyi57paa',
            auth: {
                iam: {
                    tenantId: 'ocid1.tenancy.oc1..aaaaaaaahrs4avamaxisc...........slpsdb2d2xe2kp2q',
                    userId: 'ocid1.user.oc1..aaaaaaaaq.......co3ssybmexcu4ba',
                    fingerprint: 'e1:4f:7f:e7:b5:7c:11:38:ed:e5:9f:6d:92:bb:ae:3d',
                    privateKeyFile: 'NoSQLprivateKey.pem'
                }
            }
        });
````

Another way to do this connection is using **Dynamic groups**

To enable a function to access another Oracle Cloud Infrastructure resource, you have to include the function in a dynamic group, and then create a policy to grant the
dynamic group access to that resource. 

Having set up the policy and the dynamic group, you can then include a call to a 'resource principal provider' in your function code. The resource principal 
provider uses a resource provider session token (RPST) that enables the function to authenticate itself with other Oracle Cloud Infrastructure services. The token 
is only valid for the resources to which the dynamic group has been granted access. 

**Dynamic groups** allow you to group Oracle Cloud Infrastructure compute instances as "principal" actors (similar to user groups). You can then create policies to 
permit instances to make API calls against Oracle Cloud Infrastructure services. When you create a dynamic group, rather than adding members explicitly to the group, 
you instead define a set of matching rules to define the group members

After doing the setup (**done for you in the step 3**), you just use **Resource Principals to do the connection to NoSQL Cloud Service** as shown below.

In this snippet, there are hard-coded references (eg REGION).

**NoSQL Database Node.js SDK**
```
function createClientResource() {
  return  new NoSQLClient({
    region: Region.EU_FRANKFURT_1,
    compartment:'ocid1.compartment.oc1..aaaaaaaafml3tc......jimgx3cdnirgup6rhptxwnandq',
    auth: {
        iam: {
            useResourcePrincipal: true
        }
    }
  });
}
```
**NoSQL Database Python SDK**
```
def get_handle():
     provider = borneo.iam.SignatureProvider.create_with_resource_principal()
     config = borneo.NoSQLHandleConfig('eu-frankfurt-1', provider).set_logger(None)
     return borneo.NoSQLHandle(config)
```

### Step 5. Cloud Shell Configuration - clone github, execute shell data.sh and setup the fn env.

Open the Cloud Shell (click in the icon > ) in the top right menu

````
git clone https://github.com/dario-vega/demo-lab-baggage
sh ~/demo-lab-baggage/data.sh
````

Go to OCI console -> Developer Services -> Applications

On the left List Scope - Compartment - choose demonosql compartment

Click on nosql_demos application

On the left choose Getting Started

Click on Cloud Shell Setup

Execute the steps 1 to 7 provided in this Wizard 

Note: In the step 4 replace [OCIR-REPO] by demonosql (the name of the compartment)

![Working](FunctionsSetup.png)


## LAB2 NoSQL - 20 minutes

### Step 1. NoSQL Tables Deployment

Open the Cloud Shell (click in the icon > ) in the top right menu. Use the following instructions


Creating NoSQL tables using oci-cli - DDL for create tables in this [directory](./objects) (e.g demo.nosql)
```
cd ~/demo-lab-baggage/objects
CMP_ID=`oci iam compartment list --name  demonosql | jq -r '."data"[].id'`
COMP_ID=${CMP_ID-$OCI_TENANCY}
echo $COMP_ID
DDL_TABLE=$(cat demo.nosql)
echo $DDL_TABLE

oci nosql table create --compartment-id "$COMP_ID"   \
--name demo --ddl-statement "$DDL_TABLE" \
--table-limits="{\"maxReadUnits\": 15,  \"maxStorageInGBs\": 1,  \"maxWriteUnits\": 15 }"

DDL_TABLE=$(cat demoKeyVal.nosql)
echo $DDL_TABLE

oci nosql table create --compartment-id "$COMP_ID"   \
--name demoKeyVal  --ddl-statement "$DDL_TABLE" \
--table-limits="{\"maxReadUnits\": 15,  \"maxStorageInGBs\": 1,  \"maxWriteUnits\": 15 }"

```

This section is for information purpose only: How to create a NoSQL tables Using Terraform.
Please review the following script, it was executed automatically during the step 3

```
cat nosql.tf
```

Minimize Shell Cloud Console

### Step 2. Adding Data to the NoSQL table from the OCI Console 

Go to OCI console -> Databases -> Oracle NoSQL Database - Tables

On the left List Scope - Compartment - choose demonosql compartment

Click on demo table

Click on Insert Row. This opens up a new window. Choose Advanced Json Input 

Copy/Paste the json Baggage document in JSON input text box

Click on Insert Row

### Step 3.  Show data from the Console

On the left Click on Table Rows

In the textbox Query, keep the text SELECT * FROM demo

Click on Run query 

###  Show queries - Working in progress

```
cd ~/demo-lab-baggage/objects
cat queries.sql

```

## LAB3  Execute and Review Code Functions  - 20 minutes

### Step 1 Review and Execute the code

Open the Cloud Shell (click in the icon > ) in the top right menu. 


Execute the following instructions, it is mandatory for next Steps - if you close/open the Cloud Shell Console, please reexecute

```
CMP_ID=`oci iam compartment list --name  demonosql | jq -r '."data"[].id'`
COMP_ID=${CMP_ID-$OCI_TENANCY}
echo $COMP_ID
export APP_NAME="nosql_demos"

```

### Step 2 Load data in the table

The goal of this lab is to understand the difference betweent the 2 data model proposed.

This function will load lines in the table demoKeyVal


```
cd ~/demo-lab-baggage/functions-fn
cd load/demo-keyval-load
cat ~/BaggageData/baggage_data_file99.json | fn invoke $APP_NAME demo-keyval-load
cat ~/BaggageData/baggage_data_file99.json | fn invoke $APP_NAME demo-keyval-load
cat ~/BaggageData/baggage_data_file103.json  | fn invoke  $APP_NAME demo-load
```

Use the steps in the previous Lab to read the data for this table 


This function will load lines in the table demo

```
cd ~/demo-lab-baggage/functions-fn
cd load/demo-load
cat ~/BaggageData/baggage_data_file99.json | fn invoke  $APP_NAME demo-load
cat ~/BaggageData/baggage_data_file99.json | fn invoke  $APP_NAME demo-load 
cat ~/BaggageData/baggage_data_file103.json  | fn invoke  $APP_NAME demo-load
```

### Step 3 Read data using a REST API application

In this first step, we will review the code and trigger the fuction manually using the `fn invoke` cli command.

In the nexts Labs, we will trigger the same function using an Endpoint from your API/Web browser. Integration with API Gateway

```
cd ~/demo-lab-baggage/functions-fn
cd api/demo-api

echo '{"ticketNo":"1762386738153", "endPoint":"getBagInfoByTicketNumber"}' | fn invoke $APP_NAME demo-api | jq
echo '{"endPoint":"getBagInfoByTicketNumber"}' | fn invoke $APP_NAME demo-api | jq
echo '{"endPoint":"getBagInfoByTicketNumber"}' | fn invoke $APP_NAME demo-api | jq '. | length'
```
As you can see The getPassengersAffectedByFlight is still under construction
```
echo '{"endPoint":"getPassengersAffectedByFlight"}' | fn invoke $APP_NAME demo-api | jq
echo '{"endPoint":"getPassengersAffectedByFlight"}' | fn invoke $APP_NAME demo-api | fn invoke $APP_NAME demo-api | jq
```

In the next Lab, we will proceed to implement this function with you
In the meantime, some queries that you can run from the console

````
SELECT * 
FROM demo d 
WHERE d.bagInfo.flightLegs.flightNo =ANY 'BM715'

SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo 
FROM demo d 
WHERE d.bagInfo.flightLegs.flightNo =ANY 'BM715'

SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
FROM demo d 
WHERE d.bagInfo.flightLegs.flightNo =ANY "BM715" 
AND d.bagInfo.flightLegs.flightNo =ANY "BM204"

SELECT d.fullName, d.contactPhone, d.ticketNo , d.bagInfo.flightLegs.flightNo as bagInfo
FROM   demo d 
WHERE  d.bagInfo.flightLegs.flightNo =ANY "BM715" 
AND    d.bagInfo.flightLegs.flightNo =ANY "BM204"
AND    size(d.bagInfo.flightLegs) = 2

````

````
SQL_STATEMENT=$(cat ~/demo-lab-baggage/objects/query1.sql | tr '\n' ' ')
echo $SQL_STATEMENT
echo "{\"sql\":\"$SQL_STATEMENT\",\""endPoint\"": \""executeSQL\"" }"  | fn invoke $APP_NAME demo-api

oci nosql query execute -c  $COMP_ID --statement "$SQL_STATEMENT"

````

### Step 4 Load Data using Streaming


In this first step, we will review the code and trigger the fuction manually using the `fn invoke` cli command.

In the nexts labs, we will trigger the same function using Streams. Integration with Streaming and Service Connector


```
cd ~/demo-lab-baggage/functions-fn
cd streaming/load-target
var1=`base64 -w 0 ~/BaggageData/baggage_data_file99.json`
cp test_templ.json stream_baggage_data_file99.json
sed -i "s/<here>/$var1/g"  stream_baggage_data_file99.json

fn invoke $APP_NAME load-target < stream_baggage_data_file99.json

```


## LAB4 Execute Streaming tests - 5 minutes

Go to OCI console -> Databases -> Analytics & AI -> Messaging -Streaming

On the left List Scope - Compartment - choose demonosql compartment

Click on nosql_demos

Click on Produce Test Message

Publishing messages to the Stream instance from the OCI Console (copy/paste the json Baggage document in Data text box.).

![Working](PublishMessage.png)

using OCI cli commands in order to simulate real-time traffic from the Cloud Shell

````
CMP_ID=`oci iam compartment list --name  demonosql | jq -r '."data"[].id'`
COMP_ID=${CMP_ID-$OCI_TENANCY}
echo $COMP_ID

cd ~/demo-lab-baggage/functions-fn
cd streaming/load-target
STREAM_OCID=`oci streaming admin stream list --compartment-id $COMP_ID --name nosql_demos --lifecycle-state ACTIVE | jq -r '."data"[].id'`
STREAM_ENDPOINT=`oci streaming admin stream list --compartment-id $COMP_ID --name nosql_demos --lifecycle-state ACTIVE | jq -r '."data"[]."messages-endpoint"'`
echo $COMP_ID
echo $STREAM_OCID
echo $STREAM_ENDPOINT

for file in `ls -1 ~/BaggageData/baggage_data* | tail -20`; do
  echo $file
  filename=`basename $file` 
  var1=`base64 -w 0 $file`
  cp stream_oci_cli_templ.json stream_oci_cli_$filename
  sed -i "s/<here>/$var1/g"  stream_oci_cli_$filename
  oci streaming stream message put --stream-id  $STREAM_OCID \
  --messages file://stream_oci_cli_$filename --endpoint $STREAM_ENDPOINT
  sleep 2
done
````


## LAB5 Execute API tests  - 5 minutes

Execute the following commmands
````
CMP_ID=`oci iam compartment list --name  demonosql | jq -r '."data"[].id'`
COMP_ID=${CMP_ID-$OCI_TENANCY}
echo $COMP_ID
IFS=$'\n'
unset ticketNo
HOSTAPI=`oci api-gateway gateway list --compartment-id $COMP_ID --lifecycle-state ACTIVE --display-name nosql_demos | jq -r '."data".items[]."hostname"'`
URL="https://$HOSTAPI/BaggageDemo/getBagInfoByTicketNumber"

echo $HOSTAPI
echo $URL
````
Click on the URL provided, This opens up a new Tab with the Result to call the getBagInfoByTicketNumber Endpoint API


using CURL to simulate multiples call for each ticketNo


````


ticketNo=($(curl ${URL} | jq -r '[.[].ticketNo] | .[]?'	))
for (( i=0; i<${#ticketNo[@]}; i++ )); do
   curl -X GET -k -d '{"name":"${ticketNo[i]}"}' "${URL}?ticketNo=${ticketNo[i]}" 2>/dev/null | jq
   sleep 2
done
````

## LAB6  Review Streaming/API/Functions configuration

Show OCI Console

