/* Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
*/
import java.io.File;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.DeleteResult;
import oracle.nosql.driver.ops.MultiDeleteRequest;
import oracle.nosql.driver.ops.MultiDeleteResult;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.ops.QueryIterableResult;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;

public class MultiDataOps {
    /* Name of your table */
   final static String tableName = "examplesAddress";
   static NoSQLHandle handle;

   public static void main(String[] args) throws Exception {
      /* UNCOMMENT the lines of code below if you are using Oracle NoSQL
      * Database Cloud service.
      * Leave the lines commented if you are using onPremise database
      * Add the appropriate values of your region and compartment OCID
      String region ="<your_region_identifier>";
      String compId ="<ocid_of_your_compartment>";
      handle = generateNoSQLHandleCloud(region,compId); */

      /* UNCOMMENT the 2 lines of code below if you are using onPremise Oracle
      * NoSQL Database.
      * Leave the lines commented if you are using NoSQL Database Cloud Service
      * Give appropriate value of your endpoint for the onPremise kvstore.
      String kvstore_endpoint ="http://<your_hostname>:8080";
      handle = generateNoSQLHandleonPrem(kvstore_endpoint); */
      try {
         createTable(handle);
         writeRows(handle,1,"10 Red Street","Apt 3",1234567);
         writeRows(handle,2,"2 Green Street","Suite 9",1234567);
         writeRows(handle,3,"5 Blue Ave","Floor 2",1234567);
         writeRows(handle,4,"9 Yellow Boulevard","Apt 3",87654321);
         String sqlstmt_allrows="select * from examplesAddress";
         fetchRowCnt(handle,sqlstmt_allrows);
         delMulRows(handle,1234567);
         fetchRowCnt(handle,sqlstmt_allrows);
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
      * password of the store to StoreAccessTokenProvider
      */
      /* config.setAuthorizationProvider(new StoreAccessTokenProvider(username, password)); */
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }

   /* Create a NoSQL handle to access the cloud service */
   private static NoSQLHandle generateNoSQLHandleCloud(String region, String compId) throws Exception {
      SignatureProvider ap = new SignatureProvider();
      NoSQLHandleConfig config = new NoSQLHandleConfig(region, ap);
      // set your default compartment
      config.setDefaultCompartment(compId);
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }
  /**
   * Create a simple table and set your desired table capacity
   */
   private static void createTable(NoSQLHandle handle) throws Exception {
      String createTableDDL =
          "CREATE TABLE IF NOT EXISTS " + tableName +
          "(id INTEGER, " +
          " address_line1 STRING, " +
          " address_line2 STRING, " +
          " pin INTEGER, " +
          " PRIMARY KEY(SHARD(pin), id))";

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

    /**
     * Add a row of data
     */
   private static void writeRows(NoSQLHandle handle, int idval,String add1, String aa2, int pinval) throws Exception {
      MapValue value = new MapValue().
          put("id", idval).put("pin",pinval).
          put("address_line1", "add1").
          put("address_line2", "add2");

      PutRequest putRequest = new PutRequest()
          .setValue(value)
          .setTableName(tableName);

      PutResult putRes = handle.put(putRequest);
      if (putRes.getVersion() != null) {
         System.out.println("Wrote a row ");
      } else {
         System.out.println("Put failed");
      }
   }
   //Fetch rows from the table
   private static void fetchRowCnt(NoSQLHandle handle,String sqlstmt) throws Exception {
         QueryRequest queryRequest = new QueryRequest().setStatement(sqlstmt);
         QueryResult results = handle.query(queryRequest);
         System.out.println("Number of rows in table: " + results.getResults().size());

   }
   //Delete multiple rows from the table
   private static void delMulRows(NoSQLHandle handle,int pinval) throws Exception {

      MapValue key = new MapValue().put("pin", 1234567);
      MultiDeleteRequest multiDelRequest = new MultiDeleteRequest()
          .setKey(key)
          .setTableName(tableName);

      MultiDeleteResult mRes = handle.multiDelete(multiDelRequest);
      System.out.println("MultiDelete result = " + mRes);

   }
}
