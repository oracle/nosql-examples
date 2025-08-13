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
import oracle.nosql.driver.ops.WriteMultipleRequest;
import oracle.nosql.driver.ops.WriteMultipleResult;
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.DeleteResult;
import oracle.nosql.driver.ops.MultiDeleteRequest;
import oracle.nosql.driver.ops.MultiDeleteResult;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.ops.QueryIterableResult;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;

public class MultiMetadataOps {
    /* Name of your table */
   final static String tableName = "Emp_Address";
   static NoSQLHandle handle;

   final static String emp1="{ \"id\": 1, \"address_line1\": \"10 Red Street\", \"address_line2\": \"Apt 3\", \"pin\": 1234567 }";
   final static String emp2="{ \"id\": 2, \"address_line1\": \"2 Green Street\", \"address_line2\": \"Suite 9\", \"pin\": 1234567 }";
   final static String emp3="{ \"id\": 3, \"address_line1\": \"5 Blue Ave\", \"address_line2\": \"Floor 2\", \"pin\": 1234567 }";

   final static String rm1="{\"modified_by\" : \"Shreya Sharma\",\"joined_in\" : \"January 2012\",\"update_reason\" : \"Change in address\"}";
   final static String rm2 = "{\"modified_by\" : \"Slvia Sanz\",\"data_source\" : \"individual update\"}";
   final static String rm3 = "{\"modified_by\" : \"Raul Daniels\",\"joined_in\" : \"September 2023\"}";
   final static String rm4 = "{\"modified_by\" : \"Shreya Sharma\",\"joined_in\" : \"May 2004\",\"update_reason\":\"Internal job transfer\"}";

   static FieldValue newvalue = FieldValue.createFromJson(emp1,null);
   static FieldValue newvalue1 = FieldValue.createFromJson(emp2,null);
   static FieldValue newvalue2 = FieldValue.createFromJson(emp3,null);

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
      handle = generateNoSQLHandleonPrem(kvstore_endpoint); 
	  */
      try {
         createTable(handle);
		 
		 /*The below function call is to write multiple rows with row metadata*/
         writeMultipleRows(handle, (MapValue)newvalue, (MapValue)newvalue1, (MapValue)newvalue2, rm1, rm2, rm3);

         writeRows(handle,4,"9 Yellow Boulevard","Apt 3",87654321, rm4);
        
         /*The below function call is to delete multiple rows with row metadata */
         delMulRows(handle,1234567);
         

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
   private static void writeRows(NoSQLHandle handle, int idval,String add1, String aad2, int pinval, String rm4) throws Exception {
      MapValue value = new MapValue().
          put("id", idval).put("pin",pinval).
          put("address_line1", "add1").
          put("address_line2", "add2")
          ;

      PutRequest putRequest = new PutRequest()
          .setValue(value)
          .setTableName(tableName)
          .setRowMetadata(rm4);

      PutResult putRes = handle.put(putRequest);
      if (putRes.getVersion() != null) {
         System.out.println("Wrote a row ");
      } else {
         System.out.println("Put failed");
      }
   }

   /*Add multiple rows with row metadata in the table */
   private static void writeMultipleRows(NoSQLHandle handle, MapValue val1, MapValue val2, MapValue val3, String rm1, String rm2, String rm3 ){
      PutRequest put1 = new PutRequest()
         .setValue(val1)
         .setTableName(tableName)
         .setRowMetadata(rm1);
      PutRequest put2 = new PutRequest()
         .setValue(val2)
         .setTableName(tableName)
         .setRowMetadata(rm2);
      PutRequest put3 = new PutRequest()
         .setValue(val3)
         .setTableName(tableName)
         .setRowMetadata(rm3);

      WriteMultipleRequest wmRequest = new WriteMultipleRequest()
         .add(put1, false)
         .add(put2, false)
         .add(put3, false);

      WriteMultipleResult wmResult = handle.writeMultiple(wmRequest);

      if (wmResult.getSuccess()) {
         System.out.println("Successfully inserted all 3 rows with rowMetadata.");
     } else {
         System.out.println("WriteMultiple failed. Some or all operations may not have succeeded.");
     }
   }

   /*Delete multiple rows and set row metadata*/
   private static void delMulRows(NoSQLHandle handle,int pinval) throws Exception {

      MapValue key = new MapValue().put("pin", 1234567);
      MultiDeleteRequest multiDelRequest = new MultiDeleteRequest()
          .setKey(key)
          .setTableName(tableName)
          .setRowMetadata("{\"deletedBy\": \"Hannah Lee\",\"reason\":\"Employee no longer works in the company\"}");

      MultiDeleteResult mRes = handle.multiDelete(multiDelRequest);

      System.out.println("MultiDelete result = " + mRes);
   }
}