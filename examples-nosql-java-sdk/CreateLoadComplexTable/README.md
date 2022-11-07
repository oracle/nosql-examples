# Overview

The `CreateLoadComplexTable` example program creates a table in the Oracle NoSQL Database Cloud Service and then uses the [Oracle NoSQLDatabase Java SDK](https://docs.oracle.com/en-us/iaas/nosql-database/doc/oracle-nosql-database-java-sdk.html) to populate the table with sample records; where the table that is created consists of all the data types (primitive and complex) currently supported by the NoSQL Cloud Service. This example program is referenced in the [Oracle NoSQL Database Analytics Integrator User Guide](https://docs.oracle.com/en/cloud/paas/nosql-cloud/zsons/index.html#articletitle).
 
The tasks you can perform with this example are:

  - Modify and execute the source code that is provided.
  - Modify and run the unit tests that are provided.
  - Build a package for release that can then be installed and executed in the desired environment.

The program can be executed in one of the three environments listed below. How the program authenticates 
with the Oracle NoSQL Database Cloud Service depends on the environment, as well as how the program is configured (described below).

  - The OCI Command Shell; where authentication is performed using either an OCI Delegation Token or the user's private credentials.
  - An OCI Compute Instance; where authentication is performed using either an OCI Instance Principal or the user's private credentials.
  - The user's local (non-cloud) environment; where authentication is performed using the user's private credentials.

# Requirements

To build and/or execute the program, the following requirements must be satisfied.

## Java Version

  - Java 11 or higher.

## OCI Cloud Environment

  - An account in the Oracle Cloud Infrastructure; either a free trial or a paid subscription.

	https://docs.cloud.oracle.com/en-us/iaas/Content/GSG/Tasks/signingup.htm

	https://www.oracle.com/cloud/free     (to create a free account)

	https://docs.oracle.com/en-us/iaas/Content/GSG/Tasks/buysubscription.htm#upgrade_promotion   (to upgrade your free account to a paid account)

## Authentication
 
  1. If the program will be run from an OCI Compute Instance, and authentication with the Oracle NoSQL 
  Database Cloud Service will be performed using an OCI Instance Principal, then do the following:

   - [Create a Dynamic Group for the OCI Compute Instance](https://docs.oracle.com/en-us/iaas/Content/Identity/Tasks/managingdynamicgroups.htm)
   - [Create a Policy with Appropriate Permissions for the Dynamic Group](https://docs.oracle.com/en-us/iaas/Content/Identity/Tasks/callingservicesfrominstances.htm#Creating)
   
   The policy you create should allow the Dynamic Group you created to access tables and rows in the Oracle NoSQL Cloud Service. For example, the policy would look something like:
   ````
   Allow dynamic-group <dyn-grp-name> to manage nosql-tables in compartment <compartment-name>
   Allow dynamic-group <dyn-grp-name> to manage nosql-rows in compartment <compartment-name>
   ````
   				
   where the token `<dyn-grp-name>` should be replaced with the name of the Dynamic Group you created, and the token `<compartment-name>` 
   should be replaced with the name of the compartment in which the OCI Compute Instance was launched. 

  2. If the program will be run from the OCI Command Shell, an OCI Compute Instance, or your own local environment, and authentication 
  will be performed using your private credentials, then you need to acquire and install the following 
  items (see [Acquiring Credentials](https://docs.oracle.com/en-us/iaas/nosql-database/doc/acquiring-credentials.html)):

  - OCID of the tenancy.
  - OCID of the user calling the API (that is, the user who executes the program).
  - Fingerprint for the key pair being used.
  - Full path and filename of the private key.
  - If the private key is encrypted, the passphrase used when encrypting the key.

# Building the Example

To build the example program for distribution, simply the type following from the command line,

````bash
cd <base>/nosql-examples/examples-nosql-java-sdk/CreateLoadComplexTable 
mvn
````

By default, the command above executes the following goals: `mvn clean dependency:copy-dependencies package`


Specifically, executing the command above is equivalent to executing the command,

````
mvn clean dependency:copy-dependencies package
````

These goals will perform the following actions:

  - remove the target directory created and populated by the prior build
  - retrieve all specified dependencies and install them in your local Maven repository (for example, `~/.m2/repository`)
  - copy the retrieved dependencies under the `target/dependencies` directory
  - compile the source
  - execute the unit tests
  - generate the javadoc
  - generate the final release package, including all necessary artifacts

The final `complextable` release artifacts (.zip and tar.gz) are placed in the `./target` directory.

The `dependency:copy-dependencies` goal is executed by default to allow the developer to test the release against the real NoSQL Cloud Service. 
The dependencies that Maven installs in your local Maven repository are needed by Maven to automatically execute the unit tests. Whereas the dependencies 
that Maven copies to the `./target/dependencies` directory allow you to execute the newly built program manually, from your local command line. 
Thus, in addition to automatically running the unit tests, you can also test the program manually, by executing the final product from the target directory 
to create and populate a table in the Oracle NoSQL Cloud Service. To do this, you would execute a command like:

````bash
cd <base>/nosql-examples/examples-nosql-java-sdk/CreateLoadComplexTable
java -Djava.util.logging.config.file=./src/main/resources/logging/java-util-logging.properties \
     -Dlog4j.configurationFile=file:./src/main/resources/logging/log4j2-complextable.properties \
     -jar ./target/dependencies/complextable-<version>.jar -config ~/.oci/<your-local-environment-config.json>
````

For details on the configuration file that must be input to the program, see the section below titled, 'Configurating the Example'.

# Unit Testing the Release

As described above, the package component of the default goal will run all the unit tests automatically. The test output, including failure information, is logged to the directory, `./target/surefire-reports`

Rather than running all the unit tests, if you wish to change the default behavior with respect to how/what unit tests are run, the various test-related goals described below can be specified instead. 
Note that when the token `fqcn` is used below, that token represents the fully-qualified classname of a given test class (for example, `nosql.cloud.table.config.ComplexTableConfigurationTest`).

1. To run only the unit tests, but not build or package the release:  `mvn test`
2. To continue running the remaining tests after encountering failure in a given test:  `mvn -DfailIfNoTests=false test`
3. To run only one test class:  `mvn -Dtest=<fqcn> test`
4. To run a single test method from a given test class:  `mvn -Dtest=<fqcn>#<method1> test`
5. To run multiple test methods from a given test class:  `mvn -Dtest=<fqcn>#method1+method2[+ ...] test`
6. To pass one or more system properties to the JVM in which the tests run:  `mvn -DargLine="prop1=val1 prop2=val2" [...] test`

# Other Useful Build Tasks

1. To generate only the javadoc:  `mvn javadoc:javadoc`,   where the generated doc files will be placed in the directory,  `./target/apidocs`
2. To remove all dependencies from the local Maven repository (ex. `~/.m2/repository`):  `mvn dependency:purge-local-repository`

# Executing the Example

After building and unit testing the example, the release can be installed and the program can be exectued in the desired enviroment, which, as described previously, can be one of the following:

  - The OCI Command Shell
  - An OCI Compute Instance
  - Your local development environment

The release is packaged in both a ZIP file and a compressed TAR file; each located under the './target' directory. The names of these files take the form complextable-<version>.zip and complextable-<version>.tar.gz respectively. To install the example, copy either the ZIP or the TAR release artifact to a directory in the desired execution environment, and type:

````
unzip complextable-<version>.zip
````  
or
````  
tar xzvf complextable-<version>.tar.gz
````
Once installed, if you have satisfied the requirements described above for interacting with the Oracle Cloud Infrastructure, then the CreateLoadComplexTable example program can be executed by typing a command like the following (assuming the version is 1.0.0):

````bash
cd complextable-1.0.0/complextable
java -Djava.util.logging.config.file=./src/main/resources/logging/java-util-logging.properties \
     -Dlog4j.configurationFile=file:./src/main/resources/logging/log4j2-complextable.properties] \
     -jar ./lib/complextable-1.0.0.jar -config~/.oci/<your-complextable-config.json>
````

Note that the same command is used whether you are executing from the OCI Command Shell, an OCI Compute Instance, or your local development environment. For details on the configuration file that must be input to the program, see the next section titled, 'Configurating the Example'.

# Configuring the Example

In order to execute the CreateLoadComplexTable program, you must specify a value for the `-config` argument that references a json-formatted configuration file. 
The contents of that file consist of required and optional information needed by the program when accessing the Oracle NoSQL Cloud Service. 
For example, if you want the program to use your private credentials when authenticating with the Oracle NoSQL Cloud Service, then the configuration 
file would look something like the example configuration file shown below, where the values shown are examples that you will need to replace 
with the actual values appropriate for your environment:

````
{
    "credentialsType" : "file",
    "region" : "us-sanjose-1",
    "compartment" : "<ocid.of.compartment.containing.table>", 
    "table" : "<table-name>",
    "delete" : true,
    "nRows" : "37",
    "readUnits" : "50",
    "writeUnits" : "50",
    "storageGb" : "25",
    "ttlDays" : "1",
    "credentialsProfile" : "nosql-tables",
    "credentialsFile" : "/home/opc/.oci/config.complex-table-file"
}
````

## Required Configuration Entries

  In order to execute the CreateLoadComplexTable program, the following configuration entries are required in all environments, no mater which authentication mechanism is employed:

  - credentialsType
  - region
  - compartment
  - table

  1. credentialsType

  The value of the `credentialsType` entry must be `token`, `principal`, or `file`; otherwise, an error occurs. 

  a. If the value of the `credentialsType` entry is `token`, then the program assumes it is being executed from the OCI Command Shell, and will authenticate with the Oracle NoSQL Database Cloud Service 
  using the Instance Principal Delagation Token that is built-in to the OCI Command Shell. 

  b. If the value of the `credentialsType` entry is `principal`, then the program assumes it is being executed from an OCI Compute Instance belonging to a 
  dynamic group with permission to manage tables and rows in the Oracle NoSQL Database Cloud Service. As a result, the program will authenticate with Oracle 
  NoSQL Database Cloud Service using the Instance Principal. 

  c. If the value of the `credentialsType` entry is `file`, then the program assumes the credentials referenced by the value of the `credentialsFile` entry should be used to authenticate 
  with the Oracle NoSQL Database Cloud Service. Thus, the `credentialsFile` entry is required if and only if the value of the `credentialsType` entry is `file`. 
  In this case, the file specified by the `credentialsFile` entry must exist and have contents like that shown below, where the values shown are examples that you will need to replace 
  with the actual values appropriate for your environment:

````
[DEFAULT]
tenancy=<ocid.of.default.user.tenancy>
user=<ocid.of.default.user>
fingerprint=<fingerprint:of:default:user>
key_file=</path/to/default/user/oci/api/privatekey.pem>
passphrase=<optional-passphrase-used-to-encrypt-default-user-key>

[nosql-tables]
tenancy=<ocid.of.nosqldb.user.tenancy>
user=<ocid.of.nosqldb.user>
fingerprint=<fingerprint:of:nosqldb:user>
key_file=</path/to/nosqldb/user/oci/api/privatekey.pem>
passphrase=<optional-passphrase-used-to-encrypt-nosqldb-user-key>
````

  For a credentials file like that shown above, the file must specify at least the `[DEFAULT]` profile; all other profiles are optional. Under any given profile (ex. `[DEFAULT]`, `[nosql-tables]`, etc.), 
  the profile must specify the entries named, `tenancy`, `user`, `fingerprint`, and `key_file`; otherwise, an error will occur when one of those entries is not specified under the given profile. 
  The entry named `passphrase` is required under a profile only when the key referenced by the associated `key_file` entry is encrypted.

  2. region

  The value of the `region` entry specifies the name of the OCI region in which to create the desired table; and must be one of the following names of existing regions in the Oracle Cloud Infrastructure:

````
  af-johannesburg-1
  ap-chuncheon-1
  ap-hyderabad-1
  ap-melbourne-1
  ap-mumbai-1
  ap-osaka-1
  ap-singapore-1
  ap-seoul-1
  ap-sydney-1
  ap-tokyo-1
  uk-cardiff-1
  uk-london-1
  eu-amsterdam-1
  eu-frankfurt-1
  eu-madrid-1
  eu-marseille-1
  eu-milan-1
  eu-paris-1
  eu-stockholm-1
  eu-zurich-1
  me-abudhabi-1
  me-dubai-1
  me-jeddah-1
  mx-queretaro-1
  il-jerusalem-1
  us-ashburn-1
  us-phoenix-1
  us-sanjose-1
  ca-montreal-1
  ca-toronto-1
  sa-saopaulo-1
  sa-santiago-1
  sa-vinhedo-1
````

  3. compartment

  The value of the `compartment` entry specifies the OCID of the compartment in which to create the desired table.

  4. table

  The value of the `table` entry specifies the name of the table to create. The value specified can be any valid table name, as defined by the Oracle NoSQL Database Cloud Service; that is, the name

  - Must be no longer than 256 characters
  - Contain only alphanumeric characters
  - Can contain the underscore character (`_`) but not the dash character (`-`) 

## Optional Configuration Entries

  The following items are optional in the configuration. When not specified in the configuration, each entry will be set to an appropriate default value, as indicated.

  1. `delete` (default = "true")

  If "true" is specified for the "delete" configuration entry, then all existing rows written to the table by previous executions of the program will first be deleted from the table before adding any new rows. Otherwise, the table will contain both the old rows and the new rows that are generated.

  2. `nRows` (default = "10")

  Specifies the number of new rows to generate and write to the table. If the entry is not specified, then the default number of rows will be written to the table. 

  3. `readUnits` (default = "50")

  Specifies the number of Oracle NoSQL Database Cloud Service read units to allocate to the table being created. If the entry is not specified, then the default number of read units will be allocated to the table. 

  4. `writeUnits` (default = "50")

  Specifies the number of Oracle NoSQL Database Cloud Service write units to allocate to the table being created. If the entry is not specified, then the default number of write units will be allocated to the table. 

  5. `storageGb` (default = "25")

  Specifies the maximum storage space (in giga-bytes) to allocate to the table being created. If the entry is not specified, then the default maximum storage space will be allocated to the table. 

  6. `ttlDays` (default = "1")

  Specifies the time-to-live (ttl, in number of days) to associate with the table that is created. For example, if "3" is specified as the value for the `ttlDays` entry, then any row that has been in the table for at least 3 days will be automatically removed from the table. If this entry is not specified, or if the value specified for the `ttlDays` entry is invalid (negative or non-numeric), then the default number of days will be used when creating the table.
  
  Note that if you wish to disable the ttl mechanism for the table so that the table's rows are never purged, then specify "0" for the value of the `tlDays` entry.

  7. `credentialsProfile` (default = "DEFAULT")

  Specifies the profile in the credentials file from which to retrieve the credentials to use when authenticating with the Oracle NoSQL Database Cloud Service. If this entry is not specified, then the default profile will be used.

## Overriding Configuration Entries

  For convenience, each entry in the configuration file can be overridden on the command line by setting a system property with name equal to the name of the configuration entry. For example, suppose your environment is the OCI Command Shell and you have specified the configuration file `~/.oci/my-config.json` with contents like, 
````
{
    "credentialsType" : "token",
    "region" : "us-sanjose-1",
    "compartment" : "<ocid.of.compartment.containing.table>", 
    "table" : "my_table_1",
    "readUnits" : "50",
    "writeUnits" : "50",
    "storageGb" : "25"
}
````
  Specifying the configuration above when executing the following command will cause the program to authenticate using the OCI Command Shell delegation token when creating and populating a table named `my_table_1`; where the table will have 10 rows (the default), and will be allocated 50 read units, 50 write units, and maximum storage space of 25 Gbs.

````
java -jar ./lib/complextable-1.0.0.jar -config ~/.oci/my-config.json
````

  Next, suppose you wish to create a new table named "my_table_2" with 51 rows. And suppose you wish to allocate to that new table, 10 read units, 15 write units, and 3 Gbs maximum storage. To do this, you can create a new configuration file with new values, or you can use the configuration file above, but override the deired configuration entries on the command line. For example,

````
java -Dtable=my_table_2 -DreadUnits=10 -DwriteUnits=15 -DstorageGb=3 \
     -jar ./lib/complextable-1.0.0.jar -config ~/.oci/my-config.json
````

  This feature may be useful when testing or writing scripts that run the utility at regular intervals; using a single configuration file 'template', but overriding the entries you wish to manipulate. 

## Configuring Logging

  The system properties that configure the loggers used during execution are optional. If those system properties are not specified, then the utility will produce no logging output.

  The CreateLoadComplexTable example program executes software from different third-party libraries, where each library defines its own set of loggers with different namespaces. For convenience, this example provides two logging configuration files as part of the release; one to configure logging mechanisms based on java.util.logging, and one for loggers based on Log4j2.

  By default, the logger configuration files provided with the utility are designed to produce minimal output as the program executes. If you wish to see more verbose output from the various components employed during execution, then you should increase the logging levels of the specific loggers referenced in the appropriate logging configuration file.

