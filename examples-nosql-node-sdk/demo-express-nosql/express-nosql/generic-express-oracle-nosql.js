// 
// Copyright (c) 2022 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
// 

let express = require('express');
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const ServiceType = require('oracle-nosqldb').ServiceType;
const bodyParser = require('body-parser');

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

// Show the structure of the table tablename

app.get('/:tablename/desc', async function (req, res) {
   const { tablename } = req.params;
   try {
      let resExistingTab = await client.getTable(tablename);
      await client.forCompletion(resExistingTab);
      res.send(resExistingTab.schema)
    } catch (err){
        console.error('failed to show tables', err);
        res.sendStatus(500).json({ error: err });
    } finally {
    }
  });


// Create a new record in the table tablename
app.post('/:tablename', async (req, res) => {
    const { tablename } = req.params;
    try {
        const result = await client.put(tablename, req.body, {exactMatch:true} );
        res.json({ result: result});
    } catch (err) {
        console.error('failed to insert data', err);
        res.status(500).json({ error: err });
    }
});

// Get a record from the table tablename by id
// Currently the id is hardcoded as key of the table
app.get('/:tablename/:id', async (req, res) => {
    const { tablename, id } = req.params;
    try {
        const result = await client.get(tablename, { id })
        res.json(result.row);
    } catch (err) {
        console.error('failed to get data', err);
        res.status(500).json({ error: err });
    }
});

// Delete a record from the table tablename by id
// Currently the id is hardcoded as key of the table
app.delete('/:tablename/:id', async (req, res) => {
    const { tablename, id } = req.params;
    try {
        const result = await client.delete(tablename, { id });
        res.json({ result: result});
    } catch (err) {
        console.error('failed to delete data', err);
        res.status(500).json({ error: err });
    }
});

// Get all records for the table tablename
app.get('/:tablename/', async function (req, resW) {
    const { tablename } = req.params;
    let statement = "SELECT * FROM " + tablename;
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

    console.log (statement)  

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

app.get('/', async function (req, res) {

    try {
      let varListTablesResult = await client.listTables();
      res.send(varListTablesResult)
    } catch (err){
        console.error('failed to show tables', err);
        res.sendStatus(500).json({ error: err });
    } finally {
    }
  });
  app.listen(port);
  client = createClient();
  console.log('Application running!');

function createClient() {
  return new NoSQLClient({
            serviceType: ServiceType.KVSTORE,
            endpoint: process.env.NOSQL_ENDPOINT + ":" + process.env.NOSQL_PORT
        });

}
