/*Copyright (c) 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
'use strict';

const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const Region = require('oracle-nosqldb').Region;
const TABLE_NAME = 'stream_acct';

/**
  * Call the main function for this example
  **/
docreatetable();

/** This function will authenticate with the cloud service,
  * create a table, write a record to the table, then read that record back
  **/
async function docreatetable() {
    try {
        let handle = await getConnection(Region.US_ASHBURN_1);
        await createTable(handle);
        process.exit(0);
    } catch (error ) {
        console.log(error);
        process.exit(-1);
    }
}

/**
  * Create and return an instance of a NoSQLCLient object. NOTE that
  * you need to fill in your cloud credentials and the compartment
  * where you want your table created. Compartments can be dot seperated
  * paths.  For example: developers.dave.
  *
  * @param {Region} which Region An element in the Region enumeration
  * indicating the cloud region you wish to connect to
  */
// replace the placeholder for compartment with the OCID of your compartment.
function getConnection(whichRegion) {

    return new NoSQLClient({
       region: whichRegion,
       compartment: "<ocid_your_compartment>",
    });
}

/**
  * @param {NoSQLClient} handle An instance of NoSQLClient
  */
  async function createTable(handle) {
    const createDDL = `CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (acct_Id INTEGER,
                                                                 profile_name STRING,
                                                                 account_expiry TIMESTAMP(1),
                                                                 acct_data JSON,
                                                                 primary key(acct_Id))`;
    console.log('Create table: ' + createDDL);

        let res =  await handle.tableDDL(createDDL, {
            complete: true,
            tableLimits: {
                readUnits: 20,
                writeUnits: 20,
                storageGB: 1
            }
        });
        console.log('Created table: ' + TABLE_NAME);
}
