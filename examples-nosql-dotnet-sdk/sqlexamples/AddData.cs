/*-
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 *  https://oss.oracle.com/licenses/upl/
 */

namespace Oracle.NoSQL.SDK.Samples
{
    using System;
    using System.Threading.Tasks;
    using Oracle.NoSQL.SDK;
    // ------------------------------------------------------------------------
    // Run the example as:
    //
    // dotnet run -f <target framework>
    //
    // where:
    //   - <target framework> is target framework moniker, supported values
    //     are netcoreapp3.1, net5.0, net7.0

    // -----------------------------------------------------------------------
    public class AddData
    {
        private const string Usage =
            "Usage: dotnet run -f <target framework> [-- <config file>]";
        private const string TableName = "stream_acct";
        private const string acct1= @"{
          ""acct_Id"": 1,
          ""profile_name"": ""AP"",
          ""account_expiry"": ""2023-10-18"",
          ""acct_data"": {
              ""firstName"": ""Adam"",
              ""lastName"": ""Phillips"",
              ""country"": ""Germany"",
              ""contentStreamed"": [{
                 ""showName"": ""At the Ranch"",
                 ""showId"": 26,
                 ""showtype"": ""tvseries"",
                 ""genres"": [""action"", ""crime"", ""spanish""],
                 ""numSeasons"": 4,
                 ""seriesInfo"": [{
                    ""seasonNum"": 1,
                    ""numEpisodes"": 2,
                    ""episodes"": [{
                       ""episodeID"": 20,
                       ""episodeName"": ""Season 1 episode 1"",
                       ""lengthMin"": 85,
                       ""minWatched"": 85,
                       ""date"": ""2022-04-18""
                    },
                    {
                       ""episodeID"": 30,
                       ""lengthMin"": 60,
                       ""episodeName"": ""Season 1 episode 2"",
                       ""minWatched"": 60,
                       ""date"": ""2022 - 04 - 18""
                    }]
                 },
                 {
                    ""seasonNum"": 2,
                    ""numEpisodes"": 2,
                    ""episodes"": [{
                       ""episodeID"": 40,
                       ""episodeName"": ""Season 2 episode 1"",
                       ""lengthMin"": 50,
                       ""minWatched"": 50,
                       ""date"": ""2022-04-25""
                    },
                    {
                       ""episodeID"": 50,
                       ""episodeName"": ""Season 2 episode 2"",
                       ""lengthMin"": 45,
                       ""minWatched"": 30,
                       ""date"": ""2022-04-27""
                    }]
                 },
                 {
                    ""seasonNum"": 3,
                    ""numEpisodes"": 2,
                    ""episodes"": [{
                       ""episodeID"": 60,
                       ""episodeName"": ""Season 3 episode 1"",
                       ""lengthMin"": 50,
                       ""minWatched"": 50,
                       ""date"": ""2022-04-25""
                    },
                    {
                       ""episodeID"": 70,
                       ""episodeName"": ""Season 3 episode 2"",
                       ""lengthMin"": 45,
                       ""minWatched"": 30,
                       ""date"": ""2022 - 04 - 27""
                    }]
                 }]
              },
              {
                ""showName"": ""Bienvenu"",
                 ""showId"": 15,
                ""showtype"": ""tvseries"",
                 ""genres"": [""comedy"", ""french""],
                ""numSeasons"": 2,
                 ""seriesInfo"": [{
                    ""seasonNum"": 1,
                    ""numEpisodes"": 2,
                    ""episodes"": [{
                      ""episodeID"": 20,
                       ""episodeName"": ""Bonjour"",
                       ""lengthMin"": 45,
                       ""minWatched"": 45,
                       ""date"": ""2022-03-07""
                    },
                    {
                       ""episodeID"": 30,
                       ""episodeName"": ""Merci"",
                       ""lengthMin"": 42,
                       ""minWatched"": 42,
                       ""date"": ""2022-03-08""
                    }]
                 }]
              }] }
        }";
        private const string acct2= @"{
        	""acct_Id"":2,
        	""profile_name"":""Adwi"",
        	""account_expiry"":""2023-10-31"",
        	""acct_data"":
        	   {""firstName"": ""Adelaide"",
        	   ""lastName"": ""Willard"",
        	   ""country"": ""France"",
        	   ""contentStreamed"": [{
        	      ""showName"" : ""Bienvenu"",
        	      ""showId"" : 15,
        	      ""showtype"" : ""tvseries"",
        	      ""genres"" : [""comedy"", ""french""],
        	      ""numSeasons"" : 2,
        	      ""seriesInfo"": [ {
        	         ""seasonNum"" : 1,
        	         ""numEpisodes"" : 2,
        	         ""episodes"": [ {
        	            ""episodeID"": 22,
        					    ""episodeName"" : ""Season 1 episode 1"",
        	            ""lengthMin"": 65,
        	            ""minWatched"": 65,
        	            ""date"" : ""2022-04-18""
        	         },
        	         {
        	            ""episodeID"": 32,
        	            ""lengthMin"": 60,
        					    ""episodeName"" : ""Season 1 episode 2"",
        	            ""minWatched"": 60,
        	            ""date"" : ""2022-04-18""
        	         }]
        	      },
        	      {
        	         ""seasonNum"": 2,
        	         ""numEpisodes"" :3,
        	         ""episodes"": [{
        	            ""episodeID"": 42,
        					    ""episodeName"" : ""Season 2 episode 1"",
        	            ""lengthMin"": 50,
        	            ""minWatched"": 50,
        	            ""date"" : ""2022-04-25""
        	            }
        	           ]
        	       }
        	     ]
        	   }
        	]}
        }";
        private const string acct3= @"{
        	""acct_Id"":3,
        	""profile_name"":""Dee"",
        	""account_expiry"":""2023-11-28"",
        	""acct_data"":
        	   {""firstName"": ""Dierdre"",
        	   ""lastName"": ""Amador"",
        	   ""country"": ""USA"",
        	   ""contentStreamed"": [{
        	      ""showName"" : ""Bienvenu"",
        	      ""showId"" : 15,
        	      ""showtype"" : ""tvseries"",
        	      ""genres"" : [""comedy"", ""french""],
        	      ""numSeasons"" : 2,
        	      ""seriesInfo"": [ {
        	         ""seasonNum"" : 1,
        	         ""numEpisodes"" : 2,
        	         ""episodes"": [ {
        	            ""episodeID"": 23,
        					    ""episodeName"" : ""Season 1 episode 1"",
        	            ""lengthMin"": 45,
        	            ""minWatched"": 40,
        	            ""date"": ""2022-08-18""
        	         },
        	         {
        	            ""episodeID"": 33,
        	            ""lengthMin"": 60,
        					    ""episodeName"" : ""Season 1 episode 2"",
        	            ""minWatched"": 50,
        	            ""date"" : ""2022-08-19""
        	         }]
        	      },
        	      {
        	         ""seasonNum"": 2,
        	         ""numEpisodes"" : 3,
        	         ""episodes"": [{
        	            ""episodeID"": 43,
        					    ""episodeName"" : ""Season 2 episode 1"",
        	            ""lengthMin"": 50,
        	            ""minWatched"": 50,
        	            ""date"" : ""2022-08-25""
        	         },
        	        {
        	            ""episodeID"": 53,
        					    ""episodeName"" : ""Season 2 episode 2"",
        	            ""lengthMin"": 45,
        	            ""minWatched"": 30,
        	            ""date"" : ""2022-08-27""
        	         }
        	       ]
        	    }
        	    ]
        	  },
        	  {
        	      ""showName"": ""Dane"",
        	      ""showId"": 16,
        	      ""showtype"": ""tvseries"",
        	      ""genres"" : [""comedy"", ""drama"",""danish""],
        	      ""numSeasons"" : 2,
        	      ""seriesInfo"": [
        	      {
        	      ""seasonNum"" : 1,
        	      ""numEpisodes"" : 2,
        	      ""episodes"": [
        	      {
        	            ""episodeID"": 24,
        					    ""episodeName"" : ""Bonjour"",
        	            ""lengthMin"": 45,
        	            ""minWatched"": 45,
        	            ""date"" : ""2022-06-07""
        	      },
        	      {
        	            ""episodeID"": 34,
        					    ""episodeName"" : ""Merci"",
        	            ""lengthMin"": 42,
        	            ""minWatched"": 42,
        	            ""date"" : ""2022-06-08""
        	      }
        	    ]
        	  }
        	]
        	}
        	]}
        }";


        private static async Task createTable(NoSQLClient client){
          // Create a table
          var sql =
              $@"CREATE TABLE IF NOT EXISTS {TableName}(acct_Id INTEGER,
                                                        profile_name STRING,
                                                        account_expiry TIMESTAMP(1),
                                                        acct_data JSON,
                                                        primary key(acct_Id))";

          Console.WriteLine("\nCreate table {0}", TableName);
          var tableResult = await client.ExecuteTableDDLAsync(sql,
              new TableDDLOptions
              {
                  TableLimits = new TableLimits(20, 20, 1)
              });

          Console.WriteLine("  Creating table {0}", TableName);
          Console.WriteLine("  Table state: {0}", tableResult.TableState);

          /// Wait for the operation completion
          await tableResult.WaitForCompletionAsync();
          Console.WriteLine("  Created table: ",tableResult.TableName); 
        }
        private static async Task insertData(NoSQLClient client, String acctdet){
          var putResult = await client.PutAsync(TableName, FieldValue.FromJsonString(acctdet).AsMapValue);

          if (putResult.ConsumedCapacity != null)
          {
              Console.WriteLine(" Added a row to the stream_acct table");
              Console.WriteLine("  " + putResult.ConsumedCapacity);
          }
        }

        //replace the place holder for compartment with the OCID of your compartmetn
        public static async Task Main(string[] args)
        {
          try {
             using var client =  new NoSQLClient(new NoSQLConfig
               {
                  Region = Region.US_ASHBURN_1,
                  Compartment = "<your_compartment_ocid>"
               });

              Console.WriteLine("Created NoSQLClient instance");
              await createTable(client);
              await insertData(client,acct1);
              await insertData(client,acct2);
              await insertData(client,acct3);
          }
          catch (Exception ex) {
               Console.WriteLine("Exception has occurred:\n{0}: {1}",
               ex.GetType().FullName, ex.Message);
               Console.WriteLine("StackTrace is ");
               Console.WriteLine( ex.StackTrace);
               if (ex.InnerException != null)
                   {
                     Console.WriteLine("\nCaused by:\n{0}: {1}",
                     ex.InnerException.GetType().FullName,
                     ex.InnerException.Message);
                   }
          }
        }

   }
}
