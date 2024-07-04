//
// Copyright (c) 2024 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
//

const fs = require('fs');
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const ServiceType = require('oracle-nosqldb').ServiceType;
const CapacityMode = require('oracle-nosqldb').ServiceType;


async function createTable (client) {
  const createDDL = fs.readFileSync('ddl.sql', 'utf8')
  // readUnits, writeUnits, storageGB using same values as for Always free
  const resTab = await client.tableDDL(createDDL, {
    tableLimits: {
      // mode: CapacityMode.ON_DEMAND,
      mode: CapacityMode.PROVISIONED,
      readUnits: 50,
      writeUnits: 50,
      storageGB: 25
    },
    complete: true
  })
  await client.forCompletion(resTab)
  console.log('  Creating table %s', resTab.tableName)
  console.log('  Table state: %s', resTab.tableState.name)
}

function createClient() {
  return new NoSQLClient({
            serviceType: ServiceType.KVSTORE,
            endpoint: process.env.NOSQL_ENDPOINT + ":" + process.env.NOSQL_PORT
        });
}


// Connecting to NoSQL and create table
async function run() {
    let client;
    client = createClient()
    await createTable(client)
    if (client) {
      client.close();
    }
}

run()

