/*Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
import java.io.File;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.ops.QueryIterableResult;

public class TableJoins{
   /* Name of your tables */
   final static String tableName = "ticket";
   final static String childTableName = "bagInfo";
   final static String fullChildTableName = "ticket.bagInfo";
   final static String descTableName = "flightLegs";
   final static String fullDescTableName = "ticket.bagInfo.flightLegs";

   public static void main(String[] args) throws Exception {
      /*Uncomment the lines of code below if you are using Oracle NoSQL Database Cloud service
      Add the appropriate values of your region and compartment OCID*/
      /*String region ="<your_region_identifier>";
      String compId ="<ocid_of_your_compartment>";
      NoSQLHandle handle = generateNoSQLHandleCloud(region,compId);*/

      /*Uncomment the 2 lines of code below if you are using onPremise Oracle NoSQL Database
      give appropriate value of your endpoint for the onPremise kvstore.*/
      /*String kvstore_endpoint ="http://phoenix71003.dev3sub1phx.databasede3phx.oraclevcn.com:8080";
      NoSQLHandle handle = generateNoSQLHandleonPrem(kvstore_endpoint);*/
      try {
         String crttab_stmt = "CREATE TABLE IF NOT EXISTS " + tableName +
                                                                 "(ticketNo LONG," +
                                                                 "confNo STRING," +
                                                                 "PRIMARY KEY(ticketNo))";
         createTable(handle,crttab_stmt,true,tableName);
         String crtchildtab_stmt = "CREATE TABLE IF NOT EXISTS " + fullChildTableName +
                                                                 "(id LONG," +
                                                                 "tagNum LONG," +
                                                                 "routing STRING," +
                                                                 "lastActionCode STRING," +
                                                                 "lastActionDesc STRING," +
                                                                 "lastSeenStation STRING," +
                                                                 "lastSeenTimeGmt TIMESTAMP(4)," +
                                                                 "bagArrivalDate TIMESTAMP(4)," +
                                                                 "PRIMARY KEY(id))";
         createTable(handle,crtchildtab_stmt,false,childTableName);
         String crtdesctab_stmt = "CREATE TABLE IF NOT EXISTS " + fullDescTableName +
                                                              "(flightNo STRING," +
                                                              "flightDate TIMESTAMP(4)," +
                                                              "fltRouteSrc STRING," +
                                                              "fltRouteDest STRING," +
                                                              "estimatedArrival TIMESTAMP(4)," +
                                                              "actions JSON," +
                                                              "PRIMARY KEY(flightNo))";
         createTable(handle,crtdesctab_stmt,false,descTableName);
         String data1 ="{\"ticketNo\": \"1762344493810\","+
                         "\"confNo\" : \"LE6J4Z\"}";
         writeRowData(handle,"ticket",data1);
         String data2 ="{\"ticketNo\":\"1762344493810\","+
                          "\"id\":\"79039899165297\","+
                          "\"tagNum\":\"17657806255240\","+
                          "\"routing\":\"MIA/LAX/MEL\","+
                          "\"lastActionCode\":\"OFFLOAD\","+
                          "\"lastActionDesc\":\"OFFLOAD\","+
                          "\"lastSeenStation\":\"MEL\","+
                          "\"lastSeenTimeGmt\":\"2019-02-01T16:13:00Z\","+
                          "\"bagArrivalDate\":\"2019-02-01T16:13:00Z\""+
         "}";
         writeRowData(handle,"ticket.bagInfo",data2);
         String data3 ="{\"ticketNo\":\"1762344493810\","+
                        "\"id\":\"79039899165297\","+
                        "\"flightNo\":\"BM604\","+
                        "\"flightDate\":\"2019-02-01T06:00:00Z\","+
                        "\"fltRouteSrc\":\"MIA\","+
                        "\"fltRouteDest\":\"LAX\","+
                        "\"estimatedArrival\":\"2019-02-01T11:00:00Z\","+
                        "\"actions\":[ { "+
                           "\"actionAt\" : \"MIA\","+
                           "\"actionCode\" : \"ONLOAD to LAX\","+
                           "\"actionTime\" : \"2019-02-01T06:13:00Z\""+
                           "}, {" +
                           "\"actionAt\" : \"MIA\","+
                           "\"actionCode\" : \"BagTag Scan at MIA\","+
                           "\"actionTime\" : \"2019-02-01T05:47:00Z\""+
                           "}, {" +
                           "\"actionAt\" : \"MIA\","+
                           "\"actionCode\" : \"Checkin at MIA\","+
                           "\"actionTime\" : \"2019-02-01T04:38:00Z\""+
                        "} ]"+
                     "}";
         writeRowData(handle,"ticket.bagInfo.flightLegs",data3);
         fetchRows(handle);
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
      /* If using a secure store uncomment the line below and pass the username, password of the store to StoreAccessTokenProvider*/
      /*config.setAuthorizationProvider(new StoreAccessTokenProvider(username, password));*/
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }
   /* Create a NoSQL handle to access the cloud service */
   private static NoSQLHandle generateNoSQLHandleCloud(String region, String compId) throws Exception {
      SignatureProvider ap = new SignatureProvider();
      NoSQLHandleConfig config = new NoSQLHandleConfig(region, ap);
      /* set your default compartment*/
      config.setDefaultCompartment(compId);
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }
   /**
     * Creates a table and sets your desired table capacity
     */
   private static void createTable(NoSQLHandle handle, String sql_stmt,boolean reg_table, String tableName) throws Exception {
      TableRequest treq = new TableRequest().setStatement(sql_stmt);
      if (reg_table== true){
         TableLimits limits = new TableLimits(20, 20, 1);
         treq = new TableRequest().setStatement(sql_stmt).setTableLimits(limits);
      }
      TableResult tres = handle.tableRequest(treq);
      /* The request is async,
       * so wait for the table to become active.
       */
      tres.waitForCompletion(handle, 60000, /* wait 60 sec */
            1000); /* delay ms for poll */
      System.out.println("Table " + tableName + " is active");
   }

   /**
     * Make a row in the table and write it
     */
   private static void writeRowData(NoSQLHandle handle,String table_name,String row_data) throws Exception {
      PutRequest putRequest =
         new PutRequest().setValueFromJson(row_data,null).setTableName(table_name);
      PutResult putResult = handle.put(putRequest);
      if (putResult.getVersion() != null) {
         System.out.println("Wrote " + row_data);
      } else {
         System.out.println("Put failed");
      }
   }
   /* fetch rows based on joins*/
   private static void fetchRows(NoSQLHandle handle) throws Exception {

      try (
         QueryRequest queryRequest = new QueryRequest().setStatement("SELECT * FROM ticket a LEFT OUTER JOIN ticket.bagInfo.flightLegs b ON a.ticketNo=b.ticketNo");
         QueryIterableResult results = handle.queryIterable(queryRequest)) {
            System.out.println("Query results:");
            for (MapValue res : results) {
               System.out.println("\t" + res);
            }
         }
   }
}
