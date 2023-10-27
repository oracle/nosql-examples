/* Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
'use strict';
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const TABLE_NAME = 'stream_acct';

/*
 * Call the main function for this example
 */
donamespaces();

async function donamespaces() {
   try {
      let handle = await getConnection_onPrem();
      await createNS(handle);
      await dropNS(handle);
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

/* create a namespace */
async function createNS(handle) {
   const createNS = `CREATE NAMESPACE IF NOT EXISTS ns1`;
   let res = await handle.adminDDL(createNS);
   console.log('Namespace created: ns1' );
}
/* drop a namespace */
async function dropNS(handle) {
   const dropNS = `DROP NAMESPACE ns1`;
   let res = await handle.adminDDL(dropNS);
   console.log('Namespace dropped: ns1' );
}