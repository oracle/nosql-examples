This 15-minute tutorial walks you through the steps to connect to Oracle NoSQL Database Cloud Service and do basic table level operations using a sample application.

Background

Oracle NoSQL Database Cloud Service is a fully managed database cloud service that handles large amounts of data at high velocity. Developers can start using this service in minutes by following the simple steps outlined in this tutorial.

To get started with the service, you create a table.

After you sign up for an Oracle NoSQL Database Cloud Service account, you can easily create and populate a table by following these steps:

Generate required keys and get the Tenancy ID and User ID
Create a Java application with the required credentials
Execute the application to connect to Oracle NoSQL Database Cloud Service
What Do You Need?

Download and install Oracle NoSQL Java SDK from Oracle Cloud Downloads.
Access the Java API Reference Guide to reference Java driver packages, methods, and interfaces.
You can either create a free trial account or buy a paid subscription by navigating to oracle.com.

Step 1 Acquire the Credentials

Acquire the following credentials that are required for running this sample application. See Acquiring Credentials.
OCID of the tenancy.
OCID of the user calling the API.
Fingerprint for the key pair being used.
Full path and filename of the private key.
The passphrase used for the key, if it is encrypted.

Step 2 Update the Sample Application

1. Copy the HelloWorld.java application to an editor of your choice. You use this application to connect to Oracle NoSQL Database Cloud Service.
  You can access the JavaAPI Reference Guide to reference Java classes, methods, and interfaces included in this sample application.

2. In the generateNoSQLHandle method, update the appropriate Oracle Cloud Infrastructure region.
3. In the generateNoSQLHandle method, update the parameters of the SignatureProvider constructor with the following values.
OCID of the tenancy.
OCID of the user calling the API.
Fingerprint for the key pair being used.
Full path and filename of the private key.
The passphrase used for the key, if it is encrypted. This should be a character array.
4. Save the application in your local system.

