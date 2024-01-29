/* Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
*/
import java.io.File;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;

public class Indexes{
   /* Name of your table */
   final static String tableName = "stream_acct";
   static NoSQLHandle handle;
   public static void main(String[] args) throws Exception {
      /* UNCOMMENT the lines of code below if you are using Oracle NoSQL
      * Database Cloud service.
      * Leave the lines commented if you are using onPremise database
      * Add the appropriate values of your region and compartment OCID
      String region ="<your_region_identifier>";
      String compId ="<ocid_of_your_compartment>";
      handle = generateNoSQLHandleCloud(region,compId);
      */

      /* UNCOMMENT the 2 lines of code below if you are using onPremise Oracle
      * NoSQL Database.
      * Leave the lines commented if you are using NoSQL Database Cloud Service
      * Give appropriate value of your endpoint for the onPremise kvstore.
      String kvstore_endpoint ="http://<your_hostname>:8080";
      handle = generateNoSQLHandleonPrem(kvstore_endpoint);
      */
      try {
         createTab(handle);
         crtIndex(handle);
         dropIndex(handle);
      } catch (Exception e) {
         System.err.print(e);
      } finally {
         handle.close();
      }
   }
   /* Create a NoSQL handle to access the onPremise Oracle NoSQL database */
   private static NoSQLHandle generateNoSQLHandleonPrem(String kvstore_endpoint) throws Exception {
      NoSQLHandleConfig config = new NoSQLHandleConfig(kvstore_endpoint);
      config.setAuthorizationProvider(new StoreAccessTokenProvider());
      /* If using a secure store, uncomment the line below and pass username,
      *  password of the store to StoreAccessTokenProvider
      */
      /* config.setAuthorizationProvider(new StoreAccessTokenProvider(username, password)); */
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }

   /* Create a NoSQL handle to access the cloud service */
   private static NoSQLHandle generateNoSQLHandleCloud(String region, String compId) throws Exception {
      SignatureProvider ap = new SignatureProvider();
      /* Create a NoSQL handle to access the cloud service */
      NoSQLHandleConfig config = new NoSQLHandleConfig(region, ap);
      /* set your mycompartment*/
      config.setDefaultCompartment(compId);
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }

   /*
    * Create a table and set the desired table capacity
   */
   private static void createTab(NoSQLHandle handle) throws Exception {
      String createTableDDL = "CREATE TABLE IF NOT EXISTS " + tableName +
                                                              "(acct_Id INTEGER," +
                                                              "profile_name STRING," +
                                                              "account_expiry TIMESTAMP(1) ," +
                                                              "acct_data JSON, " +
                                                              "PRIMARY KEY(acct_Id))";

      TableLimits limits = new TableLimits(20, 20, 1);
      TableRequest treq = new TableRequest()
            .setStatement(createTableDDL).setTableLimits(limits);
      TableResult tres = handle.tableRequest(treq);
      /* The request is async,
       * so wait for the table to become active.
       */
      tres.waitForCompletion(handle, 60000, /* wait 60 sec */
            1000); /* delay ms for poll */
      System.out.println("Table " + tableName + " is active");
   }
   /*
   * Create an index acct_episodes in the stream_acct table
   */
   private static void crtIndex(NoSQLHandle handle) throws Exception {
      String createIndexDDL = "CREATE INDEX acct_episodes ON " + tableName +
                                "(acct_data.contentStreamed[].seriesInfo[].episodes[]  AS ANYATOMIC)";

      TableRequest treq = new TableRequest().setStatement(createIndexDDL);
      TableResult tres = handle.tableRequest(treq);
      tres.waitForCompletion(handle, 60000, /* wait 60 sec */
            1000); /* delay ms for poll */
      System.out.println("Index acct_episodes on " + tableName + " is created");
   }
   /* Drop the index acct_episodes */
   private static void dropIndex(NoSQLHandle handle) throws Exception {
      String dropIndexDDL = "DROP INDEX acct_episodes ON " + tableName;
      TableRequest treq = new TableRequest().setStatement(dropIndexDDL);
      TableResult tres = handle.tableRequest(treq);
      tres.waitForCompletion(handle, 60000, /* wait 60 sec */
            1000); /* delay ms for poll */
      System.out.println("Index acct_episodes on " + tableName + " is dropped");
   }
}
