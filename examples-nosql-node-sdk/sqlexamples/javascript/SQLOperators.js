/* Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */
'use strict';

const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const Region = require('oracle-nosqldb').Region;
const TABLE_NAME = 'BaggageInfo';
const seqcomp_stmt = `SELECT bag.bagInfo[].tagNum,bag.bagInfo[].flightLegs[].fltRouteSrc
                      FROM BaggageInfo bag WHERE bag.bagInfo[].flightLegs[].fltRouteSrc=any "SFO"`
const logope_stmt = `SELECT fullName, bag.bagInfo[].tagNum, bag.bagInfo[].routing,
                     bag.bagInfo[].flightLegs[].fltRouteSrc
                     FROM BaggageInfo bag
                     WHERE NOT bag.bagInfo[].flightLegs[].fltRouteSrc=any "SFO"`
const valcomp_stmt = `SELECT fullname, bag.bagInfo[].routing FROM BaggageInfo bag WHERE gender="M"`
const inope_stmt = `SELECT bagdet.fullName, bagdet.bagInfo[].tagNum
                    FROM BaggageInfo bagdet WHERE bagdet.fullName IN
                    ("Lucinda Beckman", "Adam Phillips","Dierdre Amador","Fallon Clements")`
const existsope_stmt = `SELECT fullName, bag.ticketNo FROM BaggageInfo bag WHERE
                        EXISTS bag.bagInfo[$element.bagArrivalDate >="2019-03-01T00:00:00"]`

const bag1= `
{
   "ticketNo":"1762376407826",
   "fullName":"Dierdre Amador",
   "gender":"M",
   "contactPhone":"165-742-5715",
   "confNo":"ZG8Z5N",
   "bagInfo":[ {
      "id" : "7903989918469",
      "tagNum" : "17657806240229",
      "routing" : "JFK/MAD",
      "lastActionCode" : "OFFLOAD",
      "lastActionDesc" : "OFFLOAD",
      "lastSeenStation" : "MAD",
      "flightLegs" : [ {
         "flightNo" : "BM495",
         "flightDate" : "2019-03-07T07:00:00Z",
         "fltRouteSrc" : "JFK",
         "fltRouteDest" : "MAD",
         "estimatedArrival" : "2019-03-07T14:00:00Z",
         "actions" : [ {
            "actionAt" : "MAD",
            "actionCode" : "Offload to Carousel at MAD",
            "actionTime" : "2019-03-07T13:54:00Z"
         }, {
            "actionAt" : "JFK",
            "actionCode" : "ONLOAD to MAD",
            "actionTime" : "2019-03-07T07:00:00Z"
         }, {
            "actionAt" : "JFK",
            "actionCode" : "BagTag Scan at JFK",
            "actionTime" : "2019-03-07T06:53:00Z"
         }, {
            "actionAt" : "JFK",
            "actionCode" : "Checkin at JFK",
            "actionTime" : "2019-03-07T05:03:00Z"
         } ]
      } ],
      "lastSeenTimeGmt" : "2019-03-07T13:51:00Z",
      "bagArrivalDate" : "2019-03-07T13:51:00Z"
   } ]
}`
const bag2= `
{
   "ticketNo":"1762344493810",
   "fullName":"Adam Phillips",
   "gender":"M",
   "contactPhone":"893-324-1064",
   "confNo":"LE6J4Z",
   "bagInfo":[ {
      "id" : "79039899165297",
      "tagNum" : "17657806255240",
      "routing" : "MIA/LAX/MEL",
      "lastActionCode" : "OFFLOAD",
      "lastActionDesc" : "OFFLOAD",
      "lastSeenStation" : "MEL",
      "flightLegs" : [ {
         "flightNo" : "BM604",
         "flightDate" : "2019-02-01T06:00:00Z",
         "fltRouteSrc" : "MIA",
         "fltRouteDest" : "LAX",
         "estimatedArrival" : "2019-02-01T11:00:00Z",
         "actions" : [ {
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
      }, {
         "flightNo" : "BM667",
         "flightDate" : "2019-02-01T06:13:00Z",
         "fltRouteSrc" : "LAX",
         "fltRouteDest" : "MEL",
         "estimatedArrival" : "2019-02-01T16:15:00Z",
         "actions" : [ {
            "actionAt" : "MEL",
            "actionCode" : "Offload to Carousel at MEL",
            "actionTime" : "2019-02-01T16:15:00Z"
         }, {
            "actionAt" : "LAX",
            "actionCode" : "ONLOAD to MEL",
            "actionTime" : "2019-02-01T15:35:00Z"
         }, {
            "actionAt" : "LAX",
            "actionCode" : "OFFLOAD from LAX",
            "actionTime" : "2019-02-01T15:18:00Z"
         } ]
      } ],
      "lastSeenTimeGmt" : "2019-02-01T16:13:00Z",
      "bagArrivalDate" : "2019-02-01T16:13:00Z"
   } ]
}`

const bag3= `
{
   "ticketNo":"1762341772625",
   "fullName":"Gerard Greene",
   "gender":"M",
   "contactPhone":"395-837-3772",
   "confNo":"MC0E7R",
   "bagInfo":[ {
      "id" : "79039899152842",
      "tagNum" : "1765780626568",
      "routing" : "SFO/IST/ATH/JTR",
      "lastActionCode" : "OFFLOAD",
      "lastActionDesc" : "OFFLOAD",
      "lastSeenStation" : "JTR",
      "flightLegs" : [ {
         "flightNo" : "BM318",
         "flightDate" : "2019-03-07T04:00:00Z",
         "fltRouteSrc" : "SFO",
         "fltRouteDest" : "IST",
         "estimatedArrival" : "2019-03-07T17:00:00Z",
         "actions" : [ {
            "actionAt" : "SFO",
            "actionCode" : "ONLOAD to IST",
            "actionTime" : "2019-03-07T04:08:00Z"
         }, {
            "actionAt" : "SFO",
            "actionCode" : "BagTag Scan at SFO",
            "actionTime" : "2019-03-07T03:53:00Z"
         }, {
            "actionAt" : "SFO",
            "actionCode" : "Checkin at SFO",
            "actionTime" : "2019-03-07T02:20:00Z"
         } ]
      }, {
         "flightNo" : "BM696",
         "flightDate" : "2019-03-07T05:08:00Z",
         "fltRouteSrc" : "IST",
         "fltRouteDest" : "ATH",
         "estimatedArrival" : "2019-03-08T04:10:00Z",
         "actions" : [ {
            "actionAt" : "IST",
            "actionCode" : "ONLOAD to ATH",
            "actionTime" : "2019-03-08T04:55:00Z"
         }, {
            "actionAt" : "IST",
            "actionCode" : "BagTag Scan at IST",
            "actionTime" : "2019-03-08T04:34:00Z"
         }, {
            "actionAt" : "IST",
            "actionCode" : "OFFLOAD from IST",
            "actionTime" : "2019-03-08T04:47:00Z"
         } ]
      }, {
         "flightNo" : "BM665",
         "flightDate" : "2019-03-07T04:08:00Z",
         "fltRouteSrc" : "ATH",
         "fltRouteDest" : "JTR",
         "estimatedArrival" : "2019-03-07T16:10:00Z",
         "actions" : [ {
            "actionAt" : "JTR",
            "actionCode" : "Offload to Carousel at JTR",
            "actionTime" : "2019-03-07T16:09:00Z"
         }, {
            "actionAt" : "ATH",
            "actionCode" : "ONLOAD to JTR",
            "actionTime" : "2019-03-07T15:51:00Z"
         }, {
            "actionAt" : "ATH",
            "actionCode" : "OFFLOAD from ATH",
            "actionTime" : "2019-03-07T15:43:00Z"
         } ]
      } ],
      "lastSeenTimeGmt" : "2019-03-07T16:01:00Z",
      "bagArrivalDate" : "2019-03-07T16:01:00Z"
   } ]
}`

const bag4= `
{
   "ticketNo":"1762320369957",
   "fullName":"Lorenzo Phil",
   "gender":"M",
   "contactPhone":"364-610-4444",
   "confNo":"QI3V6Q",
   "bagInfo":[ {
      "id" : "79039899187755",
      "tagNum" : "17657806240001",
      "routing" : "SFO/IST/ATH/JTR",
      "lastActionCode" : "OFFLOAD",
      "lastActionDesc" : "OFFLOAD",
      "lastSeenStation" : "JTR",
      "flightLegs" : [ {
         "flightNo" : "BM318",
         "flightDate" : "2019-03-12T03:00:00Z",
         "fltRouteSrc" : "SFO",
         "fltRouteDest" : "IST",
         "estimatedArrival" : "2019-03-12T16:00:00Z",
         "actions" : [ {
            "actionAt" : "SFO",
            "actionCode" : "ONLOAD to IST",
            "actionTime" : "2019-03-12T03:11:00Z"
         }, {
            "actionAt" : "SFO",
            "actionCode" : "BagTag Scan at SFO",
            "actionTime" : "2019-03-12T02:49:00Z"
         }, {
            "actionAt" : "SFO",
            "actionCode" : "Checkin at SFO",
            "actionTime" : "2019-03-12T01:50:00Z"
         } ]
      }, {
         "flightNo" : "BM696",
         "flightDate" : "2019-03-12T04:11:00Z",
         "fltRouteSrc" : "IST",
         "fltRouteDest" : "ATH",
         "estimatedArrival" : "2019-03-13T03:14:00Z",
         "actions" : [ {
            "actionAt" : "IST",
            "actionCode" : "ONLOAD to ATH",
            "actionTime" : "2019-03-13T04:10:00Z"
         }, {
            "actionAt" : "IST",
            "actionCode" : "BagTag Scan at IST",
            "actionTime" : "2019-03-13T03:56:00Z"
         }, {
            "actionAt" : "IST",
            "actionCode" : "OFFLOAD from IST",
            "actionTime" : "2019-03-13T03:59:00Z"
         } ]
      }, {
         "flightNo" : "BM665",
         "flightDate" : "2019-03-12T03:11:00Z",
         "fltRouteSrc" : "ATH",
         "fltRouteDest" : "JTR",
         "estimatedArrival" : "2019-03-12T15:12:00Z",
         "actions" : [ {
            "actionAt" : "JTR",
            "actionCode" : "Offload to Carousel at JTR",
            "actionTime" : "2019-03-12T15:06:00Z"
         }, {
            "actionAt" : "ATH",
            "actionCode" : "ONLOAD to JTR",
            "actionTime" : "2019-03-12T14:16:00Z"
         }, {
            "actionAt" : "ATH",
            "actionCode" : "OFFLOAD from ATH",
            "actionTime" : "2019-03-12T14:13:00Z"
         } ]
      } ],
      "lastSeenTimeGmt" : "2019-03-12T15:05:00Z",
      "bagArrivalDate" : "2019-03-12T15:05:00Z"
   },
   {
      "id" : "79039899197755",
      "tagNum" : "17657806340001",
      "routing" : "SFO/IST/ATH/JTR",
      "lastActionCode" : "OFFLOAD",
      "lastActionDesc" : "OFFLOAD",
      "lastSeenStation" : "JTR",
      "flightLegs" : [ {
         "flightNo" : "BM318",
         "flightDate" : "2019-03-12T03:00:00Z",
         "fltRouteSrc" : "SFO",
         "fltRouteDest" : "IST",
         "estimatedArrival" : "2019-03-12T16:40:00Z",
         "actions" : [ {
            "actionAt" : "SFO",
            "actionCode" : "ONLOAD to IST",
            "actionTime" : "2019-03-12T03:14:00Z"
         }, {
            "actionAt" : "SFO",
            "actionCode" : "BagTag Scan at SFO",
            "actionTime" : "2019-03-12T02:50:00Z"
         }, {
            "actionAt" : "SFO",
            "actionCode" : "Checkin at SFO",
            "actionTime" : "2019-03-12T01:58:00Z"
         } ]
      }, {
         "flightNo" : "BM696",
         "flightDate" : "2019-03-12T04:11:00Z",
         "fltRouteSrc" : "IST",
         "fltRouteDest" : "ATH",
         "estimatedArrival" : "2019-03-13T03:18:00Z",
         "actions" : [ {
            "actionAt" : "IST",
            "actionCode" : "ONLOAD to ATH",
            "actionTime" : "2019-03-13T04:17:00Z"
         }, {
            "actionAt" : "IST",
            "actionCode" : "BagTag Scan at IST",
            "actionTime" : "2019-03-13T03:59:00Z"
         }, {
            "actionAt" : "IST",
            "actionCode" : "OFFLOAD from IST",
            "actionTime" : "2019-03-13T03:48:00Z"
         } ]
      }, {
         "flightNo" : "BM665",
         "flightDate" : "2019-03-12T03:11:00Z",
         "fltRouteSrc" : "ATH",
         "fltRouteDest" : "JTR",
         "estimatedArrival" : "2019-03-12T15:12:00Z",
         "actions" : [ {
            "actionAt" : "JTR",
            "actionCode" : "Offload to Carousel at JTR",
            "actionTime" : "2019-03-12T15:06:00Z"
         }, {
            "actionAt" : "ATH",
            "actionCode" : "ONLOAD to JTR",
            "actionTime" : "2019-03-12T14:16:00Z"
         }, {
            "actionAt" : "ATH",
            "actionCode" : "OFFLOAD from ATH",
            "actionTime" : "2019-03-12T14:23:00Z"
         } ]
      } ],
      "lastSeenTimeGmt" : "2019-03-12T16:05:00Z",
      "bagArrivalDate" : "2019-03-12T16:25:00Z"
   } ]
}`
/**
  * Call the main function for this example
  **/
doSQLOperators();

async function doSQLOperators() {
   let handle;
   try {
      /* UNCOMMENT line of code below if you are using Oracle NoSQL Database
      * Cloud service. Leave the line commented if you are using onPrem database
      handle = await getConnection_cloud(); */
      /* UNCOMMENT line of code below if you are using onPremise Oracle NoSQL
       * Database. Leave the line commented if you are using NoSQL Database
       * Cloud Service
      handle = await getConnection_onPrem(); */
      await createTable(handle);
      let putResult = await handle.put(TABLE_NAME, JSON.parse(bag1));
      let putResult1 = await handle.put(TABLE_NAME, JSON.parse(bag2));
      let putResult2 = await handle.put(TABLE_NAME, JSON.parse(bag3));
      let putResult3 = await handle.put(TABLE_NAME, JSON.parse(bag4));
      console.log("Wrote records of BaggageInfo schema");
      console.log("Using Sequence Comparison operator");
      await fetchData(handle,seqcomp_stmt);

      console.log("Using Logical operator");
      await fetchData(handle,logope_stmt);

      console.log("Using Value Comparison operator");
      await fetchData(handle,valcomp_stmt);

      console.log("Using IN operator");
      await fetchData(handle,inope_stmt);

      console.log("Using EXISTS operator");
      await fetchData(handle,existsope_stmt);
   } catch (error ) {
      console.log(error);
   }
   finally {
      if (handle) {
         handle.close();
      }
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
async function createTable(handle) {
   const createDDL = `CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (ticketNo LONG,
                                                                fullName STRING,
                                                                gender STRING,
                                                                contactPhone STRING,
                                                                confNo STRING,
                                                                bagInfo JSON,
                                                                primary key(ticketNo))`;
   console.log('Create table: ' + createDDL);
   let res =  await handle.tableDDL(createDDL, {
            complete: true,
            tableLimits: {
                readUnits: 20,
                writeUnits: 20,
                storageGB: 1
            }
   });
   console.log('Table created: ' + TABLE_NAME);
}

//fetches data from the table
async function fetchData(handle,querystmt) {
   const opt = {};
   try {
      do {
         const result = await handle.query(querystmt, opt);
         for(let row of result.rows) {
            console.log('  %O', row);
         }
         opt.continuationKey = result.continuationKey;
      } while(opt.continuationKey);
   } catch(error) {
      console.error('  Error: ' + error.message);
   }
}
