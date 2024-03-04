/* Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */
'use strict';
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const TABLE_NAME = 'stream_acct';

/*
 * Call the main function for this example
 */
doaltertable();

async function doaltertable() {
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
      await alterTable(handle);
      await dropTable(handle);
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
/* creates a table */
async function createTable(handle) {
   const createDDL = `CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (acct_Id INTEGER,
                                                                 profile_name STRING,
                                                                 account_expiry TIMESTAMP(1),
                                                                 acct_data JSON,
                                                                 primary key(acct_Id))`;
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
/* alter a table and add a column */
async function alterTable(handle) {
   const alterDDL = `ALTER TABLE ${TABLE_NAME} (ADD acctname STRING)`;
   let res =  await handle.tableDDL(alterDDL);
   console.log('Table altered: ' + TABLE_NAME);
}
/* drop a table */
async function dropTable(handle) {
   const dropDDL = `DROP TABLE ${TABLE_NAME}`;
   let res =  await handle.tableDDL(dropDDL);
   console.log('Table dropped: ' + TABLE_NAME);
}
