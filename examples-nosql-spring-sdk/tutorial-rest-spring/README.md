# tutorial-rest-spring

## Building REST services with Spring

REST has quickly become the de-facto standard for building web services on the web because they’re easy to build and easy to consume.

There’s a much larger discussion to be had about how REST fits in the world of microservices, but — for this tutorial — let’s just look at building RESTful services.

This tutorial is based on existing tutorial using Spring Boot and the following dependencies: Web, JPA, H2

This tutorial will focus on how you can use Oracle NoSQL Spring Data for a flexible and elastic database that scales with your application. 
We start with the spring boot's standard tutorial - the in memory H2 database, and them move over to Oracle NoSQL Cloud Services. 
The application will be built using maven and can be deployed locally using docker-compose or to the Oracle Kubernetes Engine (OKE) cluster for elastic scalability.

## Test
Spring Initializr uses maven wrapper so type this:
```` shell
$ ./mvnw clean spring-boot:run
````

Alternatively using your installed maven version type this:
```` shell
$ mvn clean spring-boot:run
````

When the app starts, You can immediately interrogate it.
```` shell
$ curl -v localhost:8080/employees
````
Then you can try to query an employee that doesn’t exist…
```` shell
$ curl -v localhost:8080/employees/99
````
You can create a new Employee
```` shell
$ curl -X POST localhost:8080/employees -H 'Content-type:application/json' -d '{"name": "Samwise Gamgee", "role": "gardener"}'
````
You can update the employee.
```` shell
$ curl -X PUT localhost:8080/employees/3 -H 'Content-type:application/json' -d '{"name": "Samwise Gamgee", "role": "ring bearer"}'
````
Or, you can delete an employee:
```` shell
$ curl -X DELETE localhost:8080/employees/3
````



