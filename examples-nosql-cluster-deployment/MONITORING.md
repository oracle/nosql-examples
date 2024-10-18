## Monitoring your Oracle NoSQL Cluster

Being a distributed system, the Oracle NoSQL Database is composed of several software components and each expose unique metrics that can be monitored, interpreted, and
utilized to understand the general health, performance, and operational capability of the Oracle NoSQL Database cluster
 
There are three basic mechanisms for monitoring the health of the Oracle NoSQL Database: 
*	System Log File Monitoring
*	System Monitoring Agents - Integration with JMX based monitoring solutions. It is also possible to do an integration with central Information Event Management system or Application Performance Management tools (eg system based on ELK).
*	**Application Monitoring**
Good proxy for the “health” of the Oracle NoSQL Database rests with application level metrics. 
Metrics like average and 90th percentile response times, average and 90th percentile throughput, as well average number of timeout exceptions encountered from NoSQL API
calls are all potential indicators that something may be wrong with a component in the NoSQL cluster. 


1. **System Log File Monitoring**

The Oracle NoSQL Database is composed of the multiples components, and each component produces log files that can be monitored

```
$ cd $KVROOT/$KVSTORE/log
$ ls -1  | grep -v lck
admin1_0.log
admin1.je.config.csv
admin1.je.info.0
admin1.je.stat.csv
config.rg1-rn1
config.rg2-rn1
config.rg3-rn1
OUG_0.log
OUG_0.perf
OUG_0.stat
OUG_1.stat
OUG_2.stat
rg1-rn1_0.log
rg1-rn1.gc.0.current
rg1-rn1.je.config.csv
rg1-rn1.je.info.0
rg1-rn1.je.stat.csv
rg2-rn1_0.log
rg2-rn1.gc.0.current
rg2-rn1.je.config.csv
rg2-rn1.je.info.0
rg2-rn1.je.stat.csv
rg3-rn1_0.log
rg3-rn1.gc.0.current
rg3-rn1.je.config.csv
rg3-rn1.je.info.0
rg3-rn1.je.stat.csv
sn1_0.log

```
Oracle NoSQL Database automatically captures Replication Node performance statistics into a log file 
that you can into into spreadsheet software for analysis.
The store tracks, logs, and writes statistics at a user specified interval to a CSVfile. 
The file is je.stat.csv, located in the Environment directory.

```
cat rg*-rn*.je.stat.csv | awk -F',' '{print $168 "," $169 "," $170 "," $171}'  | grep -v " , , ,"
cat rg*-rn*.je.stat.csv | awk -F',' '{print $83 "," $169 "," $170 "," $171}'  | grep -v " , , ,"


```
the defaut directory is  $KVROOT/$KVSTORE/log but look the configuration in case of customization
```
kv-> show parameter -service sn1

RN Log directories:
    path=/nosql/oracle/product/rnlog1
    path=/nosql/oracle/product/rnlog2
    path=/nosql/oracle/product/rnlog3
Admin directory:
    path=/nosql/oracle/product/admin size=2024-MB 
```
2. **System Monitoring Agents**
Oracle NoSQL Database is also monitored through JMX based system management tools. For JMX based tools, the Oracle NoSQL MIB is found in lib directory of the installation 
along with the JAR files for the product. For more information on JMX, see the documentation. 

Show the JMX configuration for a specific Storage Node (NB. Remember, this is a distributed system, you need validate each storage Node)

```
kv-> show parameter -service sn1
mgmtClass=oracle.kv.impl.mgmt.NoOpAgent
```
To enable JMX:

```
plan change-parameters -service sn1 -wait -params "mgmtClass=oracle.kv.impl.mgmt.jmx.JmxAgent"
plan change-parameters -service sn2 -wait -params "mgmtClass=oracle.kv.impl.mgmt.jmx.JmxAgent"
plan change-parameters -service sn3 -wait -params "mgmtClass=oracle.kv.impl.mgmt.jmx.JmxAgent"
```
To disable JMX:
```
plan change-parameters -service sn1 -wait -params "mgmtClass=oracle.kv.impl.mgmt.NoOpAgent"
plan change-parameters -service sn2 -wait -params "mgmtClass=oracle.kv.impl.mgmt.NoOpAgent"
plan change-parameters -service sn3 -wait -params "mgmtClass=oracle.kv.impl.mgmt.NoOpAgent"
```
You can validate using jconsole. (NB. Remember, this is a distributed system, you need validate each storage Node)
```
jconsole localhost:5000
```

**Enabling the Collector Service**

Elasticsearch is a search and analytics engine. Logstash is a server-side data processing pipeline that ingests data from multiple sources simultaneously, transforms it,
and then send it to a “stash” like Elasticsearch. Kibana lets users visualize data with charts and graphs in Elasticsearch. The ELK stack can be used to monitor Oracle 
NoSQL Database.

Not all central Information Event Management system or Application Performance Management tools are reading JMX. In this case, we can Enable the Collector Service, in order
to create JSON files with all the metrics available.

```
kv-> show parameter  -global
collectorEnabled=false
collectorInterval=20 SECONDS
collectorRecorder=null
collectorStoragePerComponent=30 MB
storeName=OUG
```
Enabling the Collector Service
```
kv-> plan change-parameter -global -wait -params collectorEnabled=true
```
Now, you can see the json files created at this location with all information polled to JMX 

```
$ cd $KVROOT/$KVSTORE/sn1/collector
$ ls -1
loggingStats_0.json
ping_0.json
plan_0.json
rnEndpointGroup_0.json
rnEnv_0.json
rnEvent_0.json
rnException_0.json
rnJVM_0.json
rnOp_0.json
rnTable_0.json
```

You can then Set Up a tool like Filebeat on Each Storage Node to send the JSON files to your favorite APM System (e.g ELK setup)

e.g. (https://docs.oracle.com/pls/topic/lookup?ctx=en/database/other-databases/nosql-database/24.3/nsmon&id=filebeat-yml)

For ELK, Oracle Nosql is providing all the configuration templates to setup the metrics and do analysis using Kibana.

https://docs.oracle.com/en/database/other-databases/nosql-database/24.3/nsmon/using-elk-monitor-oracle-nosql-database.html

If you use the template files provided above, then the following indexes are available:
    kvrnjvmstats-*
    kvrnenvstats-*
    kvpingstats-*
    kvrnopstats-*
