//
// Copyright (c) 2021 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
//

let express = require('express');
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const ServiceType = require('oracle-nosqldb').ServiceType;
const bodyParser = require('body-parser');
const process = require('process');

let app = express();
app.use(bodyParser.json());
let port = process.env.PORT || 3000;

process
.on('SIGTERM', function() {
  console.log("\nTerminating");
  if (client) {
     console.log("\close client SIGTERM");
     client.close();
  }
  process.exit(0);
})
.on('SIGINT', function() {
  console.log("\nTerminating");
  if (client) {
     console.log("\close client SIGINT");
     client.close();
  }
  process.exit(0);
});

// Create a new entry in the demo table
app.post('/demo', async (req, res) => {
    try {
        const result = await client.put("demo", req.body );
        res.json({ result: result});
    } catch (err) {
        console.error('failed to insert data', err);
        res.status(500).json({ error: err });
    }
});

// Create a new entry in the demoKeyVal table
app.post('/demoKeyVal', async (req, res) => {
    try {
        const result = await client.put("demoKeyVal", {value : req.body}, {exactMatch:true} );
        res.json({ result: result});
    } catch (err) {
        console.error('failed to insert data', err);
        res.status(500).json({ error: err });
    }
});

// Get a baggage from the demo table by primary key - ticketNo
app.get('/demo/:ticketNo', async (req, res) => {
    const { ticketNo } = req.params;
    try {
        const result = await client.get("demo", { ticketNo })
        res.json(result.row);
    } catch (err) {
        console.error('failed to get data', err);
        res.status(500).json({ error: err });
    }
});

// Delete a baggage by ticketNo
app.delete('/demo/:ticketNo', async (req, res) => {
    const { ticketNo } = req.params;
    try {
        const result = await client.delete("demo", { ticketNo });
        res.json({ result: result});
    } catch (err) {
        console.error('failed to delete data', err);
        res.status(500).json({ error: err });
    }
});

// Get all baggage in the demo table with pagination
app.get('/demo', async function (req, resW) {
    let statement = `SELECT * FROM demo`;
    const rows = [];

    let offset;

    const page = parseInt(req.query.page);
    const limit = parseInt(req.query.limit);
    const orderby = req.query.orderby;
    if (page)
      console.log (page)
    if (orderby )
      statement = statement + " ORDER BY " + orderby;
    if (limit)
      statement = statement + " LIMIT " + limit;
    if (page) {
      offset = page*limit;
      statement = statement + " OFFSET " + offset;
    }

  
    try {
      let cnt ;
      let res;
      do {
         res = await client.query(statement, { continuationKey:cnt});
         rows.push.apply(rows, res.rows);
         cnt = res.continuationKey;
      } while(res.continuationKey != null);
      resW.send(rows)
    } catch (err){
        console.error('failed to select data', err);
        resW.sendStatus(500).json({ error: err });
    } finally {
    }
  });

  app.listen(port);
  client = createClient();
  console.log('Application running!');


function createClient() {
  console.log (process.env.OCI_REGION)  
  console.log (process.env.NOSQL_COMP_ID) 
  return new NoSQLClient({
      region: process.env.OCI_REGION,
      compartment:process.env.NOSQL_COMP_ID,
            auth: {
                iam: {
                    tenantId: process.env.OCI_TENANCY,
                    userId: process.env.NOSQL_USER_ID,
                    fingerprint: process.env.NOSQL_FINGERPRINT,
                    privateKeyFile: 'NoSQLLabPrivateKey.pem'
                }
            }
        });
}

/*
function createClient() {
  console.log (process.env.OCI_REGION)  
  console.log (process.env.NOSQL_COMP_ID)  
  return  new NoSQLClient({
    region: process.env.OCI_REGION,
    compartment:process.env.NOSQL_COMP_ID,
    auth: {
        iam: {
            useInstancePrincipal: true
        }
    }
  });
}
*/
