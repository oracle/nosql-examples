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
   // -----------------------------------------------------------------------
   // Run the example as:
   //
   // dotnet run -f <target framework>
   //
   // where:
   //   - <target framework> is target framework moniker, supported values
   //     are netcoreapp3.1 and net5.0
   // -----------------------------------------------------------------------
   public class Regions
   {
      private const string Usage =
            "Usage: dotnet run -f <target framework> [-- <config file>]";
      private const string TableName = "stream_acct";

      public static async Task Main(string[] args)
      {
         try {
            var client = await getconnection_onPrem();
            Console.WriteLine("Created NoSQLClient instance");
            await createRegion(client);
            await createTabInRegion(client);
            await dropTabInRegion(client);
            await dropRegion(client);
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
      private async static Task<NoSQLClient> getconnection_onPrem()
      {
         //replace the placeholder with your fullname of the host
         var client = new NoSQLClient(new NoSQLConfig
         {
            ServiceType = ServiceType.KVStore,
            Endpoint = "http://<hostname>:8080"
         });
         return client;
      }

      private static async Task createRegion(NoSQLClient client)
      {
         // Create a remote region
         var sql =
                $@"CREATE REGION LON";
         var adminResult = await client.ExecuteAdminAsync(sql);
         // Wait for the operation completion
         await adminResult.WaitForCompletionAsync();
         Console.WriteLine("  Created remote REGION LON");
         // Create a local region
         var sql1 =
                $@"SET LOCAL REGION FRA";
         var adminResult1 = await client.ExecuteAdminAsync(sql1);
         // Wait for the operation completion
         await adminResult1.WaitForCompletionAsync();
         Console.WriteLine("  Created local REGION FRA");
      }

      private static async Task createTabInRegion(NoSQLClient client)
      {
         // Create a table
         var sql =
            $@"CREATE TABLE IF NOT EXISTS {TableName}(acct_Id INTEGER,
                                                          profile_name STRING,
                                                          account_expiry TIMESTAMP(1),
                                                          acct_data JSON,
                                                          primary key(acct_Id)) IN REGIONS FRA";

         Console.WriteLine("\nCreate table {0}", TableName);
         var tableResult = await client.ExecuteTableDDLAsync(sql,
            new TableDDLOptions
            {
               TableLimits = new TableLimits(20, 20, 1)
            });
         // Wait for the operation completion
         await tableResult.WaitForCompletionAsync();
         Console.WriteLine("  Table {0} is created",
             tableResult.TableName);
         Console.WriteLine("  Table state: {0}", tableResult.TableState);
      }

      private static async Task dropTabInRegion(NoSQLClient client)
      {
         var sql =
             $@"DROP TABLE {TableName}";
         var tableResult = await client.ExecuteTableDDLAsync(sql);
         // Wait for the operation completion
         await tableResult.WaitForCompletionAsync();
         Console.WriteLine("  Table {0} is dropped",
             tableResult.TableName);
      }

      private static async Task dropRegion(NoSQLClient client)
      {
         var sql = $@"DROP REGION LON";
         var adminResult = await client.ExecuteAdminAsync(sql);
         // Wait for the operation completion
         await adminResult.WaitForCompletionAsync();
         Console.WriteLine("  Dropped region LON");
      }
   }
}
