/*Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
'use strict';
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const TABLE_NAME = 'stream_acct';

/*
 * Call the main function for this example
 */
doindexes();

async function doindexes() {
   try {
      /*UNCOMMENT the line of code below if you are using Oracle NoSQL Database Cloud service. Leave the line commented if you are using onPremise database
      let handle = await getConnection_cloud();*/
      /*UNCOMMENT the line of code below if you are using onPremise Oracle NoSQL Database. Leave the line commented if you are using NoSQL Database Cloud Service
      let handle = await getConnection_onPrem();*/
      await createTable(handle);
      await createIndex(handle);
      await dropIndex(handle);
      process.exit(0);
   } catch (error ) {
      console.log(error);
      process.exit(-1);
   }
}
/* Create and return an instance of a NoSQLCLient object for cloud service */
/* replace the placeholder for compartment with the OCID of your compartment.*/
function getConnection_cloud() {
   /*replace the placeholder with your region identifier and with the ocid of your compartment id*/
   const Region = `<your_region_identifier>`;
   return new NoSQLClient({
      region: Region,
      compartment: "<ocid_of_your_compartment>",
   });
}
/* Create and return an instance of a NoSQLCLient object for onPremises*/
function getConnection_onPrem() {
   /*replace the placeholder with your hostname*/
   const kvstore_endpoint = `http://<hostname>:8080`;
   return new NoSQLClient({
      serviceType: "KVSTORE",
      endpoint: kvstore_endpoint
   });
   /* if it is a secure store pass the credentials, username and password
   return new NoSQLClient({
      "serviceType": "KVSTORE",
      "endpoint": "",
      "auth": {
         "kvstore":{
            "user": "",
            "password": ""
         }
      }
})*/
}
/*creates a table*/
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
/*creates an index*/
async function createIndex(handle) {
   const crtindDDL = `CREATE INDEX acct_episodes ON ${TABLE_NAME}(acct_data.contentStreamed[].seriesInfo[].episodes[]  AS ANYATOMIC)`;
   let res =  await handle.tableDDL(crtindDDL);
   console.log('Index acct_episodes is created on table:' + TABLE_NAME);
}
/*drops an index*/
async function dropIndex(handle) {
   const dropindDDL = `DROP INDEX acct_episodes ON ${TABLE_NAME}`;
   let res =  await handle.tableDDL(dropindDDL);
   console.log('Index acct_episodes is dropped from table: ' + TABLE_NAME);
}
