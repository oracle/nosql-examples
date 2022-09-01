# demo-tv-streaming-app
Demo Video On Demand streaming application using GraphQL and NoSQL

## Deployment using Docker
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

2. Deploy this application

````shell
docker pull ghcr.io/dario-vega/demo-vod-streaming-app:latest
docker tag ghcr.io/dario-vega/demo-vod-streaming-app:latest demo-vod-streaming-app:latest
````

Start up this demo in a container 

````shell
docker run -d --env NOSQL_ENDPOINT=$HOSTNAME -p 3000:3000 demo-tv-streaming-app:latest 
````
or use user-defined bridge network name

````shell
docker run -d --link kvlite --env NOSQL_ENDPOINT=kvlite  -p 3000:3000 demo-vod-streaming-app:latest
````


This project offers sample container image to show how to connect a NoSQL application to Oracle NoSQL Database Proxy running in a container

The default values for the env variables are
````
ENV NOSQL_ENDPOINT kvlite
ENV NOSQL_PORT 8080
````

## Deployment using docker-compose

1. Clone this project and run the up docker-compose command

````shell
cd ~/demo-tv-streaming-app
docker-compose up -d
docker-compose ps
````

Note: https://docs.docker.com/compose/startup-order/

````shell
cd ~/demo-tv-streaming-app
docker-compose start
````


## Deployment on a external host connected to KVLite runnning in a container

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

2. Clone this project and startup the application 

````shell
cd ~/demo-tv-streaming-app/demo-vod
npm install 
export NOSQL_ENDPOINT=$HOSTNAME
export NOSQL_PORT=8080
npm start
````


## Load some test data to KVLite runnning in a container

  
````shell
cd ~/demo-tv-streaming-app/demo-vod
docker cp insert-stream-acct.sql kvlite:insert-stream-acct.sql
docker exec kvlite  java -jar lib/sql.jar -helper-hosts localhost:5000 \
-store kvstore load -file /insert-stream-acct.sql
````
*Note*: if you are using docker compose, use `docker ps` to obtain the name of the container e.g.`demo-tv-streaming-app_demo-vod-streaming-db_1` 


read  https://github.com/oracle/docker-images/tree/main/NoSQL#using-oracle-nosql-command-line-from-an-external-host 
if you want to run those commands from your host

## Run some GraphQL queries

````shell
curl --request POST     --header 'content-type: application/json' --url 'localhost:3000' \
--data '{"query":"query Streams { streams { id  info { firstName  lastName country } }}"}' | jq
````
````
{
  "data": {
    "streams": [
      {
        "id": 1,
        "info": {
          "firstName": "John",
          "lastName": "Sanders",
          "country": "USA"
        }
      },
      {
        "id": 3,
        "info": {
          "firstName": "Aniketh",
          "lastName": "Shubham",
          "country": "India"
        }
      },
      {
        "id": 2,
        "info": {
          "firstName": "Tim",
          "lastName": "Greenberg",
          "country": "USA"
        }
      }
    ]
  }
}
````


````shell
curl --request POST \
    --header 'content-type: application/json' \
    --url 'localhost:3000' \
    --data '{"query":"query WatchTime { watchTime { showName seasonNum length } } "}'
````
````


{
  "data": {
    "watchTime": [
      {
        "showName": "Apprentice",
        "seasonNum": 1,
        "length": 82
      },
      {
        "showName": "Apprentice",
        "seasonNum": 2,
        "length": 96
      },
      {
        "showName": "Rita",
        "seasonNum": 1,
        "length": 125
      },
      {
        "showName": "Mr.Chef",
        "seasonNum": 1,
        "length": 125
      },
      {
        "showName": "Mystery unfolded",
        "seasonNum": 1,
        "length": 125
      },
      {
        "showName": "Call My Agent",
        "seasonNum": 1,
        "length": 158
      },
      {
        "showName": "Call My Agent",
        "seasonNum": 2,
        "length": 192
      }
    ]
  }
}
````

more queries below


## TEST using https://studio.apollographql.com/sandbox 

[QUERIES.md](./QUERIES.md)
