// 
// Copyright (c) 2022 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
// 


// You need create the following table described in ddl.sql

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

// Create a new blog
app.post('/', async (req, res) => {
    try {
        const result = await client.put("BlogTable", req.body );
        res.json({ result: result});
    } catch (err) {
        console.error('failed to insert data', err);
        res.status(500).json({ error: err });
    }
});

// Get a blog by id
app.get('/:id', async (req, res) => {
    const { id } = req.params;
    try {
        const result = await client.get("BlogTable", { id })
        res.json(result.row);
    } catch (err) {
        console.error('failed to get data', err);
        res.status(500).json({ error: err });
    }
});

// Delete a blog
app.delete('/:id', async (req, res) => {
    const { id } = req.params;
    try {
        const result = await client.delete("BlogTable", { id });
        res.json({ result: result});
    } catch (err) {
        console.error('failed to delete data', err);
        res.status(500).json({ error: err });
    }
});

// Get all blog
app.get('/', async function (req, resW) {
    let statement = `SELECT * FROM blogtable`;
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

  app.listen(port);
  client = createClient();
  console.log('Application running!');

function createClient() {
  return new NoSQLClient({
            serviceType: ServiceType.KVSTORE,
            endpoint: process.env.NOSQL_ENDPOINT + ":" + process.env.NOSQL_PORT
        });
}
