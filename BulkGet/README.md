# OracleNoSQLBulkgGet

Table defination

PhoneTable (manufacturer string,
               price double , 
               …
               primary key(shard(manufacturer), price));


Steps to run the example:
1. Execute below command to create table and load sample records to store using CLI.
    exec "create table phones(manufacturer string, price double, name string, primary key(shard(manufacturer), price))"
    put table -name phones -json '{"manufacturer":"Samsung","price":199.991,"name":"s1"}'
    put table -name phones -json '{"manufacturer":"Samsung","price":200.1,"name":"s2"}'
    put table -name phones -json '{"manufacturer":"Samsung","price":499.991,"name":"s3"}'
    put table -name phones -json '{"manufacturer":"Samsung","price":500.001,"name":"s4"}'
    put table -name phones -json '{"manufacturer":"Apple","price":199.991,"name":"s1"}'
    put table -name phones -json '{"manufacturer":"Apple","price":200.1,"name":"s2"}'
    put table -name phones -json '{"manufacturer":"Apple","price":499.991,"name":"s3"}'
    put table -name phones -json '{"manufacturer":"Apple","price":500.001,"name":"s4"}'
    put table -name phones -json '{"manufacturer":"Nokia","price":199.991,"name":"s1"}'
    put table -name phones -json '{"manufacturer":"Nokia","price":200.1,"name":"s2"}'
    put table -name phones -json '{"manufacturer":"Nokia","price":499.991,"name":"s3"}'
    put table -name phones -json '{"manufacturer":"Nokia","price":500.001,"name":"s4"}'
    put table -name phones -json '{"manufacturer":"Motorola","price":199.991,"name":"s1"}'
    put table -name phones -json '{"manufacturer":"Motorola","price":200.1,"name":"s2"}'
    put table -name phones -json '{"manufacturer":"Motorola","price":499.991,"name":"s3"}'
    put table -name phones -json '{"manufacturer":"Motorola","price":500.001,"name":"s4"}'
    put table -name phones -json '{"manufacturer":"AAA","price":199.991,"name":"s1"}'
    put table -name phones -json '{"manufacturer":"BBB","price":200.1,"name":"s2"}'
    put table -name phones -json '{"manufacturer":"CCC","price":499.991,"name":"s3"}'
    put table -name phones -json '{"manufacturer":"DDD","price":500.001,"name":"s4"}'

2. Run example

    java -cp $KVHOME/lib/kvclient.jar:<path-to-example-class> bulk.BulkGetExample
