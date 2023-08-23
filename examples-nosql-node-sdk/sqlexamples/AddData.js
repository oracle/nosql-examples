'use strict';

const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const Region = require('oracle-nosqldb').Region;
const TABLE_NAME = 'stream_acct';
const acct1= `
{
"acct_Id": 1,
"profile_name": "AP",
"account_expiry": "2023-10-18",
"acct_data": {
   "firstName": "Adam",
   "lastName": "Phillips",
   "country": "Germany",
   "contentStreamed": [{
      "showName": "At the Ranch",
      "showId": 26,
      "showtype": "tvseries",
      "genres": ["action", "crime", "spanish"],
      "numSeasons": 4,
      "seriesInfo": [{
         "seasonNum": 1,
         "numEpisodes": 2,
         "episodes": [{
            "episodeID": 20,
            "episodeName": "Season 1 episode 1",
            "lengthMin": 85,
            "minWatched": 85,
            "date": "2022-04-18"
         },
         {
            "episodeID": 30,
            "lengthMin": 60,
            "episodeName": "Season 1 episode 2",
            "minWatched": 60,
            "date": "2022 - 04 - 18 "
         }]
      },
      {
         "seasonNum": 2,
         "numEpisodes": 2,
         "episodes": [{
            "episodeID": 40,
            "episodeName": "Season 2 episode 1",
            "lengthMin": 50,
            "minWatched": 50,
            "date": "2022-04-25"
         },
         {
            "episodeID": 50,
            "episodeName": "Season 2 episode 2",
            "lengthMin": 45,
            "minWatched": 30,
            "date": "2022-04-27"
         }]
      },
      {
         "seasonNum": 3,
         "numEpisodes": 2,
         "episodes": [{
            "episodeID": 60,
            "episodeName": "Season 3 episode 1",
            "lengthMin": 50,
            "minWatched": 50,
            "date": "2022-04-25"
         },
         {
            "episodeID": 70,
            "episodeName": "Season 3 episode 2",
            "lengthMin": 45,
            "minWatched": 30,
            "date": "2022 - 04 - 27 "
         }]
      }]
   },
   {
      "showName": "Bienvenu",
      "showId": 15,
      "showtype": "tvseries",
      "genres": ["comedy", "french"],
      "numSeasons": 2,
      "seriesInfo": [{
         "seasonNum": 1,
         "numEpisodes": 2,
         "episodes": [{
            "episodeID": 20,
            "episodeName": "Bonjour",
            "lengthMin": 45,
            "minWatched": 45,
            "date": "2022-03-07"
         },
         {
            "episodeID": 30,
            "episodeName": "Merci",
            "lengthMin": 42,
            "minWatched": 42,
            "date": "2022-03-08"
         }]
      }]
   }] }
}
`
const acct2= `
{
	"acct_Id":2,
	"profile_name":"Adwi",
	"account_expiry":"2023-10-31",
	"acct_data":
	   {"firstName": "Adelaide",
	   "lastName": "Willard",
	   "country": "France",
	   "contentStreamed": [{
	      "showName" : "Bienvenu",
	      "showId" : 15,
	      "showtype" : "tvseries",
	      "genres" : ["comedy", "french"],
	      "numSeasons" : 2,
	      "seriesInfo": [ {
	         "seasonNum" : 1,
	         "numEpisodes" : 2,
	         "episodes": [ {
	            "episodeID": 22,
					    "episodeName" : "Season 1 episode 1",
	            "lengthMin": 65,
	            "minWatched": 65,
	            "date" : "2022-04-18"
	         },
	         {
	            "episodeID": 32,
	            "lengthMin": 60,
					    "episodeName" : "Season 1 episode 2",
	            "minWatched": 60,
	            "date" : "2022-04-18"
	         }]
	      },
	      {
	         "seasonNum": 2,
	         "numEpisodes" :3,
	         "episodes": [{
	            "episodeID": 42,
					    "episodeName" : "Season 2 episode 1",
	            "lengthMin": 50,
	            "minWatched": 50,
	            "date" : "2022-04-25"
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
	"acct_data":
	   {"firstName": "Dierdre",
	   "lastName": "Amador",
	   "country": "USA",
	   "contentStreamed": [{
	      "showName" : "Bienvenu",
	      "showId" : 15,
	      "showtype" : "tvseries",
	      "genres" : ["comedy", "french"],
	      "numSeasons" : 2,
	      "seriesInfo": [ {
	         "seasonNum" : 1,
	         "numEpisodes" : 2,
	         "episodes": [ {
	            "episodeID": 23,
					    "episodeName" : "Season 1 episode 1",
	            "lengthMin": 45,
	            "minWatched": 40,
	            "date": "2022-08-18"
	         },
	         {
	            "episodeID": 33,
	            "lengthMin": 60,
					    "episodeName" : "Season 1 episode 2",
	            "minWatched": 50,
	            "date" : "2022-08-19"
	         }]
	      },
	      {
	         "seasonNum": 2,
	         "numEpisodes" : 3,
	         "episodes": [{
	            "episodeID": 43,
					    "episodeName" : "Season 2 episode 1",
	            "lengthMin": 50,
	            "minWatched": 50,
	            "date" : "2022-08-25"
	         },
	        {
	            "episodeID": 53,
					    "episodeName" : "Season 2 episode 2",
	            "lengthMin": 45,
	            "minWatched": 30,
	            "date" : "2022-08-27"
	         }
	       ]
	    }
	    ]
	  },
	  {
	      "showName": "Dane",
	      "showId": 16,
	      "showtype": "tvseries",
	      "genres" : ["comedy", "drama","danish"],
	      "numSeasons" : 2,
	      "seriesInfo": [
	      {
	      "seasonNum" : 1,
	      "numEpisodes" : 2,
	      "episodes": [
	      {
	            "episodeID": 24,
					    "episodeName" : "Bonjour",
	            "lengthMin": 45,
	            "minWatched": 45,
	            "date" : "2022-06-07"
	      },
	      {
	            "episodeID": 34,
					    "episodeName" : "Merci",
	            "lengthMin": 42,
	            "minWatched": 42,
	            "date" : "2022-06-08"
	      }
	    ]
	  }
	]
	}
	]}
}
`

/**
  * Call the main function for this example
  **/
doAddData();

/** This function will authenticate with the cloud service,
  * create a table, write a record to the table, then read that record back
  **/
async function doAddData() {
    try {
        let handle = await getConnection(Region.US_ASHBURN_1);
        await createTable(handle);
        let putResult = await handle.put(TABLE_NAME, JSON.parse(acct1));
        let putResult1 = await handle.put(TABLE_NAME, JSON.parse(acct2));
        let putResult2 = await handle.put(TABLE_NAME, JSON.parse(acct3));
        console.log("Added rows to the stream_acct table");

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
/// replace the placeholder for compartment with the OCID of your compartment.
function getConnection(whichRegion) {

    return new NoSQLClient({
       region: whichRegion,
       compartment: "<ocid_your_compartment>",
    });
}

/**
  * This function will create the HelloWorldTable table with two columns,
  * one integer column which will be the primary key and one string which will be the name.
  *
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
}
