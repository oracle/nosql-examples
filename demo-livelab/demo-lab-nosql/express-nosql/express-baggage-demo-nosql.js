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
let port = process.env.PORT || 3500;

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


// Get all  baggage with pagination
app.get('/getBagInfoByTicketNumber', async function (req, resW) {
    let statement = `SELECT * FROM demo`;
    const rows = [];

    const ticketNo = parseInt(req.query.ticketNo);
    if (ticketNo)
      statement = statement + " WHERE ticketNo = '" + ticketNo + "'";

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

app.get('/getPassengersAffectedByFlight', async function (req, resW) {
    let statement = `SELECT d.ticketNo as ticketNo, d.fullName as fullName, d.contactPhone as contactInfo, size(d.bagInfo) as numBags FROM demo d `;
    let result ;

    const flightNo = req.query.flightNo;
    if (flightNo)
      statement = statement + "WHERE d.bagInfo.flightLegs.flightNo =ANY '" + flightNo +"'";
    result = {'message': "getPassengersAffectedByFlight" + " under construction." , "sql" : statement, "index" : "bagInfo.flightLegs.flightNo", "endPoint":"executeSQL"}
    resW.send(result)
	
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
