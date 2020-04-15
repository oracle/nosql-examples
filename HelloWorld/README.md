This 15-minute tutorial walks you through the steps to connect to Oracle NoSQL Database Cloud Service and do basic table level operations using a sample HelloWord application. This application is referenced in the [blog](https://blogs.oracle.com/nosql/15-minutes-to-hello-world)

# Background

Oracle NoSQL Database Cloud Service is a fully managed database cloud service that handles large amounts of data at high velocity. Developers can start using this service in minutes by following the simple steps outlined in this tutorial.

To get started with the service, you create a table.

After you sign up for an Oracle NoSQL Database Cloud Service account, you can easily create and populate a table by following these steps:

- Generate required keys and get the Tenancy ID and User ID
- Create a Java application with the required credentials
- Execute the application to connect to Oracle NoSQL Database Cloud Service.

# What Do You Need?

- Download and install Oracle NoSQL Java SDK from [Oracle Cloud Downloads](http://www.oracle.com/technetwork/topics/cloud/downloads/index.html#nosqlsdk).
- Access the [Java API Reference Guide](https://docs.oracle.com/en/cloud/paas/nosql-cloud/csnjv/index.html) to reference Java driver packages, methods, and interfaces.
- You can either create a [free trial account](https://www.oracle.com/cloud/free) or [buy a paid subscription](https://myservices.us.oraclecloud.com/mycloud/signup?selectedPlan=PAYG&language=en&sourceType=_ref_coc-asset-opcHome) by navigating to [oracle.com](https://www.oracle.com/database/nosql-cloud.html).

## Step 1 Acquire the Credentials

Acquire the following credentials that are required for running this sample application. See [Acquiring Credentials](https://docs.oracle.com/pls/topic/lookup?ctx=cloud&id=CSNSD-GUID-B09F1A47-98E4-4F02-AB23-5D4284F481F4).
OCID of the tenancy.
OCID of the user calling the API.
Fingerprint for the key pair being used.
Full path and filename of the private key.
The passphrase used for the key, if it is encrypted.

## Step 2 Update the Sample Application

1. Copy the HelloWorld.java application to an editor of your choice. You use this application to connect to Oracle NoSQL Database Cloud Service.
  You can access the [JavaAPI Reference Guide](https://docs.oracle.com/en/cloud/paas/nosql-cloud/csnjv/index.html) to reference Java classes, methods, and interfaces included in this sample application.

2. In the generateNoSQLHandle method, update the appropriate Oracle Cloud Infrastructure region.
3. In the generateNoSQLHandle method, update the parameters of the SignatureProvider constructor with the following values.
    - OCID of the tenancy.
    - OCID of the user calling the API.
    - Fingerprint for the key pair being used.
    - Full path and filename of the private key.
    - The passphrase used for the key, if it is encrypted. This should be a character array.
4. Save the application in your local system.

## Step 3 Execute the Sample Application

- Open the Command Prompt.
- Build the HelloWorld.java application as below:

    javac -cp oracle-nosql-java-sdk-X.X.X/lib/* HelloWorld.java

    Note: Update oracle-nosql-java-sdk-X.X.X with the Java driver version number that you have downloaded.

    For Example:
    javac -cp oracle-nosql-java-sdk-5.2.11/lib/* HelloWorld.java
    
- Execute the HelloWorld.java application :
   java -cp ".;oracle-nosql-java-sdk-X.X.X/lib/*" HelloWorld

    Note: Update oracle-nosql-java-sdk-X.X.X with the Java driver version number that you have downloaded.

   For Example:

   java -cp ".;oracle-nosql-java-sdk-5.2.11/lib/*" HelloWorld

   ##### Expected output:

   {"id":1,"content":{"hello":"world}}





    





