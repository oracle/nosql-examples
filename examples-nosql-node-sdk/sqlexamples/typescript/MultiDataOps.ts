/* Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */
'use strict';
import { NoSQLClient, Region } from 'oracle-nosqldb'; //Uncomment for Cloud
/* import { NoSQLClient, ServiceType  } from 'oracle-nosqldb'; */ //Uncomment for On-Premise

interface AddressInt {
   id: Integer;
   address_line1: String;
   address_line2: String;
   pin: Integer;
}
const TABLE_NAME = 'examplesAddress';
const add1= `
{
   "id":1,
   "address_line1":"10 Red Street",
   "address_line2":"Apt 3",
   "pin":1234567
}`
const add2= `
{
   "id":2,
   "address_line1":"2 Green Street",
   "address_line2":"Street 9",
   "pin":1234567
}`
const add3= `
{
   "id":3,
   "address_line1":"5 Blue Ave",
   "address_line2":"Floor 9",
   "pin":1234567
}`
const add4= `
{
   "id":4,
   "address_line1":"9 Yellow Boulevard",
   "address_line2":"Apt 3",
   "pin":87654321
}`
const stmt1 = 'select * from examplesAddress';


/**
  * Call the main function 
  **/
doMultiDataOps();

async function doMultiDataOps() {
   let handle;
   try {
      /* UNCOMMENT line of code below if you are using Oracle NoSQL Database
      * Cloud service. Leave the line commented if you are using onPrem database*/
      handle = await getConnection_cloud();
      /* UNCOMMENT line of code below if you are using onPremise Oracle NoSQL
       * Database. Leave the line commented if you are using NoSQL Database
       * Cloud Service
      handle = await getConnection_onPrem(); */
      await createTable(handle);
      let putResult = await handle.put<AddressInt>(TABLE_NAME, JSON.parse(add1));
      let putResult1 = await handle.put<AddressInt>(TABLE_NAME, JSON.parse(add2));
      let putResult2 = await handle.put<AddressInt>(TABLE_NAME, JSON.parse(add3));
      let putResult3 = await handle.put<AddressInt>(TABLE_NAME, JSON.parse(add4));
      console.log("Wrote records of examplesAddress schema");
      await fetchRowCnt(handle,stmt1);
      await mulRowDel(handle,1234567);
      await fetchRowCnt(handle,stmt1);
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
async function createTable(handle: NoSQLClient) {
   const createDDL = `CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (id INTEGER,
                                                                 address_line1 STRING,
                                                                 address_line2 STRING,
                                                                 pin INTEGER,
                                                                 PRIMARY KEY(SHARD(pin), id))`;
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
async function fetchRowCnt(handle: NoSQLClient,querystmt: string) {
   const opt = {};
   try {
      do {
         const result = await handle.query<AddressInt>(querystmt, opt);
         for(let row of result.rows) {
            console.log('  %O', row);
         }
         opt.continuationKey = result.continuationKey;
      } while(opt.continuationKey);
   } catch(error) {
      console.error('  Error: ' + error.message);
   }
}

//deletes multiple rows
async function mulRowDel(handle: NoSQLClient,pinval: string){
   try {
      /* Unconditional delete, should succeed.*/
      var result = await handle.deleteRange<AddressInt>(TABLE_NAME, { pin: pinval });
      /* Expected output: delete succeeded*/
      console.log('delete ' + result.success ? 'succeeded' : 'failed');
   } catch(error) {
      console.error('  Error: ' + error.message);
   }
}
