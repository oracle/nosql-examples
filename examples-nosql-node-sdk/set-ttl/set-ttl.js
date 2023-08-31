// 
// Copyright (c) 2023 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
// 

'use strict';

const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const Region = require('oracle-nosqldb').Region;
const ServiceType = require('oracle-nosqldb').ServiceType;

// Target table used by this example
const USAGE = 'Usage: node tll.js serviceType TABLE_NAME TTL_FIELD';
const USAGE_EXAMPLE = 'Example: node tll.js cloud|cloudsim|kvstore test_ddb document.ttl';

async function quickstart() {
    let client;
    try {
        const args = process.argv;
        let serviceType = args[2];
        if (!serviceType) {
            return console.error(USAGE + '\n' + USAGE_EXAMPLE);
        }
        let TABLE_NAME = args[3];
        if (!TABLE_NAME) {
           TABLE_NAME = 'test_ddb';
        }
        let TTL_FIELD = args[4];
        if (!TTL_FIELD) {
           TTL_FIELD = 'document.ttl'
        }
        // Set up access to the cloud service
        client = createClient(serviceType);
        console.log('Created NoSQLClient instance');
        await run(client, TABLE_NAME, TTL_FIELD);
        console.log('Success!');
    } catch (err) {
        console.error('  Error: ' + err.message);
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
            //auth: { iam: { useInstancePrincipal: true } }
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
            endpoint: 'node1-nosql:8087'
        });

    default:
        throw new Error('Unknown service type: ' + serviceType);
    }
}

/*
 * Create a table, read and write a record
 */
async function run(client, TABLE_NAME, TTL_FIELD) {

try {
  let statement = `select $u as obj, $u.${TTL_FIELD} as ttl, current_time_millis()/1000 as ctepochsec, current_time() as ct, cast(cast($u.${TTL_FIELD} as LONG) * 1000  as timestamp) as nt, remaining_hours($u) as rh from ${TABLE_NAME} $u `;
  let newTTL;
  let resUpd;

  for await(const res of client.queryIterable(statement)) {
     for(const row in res.rows){
           //console.log(res.rows[row]);
           newTTL = Math.trunc( ( res.rows[row].ttl - res.rows[row].ctepochsec) / 3600)
           if (newTTL >= 1) {
             resUpd = await client.putIfPresent(TABLE_NAME, res.rows[row].obj ,  {ttl: { hours: Math.max(1, newTTL)} });
             console.log(resUpd);
           } else {
             console.log("Old document: " + newTTL);
             resUpd = await client.putIfPresent(TABLE_NAME, res.rows[row].obj ,  {ttl: { hours: 1} });
             console.log(resUpd);
           }
     }
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
