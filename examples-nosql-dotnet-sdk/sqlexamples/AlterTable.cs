// Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at
//  https://oss.oracle.com/licenses/upl/

namespace Oracle.NoSQL.SDK.Samples
{
   using System;
   using System.Threading.Tasks;
   using Oracle.NoSQL.SDK;
   // -----------------------------------------------------------------------
   // Run the example as:
   // dotnet run -f <target framework>
   // where:
   //   - <target framework> is target framework moniker, supported values
   //     are netcoreapp5.1 and net7.0
   // -----------------------------------------------------------------------
   public class AlterTable
   {
      private const string Usage =
         "Usage: dotnet run -f <target framework> [-- <config file>]";
      private const string TableName = "stream_acct";

      public static async Task Main(string[] args)
      {
         try {
            // if using cloud service uncomment the code below, else if using onPremises comment it
            var client = await getconnection_cloud();
            // if using onPremise uncomment the code below, else if using cloud service comment it
            // var client = await getconnection_onPrem();
            Console.WriteLine("Created NoSQLClient instance");
            await createTable(client);
            await alterTable(client);
            await dropTable(client);
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
         // replace the place holder for compartment with your region identifier and OCID of your compartment
         var client =  new NoSQLClient(new NoSQLConfig
         {
            Region = <your_region_identifier>,
            Compartment = "<your_compartment_ocid"
         });
         return client;
      }
      // Get a connection handle for onPremise data store
      private async static Task<NoSQLClient> getconnection_onPrem()
      {
         // replace the placeholder with your fullname of the host
         var client = new NoSQLClient(new NoSQLConfig
         {
            ServiceType = ServiceType.KVStore,
            Endpoint = "http://<hostname>:8080"
         });
         return client;
      }
      // Create a table
      private static async Task createTable(NoSQLClient client)
      {
         var sql =
                $@"CREATE TABLE IF NOT EXISTS {TableName}(acct_Id INTEGER,
                                                          profile_name STRING,
                                                          account_expiry TIMESTAMP(1),
                                                          acct_data JSON,
                                                          primary key(acct_Id))";

         Console.WriteLine("\nCreate table {0}", TableName);
         var tableResult = await client.ExecuteTableDDLAsync(sql,
                                 new TableDDLOptions{
                                     TableLimits = new TableLimits(20, 20, 1)
                                 });
         // Wait for the operation completion
         await tableResult.WaitForCompletionAsync();
         Console.WriteLine("  Table {0} is created",
             tableResult.TableName);
         Console.WriteLine("  Table state: {0}", tableResult.TableState);
      }

      private static async Task alterTable(NoSQLClient client)
      {
         var sql =
                $@"ALTER TABLE {TableName}(ADD acctname STRING)";
         var tableResult = await client.ExecuteTableDDLAsync(sql);
         // Wait for the operation completion
         await tableResult.WaitForCompletionAsync();
         Console.WriteLine("  Table {0} is altered",
                tableResult.TableName);
      }

      private static async Task dropTable(NoSQLClient client)
      {
         var sql =
               $@"DROP TABLE {TableName}";
         var tableResult = await client.ExecuteTableDDLAsync(sql);
         // Wait for the operation completion
         await tableResult.WaitForCompletionAsync();
         Console.WriteLine("  Table {0} is dropped",
                tableResult.TableName);
      }
   }
}
