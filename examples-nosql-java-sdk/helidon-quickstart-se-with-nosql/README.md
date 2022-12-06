# Helidon Quickstart SE

Sample Helidon SE project that includes multiple REST operations.

## Build and run

With JDK11+
```bash

# modify src/main/resources/application.yaml

cat src/main/resources/application.yaml

app:
  greeting: "Hello"

server:
  port: 8080
  host: 0.0.0.0

nosql:
  OCI_CLI_AUTH: instance_principal
  region: us-ashburn-1
  compartment-id: ocid1.compartment.oc1..aaaaaaaa4mlehopmvdluv2wjcdp4tnh2ypjz3nhhpahb4ss7yvxaa3be3diq

 
mvn package
java -jar target/helidon-quickstart-se.jar
```

## Exercise the application


```
curl -X PUT -H "Content-Type: application/json" -d '{"id":1, "message" : "Hola"}' http://localhost:8080/greet/greeting
curl -X PUT -H "Content-Type: application/json" -d '{"id":2, "message" : "Bonjour"}' http://localhost:8080/greet/greeting
curl -X PUT -H "Content-Type: application/json" -d '{"id":3, "message" : "Hello"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet
[
  {
    "id": 3,
    "message": "Helll"
  },
  {
    "id": 2,
    "message": "Bonjour"
  },
  {
    "id": 1,
    "message": "Hola"
  }
]


curl -X GET http://localhost:8080/greet/1
{"id":1,"message":"Hola"}

curl -X GET http://localhost:8080/greet/2
{"id":2,"message":"Bonjour"}

curl -X GET http://localhost:8080/greet/3
{"id":3,"message":"Hello"}

```

## Try health and metrics

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...
. . .

# Prometheus Format
curl -s -X GET http://localhost:8080/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics
{"base":...
. . .

```

## Build the Docker Image

```
docker build -t helidon-quickstart-se .
```

## Start the application with Docker

```
docker run --rm -p 8080:8080 helidon-quickstart-se:latest
```

Exercise the application as described above

## Deploy the application to Kubernetes

```
kubectl cluster-info                        # Verify which cluster
kubectl get pods                            # Verify connectivity to cluster
kubectl create -f app.yaml                  # Deploy application
kubectl get pods                            # Wait for quickstart pod to be RUNNING
kubectl get service helidon-quickstart-se   # Get service info
```

Note the PORTs. You can now exercise the application as you did before but use the second
port number (the NodePort) instead of 8080.

After you’re done, cleanup.

```
kubectl delete -f app.yaml
```

## Build a native image with GraalVM

GraalVM allows you to compile your programs ahead-of-time into a native
 executable. See https://www.graalvm.org/docs/reference-manual/aot-compilation/
 for more information.

You can build a native executable in 2 different ways:
* With a local installation of GraalVM
* Using Docker

### Local build

Download Graal VM at https://www.graalvm.org/downloads, the versions
 currently supported for Helidon are `20.1.0` and above.

```
# Setup the environment
export GRAALVM_HOME=/path
# build the native executable
mvn package -Pnative-image
```

You can also put the Graal VM `bin` directory in your PATH, or pass
 `-DgraalVMHome=/path` to the Maven command.

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin#goal-native-image
 for more information.

Start the application:

```
./target/helidon-quickstart-se
```

### Multi-stage Docker build

Build the "native" Docker Image

```
docker build -t helidon-quickstart-se-native -f Dockerfile.native .
```

Start the application:

```
docker run --rm -p 8080:8080 helidon-quickstart-se-native:latest
```

## Build a Java Runtime Image using jlink

You can build a custom Java Runtime Image (JRI) containing the application jars and the JDK modules
on which they depend. This image also:

* Enables Class Data Sharing by default to reduce startup time.
* Contains a customized `start` script to simplify CDS usage and support debug and test modes.

You can build a custom JRI in two different ways:
* Local
* Using Docker


### Local build

```
# build the JRI
mvn package -Pjlink-image
```

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin#goal-jlink-image
 for more information.

Start the application:

```
./target/helidon-quickstart-se-jri/bin/start
```

### Multi-stage Docker build

Build the JRI as a Docker Image

```
docker build -t helidon-quickstart-se-jri -f Dockerfile.jlink .
```

Start the application:

```
docker run --rm -p 8080:8080 helidon-quickstart-se-jri:latest
```

See the start script help:

```
docker run --rm helidon-quickstart-se-jri:latest --help
```
