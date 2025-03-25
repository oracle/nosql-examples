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
   public class TableJoins
   {
      private const string Usage =
            "Usage: dotnet run -f <target framework> [-- <config file>]";
      private const string regTblName = "ticket";
      private const string childTblName = "ticket.bagInfo";
      private const string descTblName = "ticket.bagInfo.flightLegs";
      private const string regtbl_ddl = $@"CREATE TABLE IF NOT EXISTS {regTblName}(ticketNo LONG,
                                                                                   confNo STRING,
                                                                                   primary key(ticketNo))";
      private const string childtbl_ddl = $@"CREATE TABLE IF NOT EXISTS {childTblName}(id LONG,
                                                         tagNum LONG,
                                                         routing STRING,
                                                         lastActionCode STRING,
                                                         lastActionDesc STRING,
                                                         lastSeenStation STRING,
                                                         lastSeenTimeGmt TIMESTAMP(4),
                                                         bagArrivalDate TIMESTAMP(4),
                                                         primary key(id))";
      private const string desctbl_ddl = $@"CREATE TABLE IF NOT EXISTS {descTblName}(flightNo STRING,
                                                         flightDate TIMESTAMP(4),
                                                         fltRouteSrc STRING,
                                                         fltRouteDest STRING,
                                                         estimatedArrival TIMESTAMP(4),
                                                         actions JSON,
                                                         primary key(flightNo))";
      private const string data1=@"{""ticketNo"": ""1762344493810"",""confNo"" : ""LE6J4Z"" }";
      private const string data2=@"{""ticketNo"":""1762344493810"",
                                    ""id"":""79039899165297"",
                                    ""tagNum"":""17657806255240"",
                                    ""routing"":""MIA/LAX/MEL"",
                                    ""lastActionCode"":""OFFLOAD"",
                                    ""lastActionDesc"":""OFFLOAD"",
                                    ""lastSeenStation"":""MEL"",
                                    ""lastSeenTimeGmt"":""2019-02-01T16:13:00Z"",
                                    ""bagArrivalDate"":""2019-02-01T16:13:00Z""}";
      private const string data3=@"{""ticketNo"":""1762344493810"",
                                    ""id"":""79039899165297"",
                                    ""flightNo"":""BM604"",
                                    ""flightDate"":""2019-02-01T06:00:00Z"",
                                    ""fltRouteSrc"":""MIA"",
                                    ""fltRouteDest"":""LAX"",
                                    ""estimatedArrival"":""2019-02-01T11:00:00Z"",
                                    ""actions"":[ {
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
                                       ""actionTime"" : ""2019-02-01T04:38:00Z""} ]}";
      private const string stmt_loj ="SELECT * FROM ticket a LEFT OUTER JOIN ticket.bagInfo.flightLegs b ON a.ticketNo=b.ticketNo";
      private const string stmt_nt ="SELECT * FROM NESTED TABLES (ticket a descendants(ticket.bagInfo.flightLegs b))";
      private const string stmt_ij ="SELECT * FROM ticket a, ticket.bagInfo.flightLegs b WHERE a.ticketNo=b.ticketNo";

      //Get a connection handle for Oracle NoSQL Database Cloud Service
      private async static Task<NoSQLClient> getconnection_cloud()
      {
         // replace the region and compartment place holders with actual values
         var client =  new NoSQLClient(new NoSQLConfig
         {
            Region = <your_region_identifier>,
            Compartment = "<your_compartment_ocid"
         });
         return client;
      }
      //Get a connection handle for onPremise data store
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
      // Create a table
      private static async Task createTable(NoSQLClient client, string query_stmt, string reg_table)
      {
         if (reg_table == "true") {
            var tableResult = await client.ExecuteTableDDLAsync(query_stmt,
                                 new TableDDLOptions{
                                        TableLimits = new TableLimits(20, 20, 1)
                                 });
            await tableResult.WaitForCompletionAsync();
            Console.WriteLine("  Table {0} is created",tableResult.TableName);
         }
         else {
            var tableResult= await client.ExecuteTableDDLAsync(query_stmt);
            // Wait for the operation completion
            await tableResult.WaitForCompletionAsync();
            Console.WriteLine("  Table {0} is created",
               tableResult.TableName);
         }
      }
      private static async Task fetchData(NoSQLClient client,String querystmt){
         var queryEnumerable = client.GetQueryAsyncEnumerable(querystmt);
         await DoQuery(queryEnumerable);
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
      //replace the place holder for compartment with OCID of your compartment
      public static async Task Main(string[] args)
      {
         try {
            // if using cloud service uncomment the code below,
            // else if using onPremises comment it
            var client = await getconnection_cloud();
            // if using onPremise uncomment the code below,
            // else if using cloud service comment it
            //var client = await getconnection_onPrem();
            Console.WriteLine("Created NoSQLClient instance");

            await createTable(client,regtbl_ddl,"true");
            await createTable(client,childtbl_ddl,"false");
            await createTable(client,desctbl_ddl,"false");
            await client.PutAsync(regTblName, FieldValue.FromJsonString(data1).AsMapValue);
            Console.WriteLine("Added a row to the {0} table",regTblName);
            await client.PutAsync(childTblName, FieldValue.FromJsonString(data2).AsMapValue);
            Console.WriteLine("Added a row to the {0} table",childTblName);
            await client.PutAsync(descTblName, FieldValue.FromJsonString(data3).AsMapValue);
            Console.WriteLine("Added a row to the {0} table",descTblName);
            Console.WriteLine("Fetching data using Left Outer Joins: ");
            await fetchData(client,stmt_loj);
            Console.WriteLine("Fetching data using NESTED TABLES: ");
            await fetchData(client,stmt_nt);
            Console.WriteLine("Fetching data using Inner Joins: ");
            await fetchData(client,stmt_ij);
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
