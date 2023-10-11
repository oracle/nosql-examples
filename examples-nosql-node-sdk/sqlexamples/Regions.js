/* Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
'use strict';
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const TABLE_NAME = 'stream_acct';

/*
 * Call the main function for this example
 */
doregions();

/* This function will authenticate with the cloud service,
 * create a table, write a record to the table, then read that record back
 */
async function doregions() {
    try {
        let handle = await getConnection_onPrem();
        await createRegion(handle);
        await crtTabInRegion(handle);
        await dropTabInRegion(handle);
        await dropRegion(handle);
        process.exit(0);
    } catch (error ) {
        console.log(error);
        process.exit(-1);
    }
}

/* Create and return an instance of a NoSQLCLient object for onPremises */
function getConnection_onPrem() {
   /* replace the placeholder with your hostname */
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
}) */
}

/* creates a remote and a local region */
async function createRegion(handle) {
   /* Create a remote region */
   const crtRemReg = `CREATE REGION LON`;
   let res = await handle.adminDDL(crtRemReg);
   console.log('Remote region created: LON' );
   /* Create a local region */
   const crtLocalReg = `SET LOCAL REGION FRA`;
   let res1 = await handle.adminDDL(crtLocalReg);
   console.log('Local region created: FRA' );
}
/* creates a table ina  given region */
async function crtTabInRegion(handle) {
   const createDDL = `CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (acct_Id INTEGER,
                                                                 profile_name STRING,
                                                                 account_expiry TIMESTAMP(1),
                                                                 acct_data JSON,
                                                                 primary key(acct_Id)) IN REGIONS FRA`;
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

/* drop a table from a region */
async function dropTabInRegion(handle) {
   const dropDDL = `DROP TABLE ${TABLE_NAME}`;
   let res =  await handle.tableDDL(dropDDL);
   console.log('Table dropped: ' + TABLE_NAME);
}
/* drop a region */
async function dropRegion(handle) {
   const dropReg = `DROP REGION LON`;
   let res = await handle.adminDDL(dropReg);
   console.log('Region dropped: LON' );
}
