# REST API for Administering Oracle NoSQL Database.

The REST API for Administering Oracle NoSQL Database is configured when executing makebootconfig utility. 

`-admin-web-port <admin web service port>`
  
> The TCP/IP port on which the admin web service should be started. If not specified, the default port value is –1. 
> If a positive integer number is not specified for -admin-web-port, then admin web service does not start up along with the admin service. 
> See REST API for Administering Oracle NoSQL Database. 


All the admin commands presented in previous sections can executed via REST API calls. Ci-below some examples
  
Show topology
````
curl -i -X POST "http://node1-nosql:5999/V0/nosql/admin/topology" -d '{"command":"show"}'
curl -i -X POST "http://node2-nosql:5999/V0/nosql/admin/topology" -d '{"command":"show"}'
curl -i -X POST "http://node3-nosql:5999/V0/nosql/admin/topology" -d '{"command":"show"}'
````
Verify configuration
````
curl -i -X POST "http://node1-nosql:5999/V0/nosql/admin/configuration" -d '{"command":"verify"}'
````

  Ping
````
curl -i -X POST "http://node1-nosql:5999/V0/nosql/admin" -d '{"command":"ping"}'  
````
