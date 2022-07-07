## Step 1. Create a compartment -  

Go to OCI console -> Identity & Security -> Compartments.

Click on Create Compartment. This opens up a new window.

Choose **demonosql** as compartment name, choose a description and add it.

## Step 2. Create an API Key and Auth Token for your user

Login to OCI console.

Click on your Profile -> User Settings. On the bottom left, click on Auth Tokens. Click on Generate Token.

Provide a description and then hit Generate Token. This will generate a token. **Make sure to copy the token and save it for future steps**.

## Step 3. Create a VCN with Internet Connectivity (VCN Wizard) - vcn_nosql_demos

Go to OCI console -> Networking -> Virtual Cloud Networks.

On the left List Scope - Compartment - choose demonosql compartment

Click on Start VCN Wizard. This opens up a new window. Choose Create VCN with Internet Connectivity and start the Wizard

Choose **vcn_nosql_demos** as VCN Name, click next and Create it.

## Step 4. Allows TCP traffic for ports: 443 HTTPS 

On the left Click on Security Lists, Click on **Default Security List for vcn_nosql_demos** and then **Ingress Rules** This opens up a new window

Choose **0.0.0.0/0** as Source CIDR, then **TCP** as IP Protocol, then **443** as Destination Port Range and add it

## Step 5. Create the dynamic group and the policies 

guidelines are provided in  [./privs/dynamic-group](./privs/dynamic-group) directory

## Step 6. Create the NoSQL Tables -

guidelines are provided in [demo-lab-baggage deployment steps](README.md#demo-lab-baggage-deployment-steps) section of README

## Step 7. Create the Function Application - nosql_demos

Go to OCI console -> Developer Services -> Applications .

On the left List Scope - Compartment - choose demonosql compartment

Click on Create Application. This opens up a new window.

Choose **nosql_demos** as Name, Choose the VCN created in step 3, Choose the **public** subnet and Create it.

## Step 8. Configure Cloud Shell 

follow the Instructions in the OCI Console - Getting Started for this application

## Step 9. Deploy and test the functions in this GitHub repository

guidelines are provided in [demo-lab-baggage deployment steps](README.md#demo-lab-baggage-deployment-steps) section of README

## Step 10. Create the Stream

Go to OCI console -> Analytics & AI -> Streaming .

On the left List Scope - Compartment - choose demonosql compartment

Click on Create Stream. This opens up a new window.

Choose **nosql_demos** as Name, Choose the **default** stream pool and Create it.

## Step 11. Create and configure the Service Connector - nosql_demos

Go to OCI console -> Analytics & AI -> Service Connector Hub .

On the left List Scope - Compartment - choose demonosql compartment

Click on Create Service Connector. This opens up a new window.

Choose **nosql_demos** as Connector Name, provide a description 

Choose streaming as a source and functions as a target 

On the Configure Source panel, choose **default** as Stream Pool and **nosql_demos** as Stream

On the Configure Target panel, choose **nosql_demos** as Function Application and **load-target** as Function

Create default policy allowing this service connector to read from Streaming in compartment demonosql.

Create default policy allowing this service connector to write to Functions in compartment demonosql.

and Create it.


## Step 12. Create and configure the API Gateway - nosql_demos - /BaggageDemo/{api}

Go to OCI console -> Developer Services -> API Gateway.

On the left List Scope - Compartment - choose demonosql compartment

Click on Create Gateway. This opens up a new window.

Choose **nosql_demos** as Name, Choose the VCN created in step 3, Choose the **public** subnet and Create it.

On the left choose Deployments

Click on Create Deployment. This opens up a new window. Use the From Scratch wizard

Choose **BaggageDemo** as Name, Choose **/BaggageDemo** as path prefix and click Next

Choose **/{api}** as a Path, Choose GET as a Method, Choose Oracle Functions as Type, Choose **nosql_demos** as Application and then **demo-api** as a Function Name

Click on Next and Create it.


## Step 13. Update the policies with the good ocid- if needed

if you created the default policy allowing this service connector, you don't need modify the policies created in step 5


## Step 14. Execute the API and Streaming tests

guidelines are provided in the README:

1. [DEMO-Service-Connector](./README.md#DEMO-Service-Connector) 
2. [DEMO-API-Gateway](./README.md#DEMO-API-Gateway) 

