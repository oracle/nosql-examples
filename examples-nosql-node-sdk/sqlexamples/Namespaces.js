/* Copyright (c) 2023, 2024 Oracle and/or its affiliates.
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
   let handle;
   try {
      handle = await getConnection_onPrem();
      await createNS(handle);
      await dropNS(handle);      
   } catch (error ) {
      console.log(error);
   }
   finally {
     if (handle) {
        handle.close();
     }
  }
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
