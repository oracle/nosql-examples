// Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at
//  https://oss.oracle.com/licenses/upl/

namespace Oracle.NoSQL.SDK.Samples
{
    using System;
    using System.Threading.Tasks;
    using Oracle.NoSQL.SDK;
    // -----------------------------------------------------------------------
   // Run the example as:
   //
   // dotnet run -f <target framework>
   //
   // where:
   //   - <target framework> is target framework moniker, supported values
   //     are netcoreapp5.1 and net7.0
   // -----------------------------------------------------------------------
   public class AddBagData
   {
      private const string Usage =
            "Usage: dotnet run -f <target framework> [-- <config file>]";
      private const string TableName = "BaggageInfo";
      private const string bag1= @"{
         ""ticketNo"":""1762376407826"",
         ""fullName"":""Dierdre Amador"",
         ""gender"":""M"",
         ""contactPhone"":""165-742-5715"",
         ""confNo"":""ZG8Z5N"",
         ""bagInfo"":[ {
            ""id"" : ""7903989918469"",
            ""tagNum"" : ""17657806240229"",
            ""routing"" : ""JFK/MAD"",
            ""lastActionCode"" : ""OFFLOAD"",
            ""lastActionDesc"" : ""OFFLOAD"",
            ""lastSeenStation"" : ""MAD"",
            ""flightLegs"" : [ {
               ""flightNo"" : ""BM495"",
               ""flightDate"" : ""2019-03-07T07:00:00Z"",
               ""fltRouteSrc"" : ""JFK"",
               ""fltRouteDest"" : ""MAD"",
               ""estimatedArrival"" : ""2019-03-07T14:00:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""MAD"",
                  ""actionCode"" : ""Offload to Carousel at MAD"",
                  ""actionTime"" : ""2019-03-07T13:54:00Z""
               }, {
                  ""actionAt"" : ""JFK"",
                  ""actionCode"" : ""ONLOAD to MAD"",
                  ""actionTime"" : ""2019-03-07T07:00:00Z""
               }, {
                  ""actionAt"" : ""JFK"",
                  ""actionCode"" : ""BagTag Scan at JFK"",
                  ""actionTime"" : ""2019-03-07T06:53:00Z""
               }, {
                  ""actionAt"" : ""JFK"",
                  ""actionCode"" : ""Checkin at JFK"",
                  ""actionTime"" : ""2019-03-07T05:03:00Z""
               } ]
            } ],
            ""lastSeenTimeGmt"" : ""2019-03-07T13:51:00Z"",
            ""bagArrivalDate"" : ""2019-03-07T13:51:00Z""
         } ]
      }";
      private const string bag2= @"{
         ""ticketNo"":""1762344493810"",
         ""fullName"":""Adam Phillips"",
         ""gender"":""M"",
         ""contactPhone"":""893-324-1064"",
         ""confNo"":""LE6J4Z"",
         ""bagInfo"":[ {
            ""id"" : ""79039899165297"",
            ""tagNum"" : ""17657806255240"",
            ""routing"" : ""MIA/LAX/MEL"",
            ""lastActionCode"" : ""OFFLOAD"",
            ""lastActionDesc"" : ""OFFLOAD"",
            ""lastSeenStation"" : ""MEL"",
            ""flightLegs"" : [ {
               ""flightNo"" : ""BM604"",
               ""flightDate"" : ""2019-02-01T06:00:00Z"",
               ""fltRouteSrc"" : ""MIA"",
               ""fltRouteDest"" : ""LAX"",
               ""estimatedArrival"" : ""2019-02-01T11:00:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""MIA"",
                  ""actionCode"" : ""ONLOAD to LAX"",
                  ""actionTime"" : ""2019-02-01T06:13:00Z""
               }, {
                  ""actionAt"" : ""MIA"",
                  ""actionCode"" : ""BagTag Scan at MIA"",
                  ""actionTime"" : ""2019-02-01T05:47:00Z""
               }, {
                  ""actionAt"" : ""MIA"",
                  ""actionCode"" : ""Checkin at MIA"",
                  ""actionTime"" : ""2019-02-01T04:38:00Z""
               } ]
            }, {
               ""flightNo"" : ""BM667"",
               ""flightDate"" : ""2019-02-01T06:13:00Z"",
               ""fltRouteSrc"" : ""LAX"",
               ""fltRouteDest"" : ""MEL"",
               ""estimatedArrival"" : ""2019-02-01T16:15:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""MEL"",
                  ""actionCode"" : ""Offload to Carousel at MEL"",
                  ""actionTime"" : ""2019-02-01T16:15:00Z""
               }, {
                  ""actionAt"" : ""LAX"",
                  ""actionCode"" : ""ONLOAD to MEL"",
                  ""actionTime"" : ""2019-02-01T15:35:00Z""
               }, {
                  ""actionAt"" : ""LAX"",
                  ""actionCode"" : ""OFFLOAD from LAX"",
                  ""actionTime"" : ""2019-02-01T15:18:00Z""
               } ]
            } ],
            ""lastSeenTimeGmt"" : ""2019-02-01T16:13:00Z"",
            ""bagArrivalDate"" : ""2019-02-01T16:13:00Z""
         } ]
      }";
      private const string bag3= @"{
         ""ticketNo"":""1762341772625"",
         ""fullName"":""Gerard Greene"",
         ""gender"":""M"",
         ""contactPhone"":""395-837-3772"",
         ""confNo"":""MC0E7R"",
         ""bagInfo"":[ {
            ""id"" : ""79039899152842"",
            ""tagNum"" : ""1765780626568"",
            ""routing"" : ""SFO/IST/ATH/JTR"",
            ""lastActionCode"" : ""OFFLOAD"",
            ""lastActionDesc"" : ""OFFLOAD"",
            ""lastSeenStation"" : ""JTR"",
            ""flightLegs"" : [ {
               ""flightNo"" : ""BM318"",
               ""flightDate"" : ""2019-03-07T04:00:00Z"",
               ""fltRouteSrc"" : ""SFO"",
               ""fltRouteDest"" : ""IST"",
               ""estimatedArrival"" : ""2019-03-07T17:00:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""ONLOAD to IST"",
                  ""actionTime"" : ""2019-03-07T04:08:00Z""
               }, {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""BagTag Scan at SFO"",
                  ""actionTime"" : ""2019-03-07T03:53:00Z""
               }, {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""Checkin at SFO"",
                  ""actionTime"" : ""2019-03-07T02:20:00Z""
               } ]
            }, {
               ""flightNo"" : ""BM696"",
               ""flightDate"" : ""2019-03-07T05:08:00Z"",
               ""fltRouteSrc"" : ""IST"",
               ""fltRouteDest"" : ""ATH"",
               ""estimatedArrival"" : ""2019-03-08T04:10:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""ONLOAD to ATH"",
                  ""actionTime"" : ""2019-03-08T04:55:00Z""
               }, {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""BagTag Scan at IST"",
                  ""actionTime"" : ""2019-03-08T04:34:00Z""
               }, {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""OFFLOAD from IST"",
                  ""actionTime"" : ""2019-03-08T04:47:00Z""
               } ]
            }, {
               ""flightNo"" : ""BM665"",
               ""flightDate"" : ""2019-03-07T04:08:00Z"",
               ""fltRouteSrc"" : ""ATH"",
               ""fltRouteDest"" : ""JTR"",
               ""estimatedArrival"" : ""2019-03-07T16:10:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""JTR"",
                  ""actionCode"" : ""Offload to Carousel at JTR"",
                  ""actionTime"" : ""2019-03-07T16:09:00Z""
               }, {
                  ""actionAt"" : ""ATH"",
                  ""actionCode"" : ""ONLOAD to JTR"",
                  ""actionTime"" : ""2019-03-07T15:51:00Z""
               }, {
                  ""actionAt"" : ""ATH"",
                  ""actionCode"" : ""OFFLOAD from ATH"",
                  ""actionTime"" : ""2019-03-07T15:43:00Z""
               } ]
            } ],
            ""lastSeenTimeGmt"" : ""2019-03-07T16:01:00Z"",
            ""bagArrivalDate"" : ""2019-03-07T16:01:00Z""
         } ]
      }";
      private const string bag4= @"{
         ""ticketNo"":""1762320369957"",
         ""fullName"":""Lorenzo Phil"",
         ""gender"":""M"",
         ""contactPhone"":""364-610-4444"",
         ""confNo"":""QI3V6Q"",
         ""bagInfo"":[ {
            ""id"" : ""79039899187755"",
            ""tagNum"" : ""17657806240001"",
            ""routing"" : ""SFO/IST/ATH/JTR"",
            ""lastActionCode"" : ""OFFLOAD"",
            ""lastActionDesc"" : ""OFFLOAD"",
            ""lastSeenStation"" : ""JTR"",
            ""flightLegs"" : [ {
               ""flightNo"" : ""BM318"",
               ""flightDate"" : ""2019-03-12T03:00:00Z"",
               ""fltRouteSrc"" : ""SFO"",
               ""fltRouteDest"" : ""IST"",
               ""estimatedArrival"" : ""2019-03-12T16:00:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""ONLOAD to IST"",
                  ""actionTime"" : ""2019-03-12T03:11:00Z""
               }, {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""BagTag Scan at SFO"",
                  ""actionTime"" : ""2019-03-12T02:49:00Z""
               }, {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""Checkin at SFO"",
                  ""actionTime"" : ""2019-03-12T01:50:00Z""
               } ]
            }, {
               ""flightNo"" : ""BM696"",
               ""flightDate"" : ""2019-03-12T04:11:00Z"",
               ""fltRouteSrc"" : ""IST"",
               ""fltRouteDest"" : ""ATH"",
               ""estimatedArrival"" : ""2019-03-13T03:14:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""ONLOAD to ATH"",
                  ""actionTime"" : ""2019-03-13T04:10:00Z""
               }, {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""BagTag Scan at IST"",
                  ""actionTime"" : ""2019-03-13T03:56:00Z""
               }, {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""OFFLOAD from IST"",
                  ""actionTime"" : ""2019-03-13T03:59:00Z""
               } ]
            }, {
               ""flightNo"" : ""BM665"",
               ""flightDate"" : ""2019-03-12T03:11:00Z"",
               ""fltRouteSrc"" : ""ATH"",
               ""fltRouteDest"" : ""JTR"",
               ""estimatedArrival"" : ""2019-03-12T15:12:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""JTR"",
                  ""actionCode"" : ""Offload to Carousel at JTR"",
                  ""actionTime"" : ""2019-03-12T15:06:00Z""
               }, {
                  ""actionAt"" : ""ATH"",
                  ""actionCode"" : ""ONLOAD to JTR"",
                  ""actionTime"" : ""2019-03-12T14:16:00Z""
               }, {
                  ""actionAt"" : ""ATH"",
                  ""actionCode"" : ""OFFLOAD from ATH"",
                  ""actionTime"" : ""2019-03-12T14:13:00Z""
               } ]
            } ],
            ""lastSeenTimeGmt"" : ""2019-03-12T15:05:00Z"",
            ""bagArrivalDate"" : ""2019-03-12T15:05:00Z""
         },
         {
            ""id"" : ""79039899197755"",
            ""tagNum"" : ""17657806340001"",
            ""routing"" : ""SFO/IST/ATH/JTR"",
            ""lastActionCode"" : ""OFFLOAD"",
            ""lastActionDesc"" : ""OFFLOAD"",
            ""lastSeenStation"" : ""JTR"",
            ""flightLegs"" : [ {
               ""flightNo"" : ""BM318"",
               ""flightDate"" : ""2019-03-12T03:00:00Z"",
               ""fltRouteSrc"" : ""SFO"",
               ""fltRouteDest"" : ""IST"",
               ""estimatedArrival"" : ""2019-03-12T16:40:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""ONLOAD to IST"",
                  ""actionTime"" : ""2019-03-12T03:14:00Z""
               }, {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""BagTag Scan at SFO"",
                  ""actionTime"" : ""2019-03-12T02:50:00Z""
               }, {
                  ""actionAt"" : ""SFO"",
                  ""actionCode"" : ""Checkin at SFO"",
                  ""actionTime"" : ""2019-03-12T01:58:00Z""
               } ]
            }, {
               ""flightNo"" : ""BM696"",
               ""flightDate"" : ""2019-03-12T04:11:00Z"",
               ""fltRouteSrc"" : ""IST"",
               ""fltRouteDest"" : ""ATH"",
               ""estimatedArrival"" : ""2019-03-13T03:18:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""ONLOAD to ATH"",
                  ""actionTime"" : ""2019-03-13T04:17:00Z""
               }, {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""BagTag Scan at IST"",
                  ""actionTime"" : ""2019-03-13T03:59:00Z""
               }, {
                  ""actionAt"" : ""IST"",
                  ""actionCode"" : ""OFFLOAD from IST"",
                  ""actionTime"" : ""2019-03-13T03:48:00Z""
               } ]
            }, {
               ""flightNo"" : ""BM665"",
               ""flightDate"" : ""2019-03-12T03:11:00Z"",
               ""fltRouteSrc"" : ""ATH"",
               ""fltRouteDest"" : ""JTR"",
               ""estimatedArrival"" : ""2019-03-12T15:12:00Z"",
               ""actions"" : [ {
                  ""actionAt"" : ""JTR"",
                  ""actionCode"" : ""Offload to Carousel at JTR"",
                  ""actionTime"" : ""2019-03-12T15:06:00Z""
               }, {
                  ""actionAt"" : ""ATH"",
                  ""actionCode"" : ""ONLOAD to JTR"",
                  ""actionTime"" : ""2019-03-12T14:16:00Z""
               }, {
                  ""actionAt"" : ""ATH"",
                  ""actionCode"" : ""OFFLOAD from ATH"",
                  ""actionTime"" : ""2019-03-12T14:23:00Z""
               } ]
            } ],
            ""lastSeenTimeGmt"" : ""2019-03-12T16:05:00Z"",
            ""bagArrivalDate"" : ""2019-03-12T16:25:00Z""
         } ]
      }";
      //replace the place holder for compartment with the OCID of your compartment
      public static async Task Main(string[] args)
      {
         try {
            //if using cloud service uncomment the code below
            var client = await getconnection_cloud();
            //if using onPremise uncomment the code below
            //var client = await getconnection_onPrem();
            Console.WriteLine("Created NoSQLClient instance");
            await crtTabAddData(client);
            Console.WriteLine("\nSuccess!");
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
      // Get a connection handle for Oracle NoSQL Database Cloud Service
      private async static Task<NoSQLClient> getconnection_cloud()
      {
         // replace the region and compartment place holders with actual values
         var client =  new NoSQLClient(new NoSQLConfig
         {
            Region = <your_region_identifier>,
            Compartment = "<your_compartment_ocid>"
         });
         return client;
      }
      // Get a connection handle for onPremise data store
      private async static Task<NoSQLClient> getconnection_onPrem()
      {
         // replace the placeholder with your fullname of your host
         var client = new NoSQLClient(new NoSQLConfig
         {
            ServiceType = ServiceType.KVStore,
            Endpoint = "http://<hostname>:8080"
         });
         return client;
      }

      private static async Task crtTabAddData(NoSQLClient client)
      {
      // Create a table
         var sql =
                $@"CREATE TABLE IF NOT EXISTS {TableName}(ticketNo LONG,
                                                          fullName STRING,
                                                          gender STRING,
                                                          contactPhone STRING,
                                                          confNo STRING,
                                                          bagInfo JSON,
                                                          primary key(ticketNo))";

         Console.WriteLine("\nCreate table {0}", TableName);
         var tableResult = await client.ExecuteTableDDLAsync(sql,
            new TableDDLOptions
            {
               TableLimits = new TableLimits(20, 20, 1)
            });

         Console.WriteLine("  Creating table {0}", TableName);
         Console.WriteLine("  Table state: {0}", tableResult.TableState);
         // Wait for the operation completion
         await tableResult.WaitForCompletionAsync();
         Console.WriteLine("  Table {0} is created",
             tableResult.TableName);
         Console.WriteLine("  Table state: {0}", tableResult.TableState);
         // Write a record
         Console.WriteLine("\nInsert records");
         var putResult = await client.PutAsync(TableName, FieldValue.FromJsonString(bag1).AsMapValue);
         if (putResult.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult.ConsumedCapacity);
         }
         var putResult1 = await client.PutAsync(TableName, FieldValue.FromJsonString(bag2).AsMapValue);
         if (putResult1.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult1.ConsumedCapacity);
         }
         var putResult2 = await client.PutAsync(TableName, FieldValue.FromJsonString(bag3).AsMapValue);
         if (putResult2.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult2.ConsumedCapacity);
         }
         var putResult3 = await client.PutAsync(TableName, FieldValue.FromJsonString(bag4).AsMapValue);
         if (putResult3.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult3.ConsumedCapacity);
         }
      }
   }
}
