/*Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
'use strict';
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const Region = require('oracle-nosqldb').Region;
const TABLE_NAME = 'stream_acct';
const acct1= `
{
"acct_Id":1,
"profile_name":"AP",
"account_expiry":"2023-10-18",
"acct_data":{
   "firstName":"Adam",
   "lastName":"Phillips",
   "country":"Germany",
   "contentStreamed":[
      {
         "showName":"At the Ranch",
         "showId":26,
         "showtype":"tvseries",
         "genres":[
            "action",
            "crime",
            "spanish"
         ],
         "numSeasons":4,
         "seriesInfo":[
            {
               "seasonNum":1,
               "numEpisodes":2,
               "episodes":[
                  {
                     "episodeID":20,
                     "episodeName":"Season 1 episode 1",
                     "lengthMin":85,
                     "minWatched":85,
                     "date":"2022-04-18"
                  },
                  {
                     "episodeID":30,
                     "lengthMin":60,
                     "episodeName":"Season 1 episode 2",
                     "minWatched":60,
                     "date":"2022 - 04 - 18 "
                  }
               ]
            },
            {
               "seasonNum":2,
               "numEpisodes":2,
               "episodes":[
                  {
                     "episodeID":40,
                     "episodeName":"Season 2 episode 1",
                     "lengthMin":50,
                     "minWatched":50,
                     "date":"2022-04-25"
                  },
                  {
                     "episodeID":50,
                     "episodeName":"Season 2 episode 2",
                     "lengthMin":45,
                     "minWatched":30,
                     "date":"2022-04-27"
                  }
               ]
            },
            {
               "seasonNum":3,
               "numEpisodes":2,
               "episodes":[
                  {
                     "episodeID":60,
                     "episodeName":"Season 3 episode 1",
                     "lengthMin":50,
                     "minWatched":50,
                     "date":"2022-04-25"
                  },
                  {
                     "episodeID":70,
                     "episodeName":"Season 3 episode 2",
                     "lengthMin":45,
                     "minWatched":30,
                     "date":"2022 - 04 - 27 "
                  }
               ]
            }
         ]
      },
      {
         "showName":"Bienvenu",
         "showId":15,
         "showtype":"tvseries",
         "genres":[
            "comedy",
            "french"
         ],
         "numSeasons":2,
         "seriesInfo":[
            {
               "seasonNum":1,
               "numEpisodes":2,
               "episodes":[
                  {
                     "episodeID":20,
                     "episodeName":"Bonjour",
                     "lengthMin":45,
                     "minWatched":45,
                     "date":"2022-03-07"
                  },
                  {
                     "episodeID":30,
                     "episodeName":"Merci",
                     "lengthMin":42,
                     "minWatched":42,
                     "date":"2022-03-08"
                  }
               ] }
         ] }
   ]}
}`
const acct2= `
{
"acct_Id":2,
"profile_name":"Adwi",
"account_expiry":"2023-10-31",
"acct_data":{
   "firstName":"Adelaide",
   "lastName":"Willard",
   "country":"France",
   "contentStreamed":[
      {
         "showName":"Bienvenu",
         "showId":15,
         "showtype":"tvseries",
         "genres":[
            "comedy",
            "french"
         ],
         "numSeasons":2,
         "seriesInfo":[
            {
               "seasonNum":1,
               "numEpisodes":2,
               "episodes":[
                  {
                     "episodeID":22,
                     "episodeName":"Season 1 episode 1",
                     "lengthMin":65,
                     "minWatched":65,
                     "date":"2022-04-18"
                  },
                  {
                     "episodeID":32,
                     "lengthMin":60,
                     "episodeName":"Season 1 episode 2",
                     "minWatched":60,
                     "date":"2022-04-18"
                  }
               ]
            },
            {
               "seasonNum":2,
               "numEpisodes":3,
               "episodes":[
                  {
                     "episodeID":42,
                     "episodeName":"Season 2 episode 1",
                     "lengthMin":50,
                     "minWatched":50,
                     "date":"2022-04-25"
                  }
               ]
            }
         ]
      }
   ]}
}
`
const acct3= `
{
"acct_Id":3,
"profile_name":"Dee",
"account_expiry":"2023-11-28",
"acct_data":{
   "firstName":"Dierdre",
   "lastName":"Amador",
   "country":"USA",
   "contentStreamed":[
      {
         "showName":"Bienvenu",
         "showId":15,
         "showtype":"tvseries",
         "genres":[
            "comedy",
            "french"
         ],
         "numSeasons":2,
         "seriesInfo":[
            {
               "seasonNum":1,
               "numEpisodes":2,
               "episodes":[
                  {
                     "episodeID":23,
                     "episodeName":"Season 1 episode 1",
                     "lengthMin":45,
                     "minWatched":40,
                     "date":"2022-08-18"
                  },
                  {
                     "episodeID":33,
                     "lengthMin":60,
                     "episodeName":"Season 1 episode 2",
                     "minWatched":50,
                     "date":"2022-08-19"
                  }
               ]
            },
            {
               "seasonNum":2,
               "numEpisodes":3,
               "episodes":[
                  {
                     "episodeID":43,
                     "episodeName":"Season 2 episode 1",
                     "lengthMin":50,
                     "minWatched":50,
                     "date":"2022-08-25"
                  },
                  {
                     "episodeID":53,
                     "episodeName":"Season 2 episode 2",
                     "lengthMin":45,
                     "minWatched":30,
                     "date":"2022-08-27"
                  }
               ]
            }
         ]
      },
      {
         "showName":"Dane",
         "showId":16,
         "showtype":"tvseries",
         "genres":[
            "comedy",
            "drama",
            "danish"
         ],
         "numSeasons":2,
         "seriesInfo":[
            {
               "seasonNum":1,
               "numEpisodes":2,
               "episodes":[
                  {
                     "episodeID":24,
                     "episodeName":"Bonjour",
                     "lengthMin":45,
                     "minWatched":45,
                     "date":"2022-06-07"
                  },
                  {
                     "episodeID":34,
                     "episodeName":"Merci",
                     "lengthMin":42,
                     "minWatched":42,
                     "date":"2022-06-08"
                  }
               ]
            }
         ]
      }
   ]}
}
`
const stmt1 = 'select * from stream_acct';
const stmt2 = 'select account_expiry, acct.acct_data.lastName, acct.acct_data.contentStreamed[].showName from stream_acct acct where acct_id=1';

/**
  * Call the main function for this example
 **/
doQueryData();

async function doQueryData() {
   try {
      /*if it is a cloud service uncomment the line below, else if it is onPremise, comment the line below*/
      let handle = await getConnection_cloud();
      /*if it is a onPremise uncomment the line below,else if it is cloud service, comment the line below*/
      /*let handle = await getConnection_onPrem();*/
      await createTable(handle);
      let putResult = await handle.put(TABLE_NAME, JSON.parse(acct1));
      let putResult1 = await handle.put(TABLE_NAME, JSON.parse(acct2));
      let putResult2 = await handle.put(TABLE_NAME, JSON.parse(acct3));
      console.log("Wrote records of acct stream schema");
      console.log("Fetching all rows");
      await fetchData(handle,stmt1);
      console.log("Fetching partial filtered rows");
      await fetchData(handle,stmt2);
      process.exit(0);
   } catch (error ) {
      console.log(error);
      process.exit(-1);
    }
}

/* Create and return an instance of a NoSQLCLient object for cloud service */
/* replace the placeholder for compartment with the OCID of your compartment.*/
function getConnection_cloud() {
   /*replace the placeholder with your region identifier and with the ocid of your compartment id*/
   const Region = `<your_region_identifier>`;
   return new NoSQLClient({
      region: Region,
      compartment: "<ocid_of_your_compartment>",
   });
}
/* Create and return an instance of a NoSQLCLient object for onPremises*/
function getConnection_onPrem() {
   /*replace the placeholder with your hostname*/
   const kvstore_endpoint = `http://<hostname>:8080`;
   return new NoSQLClient({
      serviceType: "KVSTORE",
      endpoint: kvstore_endpoint
   });
   /* if it is a secure store pass the credentials, username and password
   return new NoSQLClient({
      "serviceType": "KVSTORE",
      "endpoint": "",
      "auth": {
         "kvstore":{
            "user": "",
            "password": ""
         }
      }
})*/
}
/*creates a table*/
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
   console.log('Table created: ' + TABLE_NAME);
}
/*fetches data from the table*/
async function fetchData(handle,querystmt) {
   const opt = {};
   try {
      do {
         const result = await handle.query(querystmt, opt);
         for(let row of result.rows) {
            console.log('  %O', row);
         }
         opt.continuationKey = result.continuationKey;
      } while(opt.continuationKey);
   } catch(error) {
      console.error('  Error: ' + error.message);
   }
}
