//
// Copyright (c) 2022 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
//

import fs from 'fs'
import pkg from 'oracle-nosqldb'
const { NoSQLClient, ServiceType, CapacityMode } = pkg

async function createTable (client) {
  const createDDL = fs.readFileSync('demo-stream-acct.ddl', 'utf8')
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
  let indexes = ['demo-stream-acct-idx1.ddl','demo-stream-acct-idx2.ddl','demo-stream-acct-idx3.ddl'];
  for (let idx of indexes) {
      const indexDDL = fs.readFileSync(idx, 'utf8')
      await client.tableDDL(indexDDL, {complete: true});
      console.log('  Creating index %s', idx);
  }
}

function createNoSQLClient () {
  switch (process.env.NOSQL_ServiceType) {
    case 'useInstancePrincipal':
      return new NoSQLClient({
        region: process.env.NOSQL_REGION,
        compartment: process.env.NOSQL_COMPID,
        auth: {
          iam: {
            useInstancePrincipal: true
          }
        }
      })
    case 'useDelegationToken':
      return new NoSQLClient({
        region: process.env.NOSQL_REGION,
        compartment: process.env.NOSQL_COMPID,
        auth: {
          iam: {
            useInstancePrincipal: true,
            delegationTokenProvider: process.env.OCI_DELEGATION_TOKEN_FILE
          }
        }
      })
    default:
      // on-premise non-secure configuration or Cloud Simulator
      return new NoSQLClient({
        serviceType: ServiceType.KVSTORE,
        endpoint: process.env.NOSQL_ENDPOINT + ':' + process.env.NOSQL_PORT
      })
  }
}

// Connecting to NoSQL and create table
async function quickstart() {
    let client;
    client = createNoSQLClient()
    await createTable(client)
    if (client) {
      client.close();
    }
}

quickstart()

