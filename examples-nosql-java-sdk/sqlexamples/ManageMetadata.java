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
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.DeleteResult;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.MultiDeleteRequest;
import oracle.nosql.driver.ops.MultiDeleteResult;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.ops.WriteMultipleRequest;
import oracle.nosql.driver.ops.WriteMultipleResult;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.values.FieldValue;

public class ManageMetadata {
    /* Name of your table */
   final static String tableName = "stream_acct";
   static NoSQLHandle handle;
   final static String acct1 ="{"+
   "\"acct_Id\":1,"+
   "\"profile_name\":\"AP\","+
   "\"account_expiry\":\"2023-10-18\","+
   "\"acct_data\": " +
      "{\"firstName\": \"Adam\","+
      "\"lastName\": \"Phillips\","+
      "\"country\": \"Germany\","+
      "\"contentStreamed\": [{"+
         "\"showName\" : \"At the Ranch\","+
         "\"showId\" : 26,"+
         "\"showtype\" : \"tvseries\","+
         "\"genres\" : [\"action\", \"crime\", \"spanish\"],"+
         "\"numSeasons\" : 4,"+
         "\"seriesInfo\": [ {"+
            "\"seasonNum\" : 1,"+
            "\"numEpisodes\" : 2,"+
            "\"episodes\": [ {"+
               "\"episodeID\": 20,"+
				   "\"episodeName\" : \"Season 1 episode 1\","+
               "\"lengthMin\": 85,"+
               "\"minWatched\": 85,"+
               "\"date\" : \"2022-04-18\""+
            "},"+
            "{"+
               "\"episodeID\": 30,"+
               "\"lengthMin\": 60,"+
				   "\"episodeName\" : \"Season 1 episode 2\","+
               "\"minWatched\": 60,"+
               "\"date\" : \"2022-04-18\""+
            "}]"+
         "},"+
         "{"+
            "\"seasonNum\": 2,"+
            "\"numEpisodes\" : 2,"+
            "\"episodes\": [{"+
               "\"episodeID\": 40,"+
				   "\"episodeName\" : \"Season 2 episode 1\","+
               "\"lengthMin\": 50,"+
               "\"minWatched\": 50,"+
               "\"date\" : \"2022-04-25\""+
            "},"+
            "{"+
               "\"episodeID\": 50,"+
				   "\"episodeName\" : \"Season 2 episode 2\","+
               "\"lengthMin\": 45,"+
               "\"minWatched\": 30,"+
               "\"date\" : \"2022-04-27\""+
            "}"+
         "]"+
         "},"+
         "{"+
            "\"seasonNum\": 3,"+
            "\"numEpisodes\" : 2,"+
            "\"episodes\": [{"+
               "\"episodeID\": 60,"+
	             "\"episodeName\" : \"Season 3 episode 1\","+
               "\"lengthMin\": 50,"+
               "\"minWatched\": 50,"+
               "\"date\" : \"2022-04-25\""+
            "},"+
            "{"+
               "\"episodeID\": 70,"+
		 		       "\"episodeName\" : \"Season 3 episode 2\","+
               "\"lengthMin\": 45,"+
               "\"minWatched\": 30,"+
               "\"date\" : \"2022-04-27\""+
            "}"+
         "]"+
         "}"+
      "]"+
      "},"+
      "{"+
         "\"showName\": \"Bienvenu\","+
         "\"showId\": 15,"+
         "\"showtype\": \"tvseries\","+
         "\"genres\" : [\"comedy\", \"french\"],"+
         "\"numSeasons\" : 2,"+
         "\"seriesInfo\": ["+
         "{"+
            "\"seasonNum\" : 1,"+
            "\"numEpisodes\" : 2,"+
            "\"episodes\": ["+
            "{"+
               "\"episodeID\": 20,"+
				   "\"episodeName\" : \"Bonjour\","+
               "\"lengthMin\": 45,"+
               "\"minWatched\": 45,"+
               "\"date\" : \"2022-03-07\""+
            "},"+
            "{"+
               "\"episodeID\": 30,"+
				   "\"episodeName\" : \"Merci\","+
               "\"lengthMin\": 42,"+
               "\"minWatched\": 42,"+
               "\"date\" : \"2022-03-08\""+
            "}"+
         "]"+
         "}"+
      "]"+
      "}"+
   "]}}";
  
   final static String rm1="{\"modified_by\" : \"John Doe\",\"reviewed_in\" : \"Q1\",\"update_reason\" : \"Account details updated\"}";

   static FieldValue newvalue = FieldValue.createFromJson(acct1,null);

   public static void main(String[] args) throws Exception {
      /* UNCOMMENT the lines of code below if you are using Oracle NoSQL
      *  Database Cloud service. Leave the lines commented if you are using
      * onPremise database
      * Add the appropriate values of your region and compartment OCID
       String region ="<your_region_identifier>";
       String compId ="<ocid_of_your_compartment>";
       handle = generateNoSQLHandleCloud(region,compId); */

      /* UNCOMMENT the 2 lines of code below if you are using onPremise Oracle
      * NoSQL Database.
      * Leave the lines commented if you are using Cloud Service
      * Give appropriate value of your endpoint for the onPremise kvstore.
        String kvstore_endpoint ="http://<your_hostname>:8080";
        handle = generateNoSQLHandleonPrem(kvstore_endpoint);
		*/
      
      try {
         
         createTable(handle);
         writeRowWithMetadata(handle, (MapValue)newvalue, rm1);

         /*The below function call is to fetch row-metadata using GET API based on Primary key */
         readRowMetadata(handle,1 );
         

         /*The below function call is to update the row and row-metadata using a Query */
         updateRowViaQuery(handle);
         

         /*The below function call is to fetch row metadata via query */
         readAllRowMetadataViaQuery(handle);
         

         /*The below function call is to delete a row with row-metadata based on Primary key */
         deleteRowWithMetadata(handle, 1);
         

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
       /* If using a secure store, uncomment the line below and pass the
       *  username, password of the store to StoreAccessTokenProvider
       */
       /* config.setAuthorizationProvider(new StoreAccessTokenProvider(username, password)); */
       NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
       return handle;
   }

   /* Create a NoSQL handle to access the cloud service */
   private static NoSQLHandle generateNoSQLHandleCloud(String region, String compId) throws Exception {
      SignatureProvider ap = new SignatureProvider();
      NoSQLHandleConfig config = new NoSQLHandleConfig(region, ap);
      /* set your default compartment */
      config.setDefaultCompartment(compId);
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }

   /*
   * Create a simple table and set your desired table capacity
   */
   private static void createTable(NoSQLHandle handle) throws Exception {
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
   * Add Row with row-metadata in the table
   */
  private static void writeRowWithMetadata(NoSQLHandle handle, MapValue value, String rowMetadata) throws Exception {
   PutRequest putRequest = new PutRequest()
       .setValue(value)
       .setTableName(tableName)
       .setRowMetadata(rowMetadata);

   PutResult putResult = handle.put(putRequest);
   if (putResult.getVersion() != null) {
       System.out.println("Wrote: " + value);
   } else {
       System.out.println("Put failed");
      }
   }

   /*
    * Fetch a row-metadata using GET API
    */
   private static void readRowMetadata(NoSQLHandle handle, int acctId) throws Exception {
        GetRequest gr = new GetRequest()
            .setKey(new MapValue().put("acct_Id", acctId))
            .setTableName(tableName);

        GetResult gres = handle.get(gr);
        String rm = gres.getRowMetadata();

        if (rm != null) {
            System.out.println("Row metadata for acct_Id " + acctId + ": " + rm);
        } else {
            System.out.println("No metadata found for acct_Id " + acctId);
        }
      }

   /*Delete a row and set row metadata using Delete API */   
    private static void deleteRowWithMetadata(NoSQLHandle handle, int acctId) throws Exception {
        MapValue key = new MapValue().put("acct_Id", acctId);

        DeleteRequest delRequest = new DeleteRequest()
            .setKey(key)                                      
            .setTableName(tableName)                          
            .setReturnRow(true)                               // Ask to return the deleted row and its metadata
            .setRowMetadata("{\"deletedBy\": \"Hannah Lee\",\"reason\":\"User requested account removal\"}");    // Metadata attached to deletion 

        DeleteResult del = handle.delete(delRequest);
        String rm = del.getExistingRowMetadata();             

        if (rm != null) {
            System.out.println("Deleted acct_Id " + acctId + " previous row metadata: " + rm);
        } else {
            System.out.println("Row with acct_Id " + acctId + " did not exist.");
        }
      }
      
      /*Update query and row metadata using QueryRequest API */
      private static void updateRowViaQuery(NoSQLHandle handle) throws Exception {
      // UPDATE acct_Id 1 with new profile_name and metadata using SQL query
         String updateQuery = "UPDATE " + tableName + " SET profile_name = 'Amelia Pure' WHERE acct_Id = 1";
         String rowMetadata = "{\"modifiedBy\": \"Priya Kaul\", \"updateReason\": \"full name\"}";

         QueryRequest qr = new QueryRequest()
             .setStatement(updateQuery)
             .setRowMetadata(rowMetadata);

         handle.query(qr); 
         System.out.println("Updated acct_Id 1 via query with row metadata: " + rowMetadata);
      }

      /*Fetch row metadata via query function using QueryRequest API */
      private static void readAllRowMetadataViaQuery(NoSQLHandle handle) throws Exception {
         // SELECT query to retrieve only row metadata for all rows
         String query = "SELECT row_metadata($t) as rmt FROM " + tableName + " $t";
 
         QueryRequest queryReq = new QueryRequest()
             .setStatement(query);
       do{
          QueryResult qRes = handle.query(queryReq);
          System.out.println("All row-metadata from table:");
          
          for (MapValue v : qRes.getResults()) {
             FieldValue rowMetadataFieldValue = v.get("rmt"); 
             System.out.println(rowMetadataFieldValue);
         }
       }
       while (!queryReq.isDone());
     }


}
