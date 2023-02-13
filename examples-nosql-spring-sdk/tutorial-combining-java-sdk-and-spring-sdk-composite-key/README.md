# Preparation

Clone this repository

# Setup dependencies

see [pom.xml](./pom.xml)

# Review the code


# How to run the code
**Note**: `test.serverType` and `test.endpoint` default values are `onprem` and `http://localhost:8080`. These can be modified in AppConfigBase.java lines 43 and 44.

### Using maven
Running the App code:
```shell
mvn compile exec:java -Dexec.mainClass="com.example.App" -Dtest.serverType=onprem -Dtest.endpoint=http://localhost:8080 
```

Running the App code on the cloud:
```shell
mvn compile exec:java -Dexec.mainClass="com.example.App" -Dtest.serverType=cloud -Dtest.endpoint=us-ashburn-1 -Dtest.config=/home/opc/.oci/config -Dtest.compid=demonosql
```


Running the test code:
```shell
mvn test -Dtest.serverType=onprem -Dtest.endpoint=http://localhost:8080
```

### Directly using java
Running the App code directly using java command.
```shell
mvn clean package -Dmaven.test.skip
CP=`mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q`
java -cp $CP:./target/spring-data-test-1.0-SNAPSHOT.jar -Dtest.serverType=onprem -Dtest.endpoint=http://localhost:8080  com.example.App 
```

Running the App code directly using java command on the cloud.
```shell
mvn clean package -Dmaven.test.skip
CP=`mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q`
java -cp $CP:./target/spring-data-test-1.0-SNAPSHOT.jar -Dtest.serverType=cloud -Dtest.endpoint=us-ashburn-1 -Dtest.config=/home/opc/.oci/config -Dtest.compid=demonosql  com.example.App 
```
