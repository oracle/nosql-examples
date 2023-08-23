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
    public class CreateTable
    {
        private const string Usage =
            "Usage: dotnet run -f <target framework> [-- <config file>]";
        private const string TableName = "stream_acct";

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

        private static async Task createTable(NoSQLClient client)
        {
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

            // Wait for the operation completion
            await tableResult.WaitForCompletionAsync();
            Console.WriteLine("  Created table: ",tableResult.TableName);  
        }
   }
}
