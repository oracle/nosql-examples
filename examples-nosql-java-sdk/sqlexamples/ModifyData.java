/*Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
import java.io.File;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.DeleteResult;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.ops.QueryIterableResult;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;

public class ModifyData {
    /* Name of your table */
   final static String tableName = "stream_acct";

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
  //row 2
   final static String acct2 ="{"+
   "\"acct_Id\":2,"+
   "\"profile_name\":\"Adwi\","+
   "\"account_expiry\":\"2023-10-31\","+
   "\"acct_data\": " +
      "{\"firstName\": \"Adelaide\","+
      "\"lastName\": \"Willard\","+
      "\"country\": \"France\","+
      "\"contentStreamed\": [{"+
         "\"showName\" : \"Bienvenu\","+
         "\"showId\" : 15,"+
         "\"showtype\" : \"tvseries\","+
         "\"genres\" : [\"comedy\", \"french\"],"+
         "\"numSeasons\" : 2,"+
         "\"seriesInfo\": [ {"+
            "\"seasonNum\" : 1,"+
            "\"numEpisodes\" : 2,"+
            "\"episodes\": [ {"+
               "\"episodeID\": 22,"+
               "\"episodeName\" : \"Season 1 episode 1\","+
               "\"lengthMin\": 65,"+
               "\"minWatched\": 65,"+
               "\"date\" : \"2022-04-18\""+
            "},"+
            "{"+
               "\"episodeID\": 32,"+
               "\"lengthMin\": 60,"+
               "\"episodeName\" : \"Season 1 episode 2\","+
               "\"minWatched\": 60,"+
               "\"date\" : \"2022-04-18\""+
            "}]"+
         "},"+
         "{"+
            "\"seasonNum\": 2,"+
            "\"numEpisodes\" :3,"+
            "\"episodes\": [{"+
               "\"episodeID\": 42,"+
               "\"episodeName\" : \"Season 2 episode 1\","+
               "\"lengthMin\": 50,"+
               "\"minWatched\": 50,"+
               "\"date\" : \"2022-04-25\""+
            "}"+
           "]"+
         "}"+
         "]"+
      "}"+
   "]}}";
   //row 3
   final static String acct3 ="{"+
   "\"acct_Id\":3,"+
   "\"profile_name\":\"Dee\","+
   "\"account_expiry\":\"2023-11-28\","+
   "\"acct_data\": " +
      "{\"firstName\": \"Dierdre\","+
      "\"lastName\": \"Amador\","+
      "\"country\": \"USA\","+
      "\"contentStreamed\": [{"+
         "\"showName\" : \"Bienvenu\","+
         "\"showId\" : 15,"+
         "\"showtype\" : \"tvseries\","+
         "\"genres\" : [\"comedy\", \"french\"],"+
         "\"numSeasons\" : 2,"+
         "\"seriesInfo\": [ {"+
            "\"seasonNum\" : 1,"+
            "\"numEpisodes\" : 2,"+
            "\"episodes\": [ {"+
               "\"episodeID\": 23,"+
               "\"episodeName\" : \"Season 1 episode 1\","+
               "\"lengthMin\": 45,"+
               "\"minWatched\": 40,"+
               "\"date\" : \"2022-08-18\""+
            "},"+
            "{"+
               "\"episodeID\": 33,"+
               "\"lengthMin\": 60,"+
               "\"episodeName\" : \"Season 1 episode 2\","+
               "\"minWatched\": 50,"+
               "\"date\" : \"2022-08-19\""+
            "}]"+
         "},"+
         "{"+
            "\"seasonNum\": 2,"+
            "\"numEpisodes\" : 3,"+
            "\"episodes\": [{"+
               "\"episodeID\": 43,"+
               "\"episodeName\" : \"Season 2 episode 1\","+
               "\"lengthMin\": 50,"+
               "\"minWatched\": 50,"+
               "\"date\" : \"2022-08-25\""+
            "},"+
            "{"+
               "\"episodeID\": 53,"+
               "\"episodeName\" : \"Season 2 episode 2\","+
               "\"lengthMin\": 45,"+
               "\"minWatched\": 30,"+
               "\"date\" : \"2022-08-27\""+
            "}"+
            "]"+
         "}"+
         "]"+
      "},"+
      "{"+
         "\"showName\": \"Dane\","+
         "\"showId\": 16,"+
         "\"showtype\": \"tvseries\","+
         "\"genres\" : [\"comedy\", \"drama\",\"danish\"],"+
         "\"numSeasons\" : 2,"+
         "\"seriesInfo\": ["+
         "{"+
            "\"seasonNum\" : 1,"+
            "\"numEpisodes\" : 2,"+
            "\"episodes\": ["+
            "{"+
               "\"episodeID\": 24,"+
               "\"episodeName\" : \"Bonjour\","+
               "\"lengthMin\": 45,"+
               "\"minWatched\": 45,"+
               "\"date\" : \"2022-06-07\""+
            "},"+
            "{"+
               "\"episodeID\": 34,"+
               "\"episodeName\" : \"Merci\","+
               "\"lengthMin\": 42,"+
               "\"minWatched\": 42,"+
               "\"date\" : \"2022-06-08\""+
            "}"+
            "]"+
         "}"+
         "]"+
      "}"+
   "]}}";

   static FieldValue newvalue = FieldValue.createFromJson(acct1,null);
   static FieldValue newvalue1 = FieldValue.createFromJson(acct2,null);
   static FieldValue newvalue2 = FieldValue.createFromJson(acct3,null);

   public static void main(String[] args) throws Exception {
      /* UNCOMMENT the lines of code below if you are using Oracle NoSQL Database Cloud service. Leave the lines commented if you are using onPremise database
      Add the appropriate values of your region and compartment OCID
      String region ="<your_region_identifier>";
      String compId ="<ocid_of_your_compartment>";
      handle = generateNoSQLHandleCloud(region,compId); */

      /* UNCOMMENT the 2 lines of code below if you are using onPremise Oracle NoSQL Database. Leave the lines commented if you are using NoSQL Database Cloud Service
      give appropriate value of your endpoint for the onPremise kvstore.
      String kvstore_endpoint ="http://<your_hostname>:8080";
      handle = generateNoSQLHandleonPrem(kvstore_endpoint); */
      try {
         createTable(handle);
         writeRows(handle, (MapValue)newvalue);
         writeRows(handle, (MapValue)newvalue1);
         writeRows(handle, (MapValue)newvalue2);
         String upsert_row = "UPSERT INTO stream_acct VALUES("+
         "1,"+
         "\"AP\","+
         "\"2023-10-18\","+
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
                  "\"lengthMin\": 70,"+
                  "\"minWatched\": 70,"+
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
                  "\"lengthMin\": 40,"+
                  "\"minWatched\": 40,"+
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
         "]}) RETURNING *";
         System.out.println("Upsert data ");
         upsertRows(handle,upsert_row);
         /* update non-JSON data*/
         String upd_stmt ="UPDATE stream_acct SET account_expiry=\"2023-12-28T00:00:00.0Z\" WHERE acct_Id=3";
         updateRows(handle,upd_stmt);
         /* update JSON data and add a node*/
         String upd_json_addnode="UPDATE stream_acct acct1 ADD acct1.acct_data.contentStreamed.seriesInfo[1].episodes {\"date\" : \"2022-04-26\","+
            "\"episodeID\" : 43,"+
            "\"episodeName\" : \"Season 2 episode 2\","+
            "\"lengthMin\" : 45,"+
            "\"minWatched\" : 45} WHERE acct_Id=2 RETURNING *";
         updateRows(handle,upd_json_addnode);
         /* update JSON data and remove a node*/
         String upd_json_delnode="UPDATE stream_acct acct1 REMOVE acct1.acct_data.contentStreamed.seriesInfo[1].episodes[1] WHERE acct_Id=2 RETURNING *";
         updateRows(handle,upd_json_delnode);
         /*delete a single row*/
         MapValue m1= new MapValue();
         m1.put("acct_Id",1);
         delRow(handle,m1);
         String del_stmt ="DELETE FROM stream_acct acct1 WHERE acct1.acct_data.firstName=\"Adelaide\" AND acct1.acct_data.lastName=\"Willard\"";
         /*delete rows based on a filter condition*/
         deleteRows(handle,del_stmt);
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

   /**
     * Add a row of data
     */
   private static void writeRows(NoSQLHandle handle, MapValue value) throws Exception {
      PutRequest putRequest =
            new PutRequest().setValue(value).setTableName(tableName);
      PutResult putResult = handle.put(putRequest);
      if (putResult.getVersion() != null) {
         System.out.println("Wrote " + value);
      } else {
         System.out.println("Put failed");
      }
   }
   /*Update data*/
   private static void updateRows(NoSQLHandle handle,String sqlstmt) throws Exception {
      QueryRequest queryRequest = new QueryRequest().setStatement(sqlstmt);
      handle.query(queryRequest);
      System.out.println("Updated table " + tableName);
   }
   /*Upsert data*/
   private static void upsertRows(NoSQLHandle handle,String sqlstmt) throws Exception {
      try (
         QueryRequest queryRequest = new QueryRequest().setStatement(sqlstmt);
         QueryIterableResult results = handle.queryIterable(queryRequest)){
         for (MapValue res : results) {
            System.out.println("\t" + res);
         }
      }
   }
   /*delete row based on primary KEY*/
   private static void delRow(NoSQLHandle handle, MapValue m1) throws Exception {
      DeleteRequest delRequest = new DeleteRequest().setKey(m1).setTableName(tableName);
      DeleteResult del = handle.delete(delRequest);
      if (del.getSuccess()) {
         System.out.println("Delete succeed");
      }
      else {
         System.out.println("Delete failed");
      }
   }
   /*delete rows based on a filter condition*/
   private static void deleteRows(NoSQLHandle handle, String sqlstmt) throws Exception {
      QueryRequest queryRequest = new QueryRequest().setStatement(sqlstmt);
      handle.query(queryRequest);
      System.out.println("Deleted row(s) from table " + tableName);
   }
}
