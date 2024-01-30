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
   public class MultiDataOps
   {
      private const string Usage =
            "Usage: dotnet run -f <target framework> [-- <config file>]";
      private const string TableName = "examplesAddress";
      private const string add1= @"{
         ""id"":1,
         ""address_line1"":""10 Red Street"",
         ""address_line2"":""Apt 3"",
         ""pin"":1234567
      }";
      private const string add2= @"{
         ""id"":2,
         ""address_line1"":""2 Green Street"",
         ""address_line2"":""Street 9"",
         ""pin"":1234567
      }";
      private const string add3= @"{
         ""id"":3,
         ""address_line1"":""5 Blue Ave"",
         ""address_line2"":""Floor 9"",
         ""pin"":1234567
      }";
      private const string add4= @"{
         ""id"":4,
         ""address_line1"":""9 Yellow Boulevard"",
         ""address_line2"":""Apt 3"",
         ""pin"":87654321
      }";
      private const string stmt1 ="select * from examplesAddress";

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
                $@"CREATE TABLE IF NOT EXISTS {TableName}(id INTEGER,
                                                          address_line1 STRING,
                                                          address_line2 STRING,
                                                          pin INTEGER,
                                                          PRIMARY KEY(SHARD(pin), id))";

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
         var putResult = await client.PutAsync(TableName, FieldValue.FromJsonString(add1).AsMapValue);
         if (putResult.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult.ConsumedCapacity);
         }
         var putResult1 = await client.PutAsync(TableName, FieldValue.FromJsonString(add2).AsMapValue);
         if (putResult1.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult1.ConsumedCapacity);
         }
         var putResult2 = await client.PutAsync(TableName, FieldValue.FromJsonString(add3).AsMapValue);
         if (putResult2.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult2.ConsumedCapacity);
         }
         var putResult3 = await client.PutAsync(TableName, FieldValue.FromJsonString(add4).AsMapValue);
         if (putResult3.ConsumedCapacity != null)
         {
             Console.WriteLine("  Write used:");
             Console.WriteLine("  " + putResult3.ConsumedCapacity);
         }
      }
      private static async Task fetchData(NoSQLClient client,String querystmt){
         var queryEnumerable = client.GetQueryAsyncEnumerable(querystmt);
         await DoQuery(queryEnumerable);
      }

      private static async Task mulDelRows(NoSQLClient client,int pinval){
         var parKey = new MapValue {["pin"] = pinval};
         var options = new DeleteRangeOptions();
         do
         {
            var result = await client.DeleteRangeAsync(TableName,parKey,options);
            Console.WriteLine($"Deleted {result.DeletedCount} row(s)");
            options.ContinuationKey = result.ContinuationKey;
         } while(options.ContinuationKey != null);
      }
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
            await fetchData(client,stmt1);
            await mulDelRows(client,1234567);
            await fetchData(client,stmt1);

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
      private static async Task DoQuery(IAsyncEnumerable<QueryResult<RecordValue>> queryEnumerable){
         Console.WriteLine("  Query results:");
         await foreach (var result in queryEnumerable) {
            foreach (var row in result.Rows)
            {
               Console.WriteLine();
               Console.WriteLine(row.ToJsonString());
            }
         }
      }
   }
}
