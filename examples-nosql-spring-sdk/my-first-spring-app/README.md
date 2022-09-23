# my-first-spring-app

## Preparation

Clone this repository

## Setup dependencies

see [pom.xml](./pom.xml)


## Build and Run the application 
**Note**: default values are for on-premise and `http://localhost:8080`. See application.properties 
These can be modified by setting the following env variables

export NOSQL_ENDPOINT=$HOSTNAME
export NOSQL_PORT=8080


Running the App code:
```shell
mvn compile exec:java -Dexec.mainClass="org.example.app.App" 
```

## Deploy KVLite container image and run the application

1. Start up KVLite in a container

pull the image directly from the GitHub Container Registry:

```shell
docker pull ghcr.io/oracle/nosql:latest-ce
docker tag ghcr.io/oracle/nosql:latest-ce oracle/nosql:ce
```

Start up KVLite in a container. You must give it a name and provide a hostname. Startup of
KVLite is the default `CMD` of the image:

```shell
docker run -d --name=kvlite --hostname=kvlite --env KV_PROXY_PORT=8080 -p 8080:8080 oracle/nosql:ce
```

see instuction https://github.com/oracle/docker-images/tree/main/NoSQL

2. Build and Run the application as shown above

## Learn more

* [Getting Started - Accessing Oracle NoSQL Database from Spring Data Applications](https://blogs.oracle.com/nosql/post/getting-started-accessing-oracle-nosql-database-from-spring-data-applications)

