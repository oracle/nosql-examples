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
   //     are netcoreapp3.1 and net5.0
   // -----------------------------------------------------------------------
   public class Namespaces
   {
      private const string Usage =
            "Usage: dotnet run -f <target framework> [-- <config file>]";
      private const string TableName = "stream_acct";

      public static async Task Main(string[] args)
      {
         try {
            var client = await getconnection_onPrem();
            Console.WriteLine("Created NoSQLClient instance");
            await createNS(client);
            await dropNS(client);
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

      private static async Task createNS(NoSQLClient client)
      {
         var sql =
             $@"CREATE NAMESPACE IF NOT EXISTS ns1";
         var adminResult = await client.ExecuteAdminAsync(sql);
         // Wait for the operation completion
         await adminResult.WaitForCompletionAsync();
         Console.WriteLine("  Created namespace ns1");
      }

      private static async Task dropNS(NoSQLClient client)
      {
         var sql =
                $@"DROP NAMESPACE ns1";
         var adminResult = await client.ExecuteAdminAsync(sql);
         // Wait for the operation completion
         await adminResult.WaitForCompletionAsync();
         Console.WriteLine("  Dropped namespace ns1");
      }
   }
}
