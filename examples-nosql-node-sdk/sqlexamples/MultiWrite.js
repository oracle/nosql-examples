/* Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */
'use strict';

const NoSQLClient = require('oracle-nosqldb').NoSQLClient;

/**
  * Call the main function for this example
  **/
domultiwrite();

async function domultiwrite() {
   let handle;
   try {
      /* UNCOMMENT line of code below if you are using Oracle NoSQL Database
      * Cloud service. Leave the line commented if you are using onPrem database
      handle = await getConnection_cloud(); */
      /* UNCOMMENT line of code below if you are using onPremise Oracle NoSQL
       * Database. Leave the line commented if you are using NoSQL Database
       * Cloud Service
      handle = await getConnection_onPrem(); */
      const table_name = 'ticket';
      const regtbl_DDL = `CREATE TABLE IF NOT EXISTS ${table_name} (ticketNo LONG,
                                                                   confNo STRING,
                                                                   primary key(ticketNo))`;
      const childtable_name = 'ticket.bagInfo';
      const childtbl_DDL = `CREATE TABLE IF NOT EXISTS ${childtable_name} (id LONG,
                                                                      tagNum LONG,
                                                                      routing STRING,
                                                                      lastActionCode STRING,
                                                                      lastActionDesc STRING,
                                                                      lastSeenStation STRING,
                                                                      lastSeenTimeGmt TIMESTAMP(4),
                                                                      bagArrivalDate TIMESTAMP(4),
                                                                      PRIMARY KEY(id))`;
      const desctable_name = 'ticket.bagInfo.flightLegs';
      const desctbl_DDL =  `CREATE TABLE IF NOT EXISTS ${desctable_name} (flightDate TIMESTAMP(4),
                                                                          fltRouteSrc STRING,
                                                                          fltRouteDest STRING,
                                                                          estimatedArrival TIMESTAMP(4),
                                                                          actions JSON,
                                                                          PRIMARY KEY(flightNo))`;
      const data1=`{
   	   "ticketNo":"1762344493810",
   		"id":"79039899165297",
   		"flightNo":"BM604",
   		"flightDate":"2019-02-01T06:00:00Z",
   		"fltRouteSrc":"MIA",
   		"fltRouteDest":"LAX",
   		"estimatedArrival":"2019-02-01T11:00:00Z",
   		"actions":[ {
   		   "actionAt" : "MIA",
   			"actionCode" : "ONLOAD to LAX",
   			"actionTime" : "2019-02-01T06:13:00Z"
   		 }, {
   		   "actionAt" : "MIA",
   			"actionCode" : "BagTag Scan at MIA",
   			"actionTime" : "2019-02-01T05:47:00Z"
   		 }, {
   		   "actionAt" : "MIA",
   		   "actionCode" : "Checkin at MIA",
   		   "actionTime" : "2019-02-01T04:38:00Z"
   		  } ]
   	 }`

      await createTable(handle,regtbl_DDL,'true',table_name);
      await createTable(handle,childtbl_DDL,'false',childtable_name);
      await createTable(handle,desctbl_DDL,'false',desctable_name);
      const ops = [
               {
                  tableName: 'ticket',
                  put: {
                     "ticketNo": "1762344493810",
                     "confNo" : "LE6J4Z"
                  },
                  abortOnFail: true
               },
               {
                  tableName: 'ticket.bagInfo',
                  put: {
                     "ticketNo":"1762344493810",
                     "id":"79039899165297",
                     "tagNum":"17657806255240",
                     "routing":"MIA/LAX/MEL",
                     "lastActionCode":"OFFLOAD",
                     "lastActionDesc":"OFFLOAD",
                     "lastSeenStation":"MEL",
                     "lastSeenTimeGmt":"2019-02-01T16:13:00Z",
                     "bagArrivalDate":"2019-02-01T16:13:00Z"
                   },
                  abortOnFail: true
               }
            ];
      const res = await handle.writeMany(ops, null);
      console.log("Wrote records in ticket and bagInfo table");
      let putResult = await handle.put(desctable_name, JSON.parse(data1));
      console.log("Wrote records in flightlegs table");
   } catch (error ) {
      console.log(error);
      process.exit(-1);
   }
}
/* Create and return an instance of a NoSQLCLient object for cloud service */
function getConnection_cloud() {
   /* replace the placeholders for compartment and region with actual values.*/
   const Region = `<your_region_identifier>`;
   return new NoSQLClient({
      region: Region,
      compartment: "<ocid_of_your_compartment>",
   });
}
/* Create and return an instance of a NoSQLCLient object for onPremises */
function getConnection_onPrem() {
   /* replace the placeholder with the fullname of your host */
   const kvstore_endpoint = `http://<hostname>:8080`;
   return new NoSQLClient({
      serviceType: "KVSTORE",
      endpoint: kvstore_endpoint
   });
   /* if it is a secure store, comment the return statement above and
   * uncomment the lines below and pass the credentials, username and password
   return new NoSQLClient({
      "serviceType": "KVSTORE",
      "endpoint": "",
      "auth": {
         "kvstore":{
            "user": "",
            "password": ""
         }
      }
   }); */
}

//creates a table
async function createTable(handle, query_stmt, reg_table, table_name) {
   let res;
   if(reg_table=='true'){
      res =  await handle.tableDDL(query_stmt, {
            complete: true,
            tableLimits: {
               readUnits: 20,
               writeUnits: 20,
               storageGB: 1
            }
      });
   } else{
      res =  await handle.tableDDL(query_stmt, {
            complete: true
      });
   }
   console.log('Table created: ' + table_name);
}
