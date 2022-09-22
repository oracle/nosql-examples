// 
// Copyright (c) 2022 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
// 

'use strict';

const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const Region = require('oracle-nosqldb').Region;
const ServiceType = require('oracle-nosqldb').ServiceType;

// Target table used by this example
const TABLE_NAME = 'blogtable';
const USAGE = 'Usage: node load.js cloud|cloudsim|kvstore';

async function quickstart() {
    let client;
    try {
        const args = process.argv;
        let serviceType = args[2];
        if (!serviceType) {
            return console.error(USAGE);
        }
        // Set up access to the cloud service
        client = createClient(serviceType);
        console.log('Created NoSQLClient instance');
        await run(client);
        console.log('Success!');
    } catch (err) {
        console.error('  Error: ' + err.message);
        console.error('  from: ');
        console.error(err.operation.api.name);
    } finally {
        if (client) {
            client.close();
        }
    }
}

/*
 * This function encapsulates environmental differences and returns a
 * client handle to use for data operations.
 */
function createClient(serviceType) {

    switch(serviceType) {
    case 'cloud':
        return new NoSQLClient({
            region: process.env.NOSQL_REGION ,
            compartment:process.env.NOSQL_COMPID,
            auth: {
              iam: {
                  useInstancePrincipal: true
              }
            }
        });

    case 'cloudsim':
        /*
         * EDIT: if the endpoint does not reflect how the Cloud
         * Simulator has been started, modify it accordingly.
         */
        return new NoSQLClient({
            serviceType: ServiceType.CLOUDSIM,
            endpoint: 'localhost:8080'
        });
    case 'kvstore':
        /*
         * EDIT: if the endpoint does not reflect how the Proxy
         * Server has been started, modify it accordingly.
         */
        return new NoSQLClient({
            serviceType: ServiceType.KVSTORE,
            endpoint: process.env.NOSQL_ENDPOINT + ":" + process.env.NOSQL_PORT
	});
    default:
        throw new Error('Unknown service type: ' + serviceType);
    }
}

/*
 * Create a table, read and write a record
 */
async function run(client) {


try {
  const createDDL = `CREATE TABLE IF NOT EXISTS blogtable \
     (id INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 NO CYCLE), blog STRING, PRIMARY KEY (id))`;
  console.log('Create table ' + TABLE_NAME);
  let resTab = await client.tableDDL(createDDL, {
      tableLimits: {
          readUnits: 20,
          writeUnits: 20,
          storageGB: 1
      }
  });
  await client.forCompletion(resTab);
  console.log('  Creating table %s', resTab.tableName);
  console.log('  Table state: %s', resTab.tableState.name);
	
  let res =null;
  var i;
  for (i = 0; i < 1000; i++) {
    res = await client.put(TABLE_NAME, {
//        id : i,
        blog: 'Creating an empty blog tagged #' + i
    });
  }
}
catch (e) {
  console.log(e);
}
finally {
  console.log("entering and leaving the finally block");
}

}

quickstart();
