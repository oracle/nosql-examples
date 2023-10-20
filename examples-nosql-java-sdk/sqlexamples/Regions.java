/* Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
import oracle.nosql.driver.ops.SystemResult;
import oracle.nosql.driver.ops.SystemRequest;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;

public class Regions{
   /* Name of your region */
   final static String remRegName = "LON";
   final static String localRegName = "FRA";
   final static String tableName = "stream_acct";

   public static void main(String[] args) throws Exception {
      /* Replace the placeholder below with your full hostname */
      String kvstore_endpoint ="http://<your_hostname>:8080";
      NoSQLHandle handle = generateNoSQLHandleonPrem(kvstore_endpoint);
      try {
         crtRegion(handle);
         crtTabInRegion(handle);
         dropTabInRegion(handle);
         dropRegion(handle,remRegName);
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
      /* If using a secure store pass the username, password of the store to StoreAccessTokenProvider */
      /* config.setAuthorizationProvider(new StoreAccessTokenProvider(username, password)); */
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }
   /* Create a remote region and a local region */
   private static void crtRegion(NoSQLHandle handle) throws Exception {
      /* Create a remote region */
      String createRemRegDDL = "CREATE REGION "+ remRegName;
      SystemRequest sysreq1 = new SystemRequest();
      sysreq1.setStatement(createRemRegDDL.toCharArray());
      SystemResult sysres1 = handle.systemRequest​(sysreq1);
      sysres1.waitForCompletion​(handle, 60000,1000);
      System.out.println(" Remote Region " + remRegName + " is created");
      /* Create a local region */
      String createLocRegDDL = "SET LOCAL REGION "+ localRegName;
      SystemRequest sysreq2 = new SystemRequest();
      sysreq2.setStatement(createLocRegDDL.toCharArray());
      SystemResult sysres2 = handle.systemRequest​(sysreq2);
      sysres2.waitForCompletion​(handle, 60000,1000);
      System.out.println(" Local Region " + localRegName + " is created");
  }
  /*
   * Create a table and add the table in a region
   */
  private static void crtTabInRegion(NoSQLHandle handle) throws Exception {
      String createTableDDL = "CREATE TABLE IF NOT EXISTS " + tableName +
                                                              "(acct_Id INTEGER," +
                                                              "profile_name STRING," +
                                                              "account_expiry TIMESTAMP(1) ," +
                                                              "acct_data JSON, " +
                                                              "PRIMARY KEY(acct_Id)) IN REGIONS FRA";

      TableLimits limits = new TableLimits(20, 20, 1);
      TableRequest treq = new TableRequest()
            .setStatement(createTableDDL).setTableLimits(limits);
      TableResult tres = handle.tableRequest(treq);
        /* The request is async,
         * so wait for the table to become active.
        */
        System.out.println("Table " + tableName + " is active");
   }
   /* Drop a table from a region */
   private static void dropTabInRegion(NoSQLHandle handle) throws Exception {
      String dropTableDDL = "DROP TABLE " + tableName;
      TableRequest treq = new TableRequest().setStatement(dropTableDDL);
      TableResult tres = handle.tableRequest(treq);
      tres.waitForCompletion(handle, 60000, /* wait 60 sec */
          1000); /* delay ms for poll */
      System.out.println("Table " + tableName + " is dropped");
   }
   /* Drop a region */
   private static void dropRegion(NoSQLHandle handle, String regName) throws Exception {
      String dropNSDDL = "DROP REGION " + regName;
      SystemRequest sysreq = new SystemRequest();
      sysreq.setStatement(dropNSDDL.toCharArray());
      SystemResult sysres = handle.systemRequest​(sysreq);
      sysres.waitForCompletion​(handle, 60000,1000);
      System.out.println("Region " + regName + " is dropped");
   }
}
