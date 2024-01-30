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
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.ops.QueryIterableResult;


public class SQLExpressions {
    /* Name of your table */
   final static String tableName = "BaggageInfo";
   static NoSQLHandle handle;
   final static String bag1 = "{"+
      "\"ticketNo\" : \"1762376407826\","+
      "\"fullName\" : \"Dierdre Amador\","+
      "\"gender\" : \"M\","+
      "\"contactPhone\" : \"165-742-5715\","+
      "\"confNo\" : \"ZG8Z5N\","+
      "\"bagInfo\" : [ {"+
         "\"id\" : \"7903989918469\","+
         "\"tagNum\" : \"17657806240229\","+
         "\"routing\" : \"JFK/MAD\","+
         "\"lastActionCode\" : \"OFFLOAD\","+
         "\"lastActionDesc\" : \"OFFLOAD\","+
         "\"lastSeenStation\" : \"MAD\","+
         "\"flightLegs\" : [ {"+
            "\"flightNo\" : \"BM495\","+
            "\"flightDate\" : \"2019-03-07T07:00:00Z\","+
            "\"fltRouteSrc\" : \"JFK\","+
            "\"fltRouteDest\" : \"MAD\","+
            "\"estimatedArrival\" : \"2019-03-07T14:00:00Z\","+
            "\"actions\" : [ {"+
               "\"actionAt\" : \"MAD\","+
               "\"actionCode\" : \"Offload to Carousel at MAD\","+
               "\"actionTime\" : \"2019-03-07T13:54:00Z\""+
            "}, {"+
               "\"actionAt\" : \"JFK\","+
               "\"actionCode\" : \"ONLOAD to MAD\","+
               "\"actionTime\" : \"2019-03-07T07:00:00Z\""+
            "}, {"+
               "\"actionAt\" : \"JFK\","+
               "\"actionCode\" : \"BagTag Scan at JFK\","+
               "\"actionTime\" : \"2019-03-07T06:53:00Z\""+
            "}, {"+
               "\"actionAt\" : \"JFK\","+
               "\"actionCode\" : \"Checkin at JFK\","+
               "\"actionTime\" : \"2019-03-07T05:03:00Z\""+
            "} ]"+
         "} ],"+
         "\"lastSeenTimeGmt\" : \"2019-03-07T13:51:00Z\","+
         "\"bagArrivalDate\" : \"2019-03-07T13:51:00Z\""+
      "} ]"+
   "}";

   final static String bag2 = "{"+
      "\"ticketNo\" : \"1762344493810\","+
      "\"fullName\" : \"Dierdre Amador\","+
      "\"gender\" : \"M\","+
      "\"contactPhone\" : \"893-324-1064\","+
      "\"confNo\" : \"LE6J4Z\","+
      "\"bagInfo\" : [ {"+
         "\"id\" : \"79039899165297\","+
         "\"tagNum\" : \"17657806255240\","+
         "\"routing\" : \"MIA/LAX/MEL\","+
         "\"lastActionCode\" : \"OFFLOAD\","+
         "\"lastActionDesc\" : \"OFFLOAD\","+
         "\"lastSeenStation\" : \"MEL\","+
         "\"flightLegs\" : [ {"+
            "\"flightNo\" : \"BM604\","+
            "\"flightDate\" : \"2019-02-01T06:00:00Z\","+
            "\"fltRouteSrc\" : \"MIA\","+
            "\"fltRouteDest\" : \"LAX\","+
            "\"estimatedArrival\" : \"2019-02-01T11:00:00Z\","+
            "\"actions\" : [ {"+
               "\"actionAt\" : \"MIA\","+
               "\"actionCode\" : \"ONLOAD to LAX\","+
               "\"actionTime\" : \"2019-02-01T06:13:00Z\""+
            "}, {"+
               "\"actionAt\" : \"MIA\","+
               "\"actionCode\" : \"BagTag Scan at MIA\","+
               "\"actionTime\" : \"2019-02-01T05:47:00Z\""+
            "}, {"+
               "\"actionAt\" : \"MIA\","+
               "\"actionCode\" : \"Checkin at MIA\","+
               "\"actionTime\" : \"2019-02-01T04:38:00Z\""+
            "} ]"+
         "}, {"+
            "\"flightNo\" : \"BM667\","+
            "\"flightDate\" : \"2019-02-01T06:13:00Z\","+
            "\"fltRouteSrc\" : \"LAX\","+
            "\"fltRouteDest\" : \"MEL\","+
            "\"estimatedArrival\" : \"2019-02-01T16:15:00Z\","+
            "\"actions\" : [ {"+
               "\"actionAt\" : \"MEL\","+
               "\"actionCode\" : \"Offload to Carousel at MEL\","+
               "\"actionTime\" : \"2019-02-01T16:15:00Z\""+
            "}, {"+
               "\"actionAt\" : \"LAX\","+
               "\"actionCode\" : \"ONLOAD to MEL\","+
               "\"actionTime\" : \"2019-02-01T15:35:00Z\""+
            "}, {"+
               "\"actionAt\" : \"LAX\","+
               "\"actionCode\" : \"OFFLOAD from LAX\","+
               "\"actionTime\" : \"2019-02-01T15:18:00Z\""+
            "} ]"+
         "} ],"+
         "\"lastSeenTimeGmt\" : \"2019-02-01T16:13:00Z\","+
         "\"bagArrivalDate\" : \"2019-02-01T16:13:00Z\""+
      "} ]"+
   "}";
   final static String bag3 = "{"+
      "\"ticketNo\" : \"1762341772625\","+
      "\"fullName\" : \"Gerard Greene\","+
      "\"gender\" : \"M\","+
      "\"contactPhone\" : \"395-837-3772\","+
      "\"confNo\" : \"MC0E7R\","+
      "\"bagInfo\" : [ {"+
         "\"id\" : \"79039899152842\","+
         "\"tagNum\" : \"1765780626568\","+
         "\"routing\" : \"SFO/IST/ATH/JTR\","+
         "\"lastActionCode\" : \"OFFLOAD\","+
         "\"lastActionDesc\" : \"OFFLOAD\","+
         "\"lastSeenStation\" : \"JTR\","+
         "\"flightLegs\" : [ {"+
            "\"flightNo\" : \"BM318\","+
            "\"flightDate\" : \"2019-03-07T04:00:00Z\","+
            "\"fltRouteSrc\" : \"SFO\","+
            "\"fltRouteDest\" : \"IST\","+
            "\"estimatedArrival\" : \"2019-03-07T17:00:00Z\","+
            "\"actions\" : [ {"+
               "\"actionAt\" : \"SFO\","+
               "\"actionCode\" : \"ONLOAD to IST\","+
               "\"actionTime\" : \"2019-03-07T04:08:00Z\""+
            "}, {"+
               "\"actionAt\" : \"SFO\","+
               "\"actionCode\" : \"BagTag Scan at SFO\","+
               "\"actionTime\" : \"2019-03-07T03:53:00Z\""+
            "}, {"+
               "\"actionAt\" : \"SFO\","+
               "\"actionCode\" : \"Checkin at SFO\","+
               "\"actionTime\" : \"2019-03-07T02:20:00Z\""+
            "} ]"+
         "}, {"+
            "\"flightNo\" : \"BM696\","+
            "\"flightDate\" : \"2019-03-07T05:08:00Z\","+
            "\"fltRouteSrc\" : \"IST\","+
            "\"fltRouteDest\" : \"ATH\","+
            "\"estimatedArrival\" : \"2019-03-08T04:10:00Z\","+
            "\"actions\" : [ {"+
               "\"actionAt\" : \"IST\","+
               "\"actionCode\" : \"ONLOAD to ATH\","+
               "\"actionTime\" : \"2019-03-08T04:55:00Z\""+
            "}, {"+
               "\"actionAt\" : \"IST\","+
               "\"actionCode\" : \"BagTag Scan at IST\","+
               "\"actionTime\" : \"2019-03-08T04:34:00Z\""+
            "}, {"+
               "\"actionAt\" : \"IST\","+
               "\"actionCode\" : \"OFFLOAD from IST\","+
               "\"actionTime\" : \"2019-03-08T04:47:00Z\""+
            "} ]"+
         "}, {"+
            "\"flightNo\" : \"BM665\","+
            "\"flightDate\" : \"2019-03-07T04:08:00Z\","+
            "\"fltRouteSrc\" : \"ATH\","+
            "\"fltRouteDest\" : \"JTR\","+
            "\"estimatedArrival\" : \"2019-03-07T16:10:00Z\","+
            "\"actions\" : [ {"+
               "\"actionAt\" : \"JTR\","+
               "\"actionCode\" : \"Offload to Carousel at JTR\","+
               "\"actionTime\" : \"2019-03-07T16:09:00Z\""+
            "}, {"+
               "\"actionAt\" : \"ATH\","+
               "\"actionCode\" : \"ONLOAD to JTR\","+
               "\"actionTime\" : \"2019-03-07T15:51:00Z\""+
            "}, {"+
               "\"actionAt\" : \"ATH\","+
               "\"actionCode\" : \"OFFLOAD from ATH\","+
               "\"actionTime\" : \"2019-03-07T15:43:00Z\""+
            "} ]"+
         "} ],"+
         "\"lastSeenTimeGmt\" : \"2019-03-07T16:01:00Z\","+
         "\"bagArrivalDate\" : \"2019-03-07T16:01:00Z\""+
      "} ]"+
   "}";

   final static String bag4 = "{"+
      "\"ticketNo\" : \"1762320369957\","+
      "\"fullName\" : \"Lorenzo Phil\","+
      "\"gender\" : \"M\","+
      "\"contactPhone\" : \"364-610-4444\","+
      "\"confNo\" : \"QI3V6Q\","+
      "\"bagInfo\" : [ {"+
      "\"id\" : \"79039899187755\","+
          "\"tagNum\" : \"17657806240001\","+
          "\"routing\" : \"SFO/IST/ATH/JTR\","+
          "\"lastActionCode\" : \"OFFLOAD\","+
          "\"lastActionDesc\" : \"OFFLOAD\","+
          "\"lastSeenStation\" : \"JTR\","+
          "\"flightLegs\" : [ {"+
            "\"flightNo\" : \"BM318\","+
            "\"flightDate\" : \"2019-03-12T03:00:00Z\","+
            "\"fltRouteSrc\" : \"SFO\","+
            "\"fltRouteDest\" : \"IST\","+
            "\"estimatedArrival\" : \"2019-03-12T16:00:00Z\","+
            "\"actions\" : [ {"+
              "\"actionAt\" : \"SFO\","+
              "\"actionCode\" : \"ONLOAD to IST\","+
              "\"actionTime\" : \"2019-03-12T03:11:00Z\""+
            "}, {"+
              "\"actionAt\" : \"SFO\","+
              "\"actionCode\" : \"BagTag Scan at SFO\","+
              "\"actionTime\" : \"2019-03-12T02:49:00Z\""+
            "}, {"+
              "\"actionAt\" : \"SFO\","+
              "\"actionCode\" : \"Checkin at SFO\","+
              "\"actionTime\" : \"2019-03-12T01:50:00Z\""+
            "} ]"+
          "}, {"+
            "\"flightNo\" : \"BM696\","+
            "\"flightDate\" : \"2019-03-12T04:11:00Z\","+
            "\"fltRouteSrc\" : \"IST\","+
            "\"fltRouteDest\" : \"ATH\","+
            "\"estimatedArrival\" : \"2019-03-13T03:14:00Z\","+
            "\"actions\" : [ {"+
              "\"actionAt\" : \"IST\","+
              "\"actionCode\" : \"ONLOAD to ATH\","+
              "\"actionTime\" : \"2019-03-13T04:10:00Z\""+
            "}, {"+
              "\"actionAt\" : \"IST\","+
              "\"actionCode\" : \"BagTag Scan at IST\","+
              "\"actionTime\" : \"2019-03-13T03:56:00Z\""+
            "}, {"+
              "\"actionAt\" : \"IST\","+
              "\"actionCode\" : \"OFFLOAD from IST\","+
              "\"actionTime\" : \"2019-03-13T03:59:00Z\""+
            "} ]"+
          "}, {"+
            "\"flightNo\" : \"BM665\","+
            "\"flightDate\" : \"2019-03-12T03:11:00Z\","+
            "\"fltRouteSrc\" : \"ATH\","+
            "\"fltRouteDest\" : \"JTR\","+
            "\"estimatedArrival\" : \"2019-03-12T15:12:00Z\","+
            "\"actions\" : [ {"+
              "\"actionAt\" : \"JTR\","+
              "\"actionCode\" : \"Offload to Carousel at JTR\","+
              "\"actionTime\" : \"2019-03-12T15:06:00Z\""+
            "}, {"+
              "\"actionAt\" : \"ATH\","+
              "\"actionCode\" : \"ONLOAD to JTR\","+
              "\"actionTime\" : \"2019-03-12T14:16:00Z\""+
            "}, {"+
              "\"actionAt\" : \"ATH\","+
              "\"actionCode\" : \"OFFLOAD from ATH\","+
              "\"actionTime\" : \"2019-03-12T14:13:00Z\""+
            "} ]"+
         "} ],"+
          "\"lastSeenTimeGmt\" : \"2019-03-12T15:05:00Z\","+
          "\"bagArrivalDate\" : \"2019-03-12T15:05:00Z\""+
        "},"+
        "{"+
          "\"id\" : \"79039899197755\","+
          "\"tagNum\" : \"17657806340001\","+
          "\"routing\" : \"SFO/IST/ATH/JTR\","+
          "\"lastActionCode\" : \"OFFLOAD\","+
          "\"lastActionDesc\" : \"OFFLOAD\","+
          "\"lastSeenStation\" : \"JTR\","+
          "\"flightLegs\" : [ {"+
            "\"flightNo\" : \"BM318\","+
            "\"flightDate\" : \"2019-03-12T03:00:00Z\","+
            "\"fltRouteSrc\" : \"SFO\","+
            "\"fltRouteDest\" : \"IST\","+
            "\"estimatedArrival\" : \"2019-03-12T16:40:00Z\","+
            "\"actions\" : [ {"+
              "\"actionAt\" : \"SFO\","+
              "\"actionCode\" : \"ONLOAD to IST\","+
              "\"actionTime\" : \"2019-03-12T03:14:00Z\""+
            "}, {"+
              "\"actionAt\" : \"SFO\","+
              "\"actionCode\" : \"BagTag Scan at SFO\","+
              "\"actionTime\" : \"2019-03-12T02:50:00Z\""+
            "}, {"+
              "\"actionAt\" : \"SFO\","+
              "\"actionCode\" : \"Checkin at SFO\","+
              "\"actionTime\" : \"2019-03-12T01:58:00Z\""+
            "} ]"+
          "}, {"+
            "\"flightNo\" : \"BM696\","+
            "\"flightDate\" : \"2019-03-12T04:11:00Z\","+
            "\"fltRouteSrc\" : \"IST\","+
            "\"fltRouteDest\" : \"ATH\","+
            "\"estimatedArrival\" : \"2019-03-13T03:18:00Z\","+
            "\"actions\" : [ {"+
              "\"actionAt\" : \"IST\","+
              "\"actionCode\" : \"ONLOAD to ATH\","+
              "\"actionTime\" : \"2019-03-13T04:17:00Z\""+
            "}, {"+
              "\"actionAt\" : \"IST\","+
              "\"actionCode\" : \"BagTag Scan at IST\","+
              "\"actionTime\" : \"2019-03-13T03:59:00Z\""+
            "}, {"+
              "\"actionAt\" : \"IST\","+
              "\"actionCode\" : \"OFFLOAD from IST\","+
              "\"actionTime\" : \"2019-03-13T03:48:00Z\""+
            "} ]"+
          "}, {"+
            "\"flightNo\" : \"BM665\","+
            "\"flightDate\" : \"2019-03-12T03:11:00Z\","+
            "\"fltRouteSrc\" : \"ATH\","+
            "\"fltRouteDest\" : \"JTR\","+
            "\"estimatedArrival\" : \"2019-03-12T15:12:00Z\","+
            "\"actions\" : [ {"+
              "\"actionAt\" : \"JTR\","+
              "\"actionCode\" : \"Offload to Carousel at JTR\","+
              "\"actionTime\" : \"2019-03-12T15:06:00Z\""+
            "}, {"+
              "\"actionAt\" : \"ATH\","+
              "\"actionCode\" : \"ONLOAD to JTR\","+
              "\"actionTime\" : \"2019-03-12T14:16:00Z\""+
            "}, {"+
              "\"actionAt\" : \"ATH\","+
              "\"actionCode\" : \"OFFLOAD from ATH\","+
              "\"actionTime\" : \"2019-03-12T14:23:00Z\""+
            "} ]"+
          "} ],"+
          "\"lastSeenTimeGmt\" : \"2019-03-12T16:05:00Z\","+
          "\"bagArrivalDate\" : \"2019-03-12T16:25:00Z\""+
       "} ]"+
    "}";



   static FieldValue val1 = FieldValue.createFromJson(bag1,null);
   static FieldValue val2 = FieldValue.createFromJson(bag2,null);
   static FieldValue val3 = FieldValue.createFromJson(bag3,null);
   static FieldValue val4 = FieldValue.createFromJson(bag4,null);

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
         writeRows(handle, (MapValue)val1);
         writeRows(handle, (MapValue)val2);
         writeRows(handle, (MapValue)val3);
         writeRows(handle, (MapValue)val4);
         String paran_expr="SELECT fullName, bag.bagInfo.tagNum, bag.bagInfo.routing, "+
         "bag.bagInfo[].flightLegs[].fltRouteDest FROM BaggageInfo bag WHERE "+
         "bag.bagInfo.flightLegs[].fltRouteSrc=any \"SFO\" AND "+
         "(bag.bagInfo[].flightLegs[].fltRouteDest=any \"ATH\" OR "+
         "bag.bagInfo[].flightLegs[].fltRouteDest=any \"JTR\" )";
         System.out.println("Using Paranthesized expression ");
         fetchRows(handle,paran_expr);
         String case_expr="SELECT fullName,"+
         "CASE WHEN NOT exists bag.bagInfo.flightLegs[0] "+
        "THEN \"you have no bag info\" "+
        "WHEN NOT exists bag.bagInfo.flightLegs[1] "+
        "THEN \"you have one hop\" "+
        "WHEN NOT exists bag.bagInfo.flightLegs[2] "+
        "THEN \"you have two hops.\" "+
        "ELSE \"you have three hops.\" "+
        "END AS NUMBER_HOPS "+
        "FROM BaggageInfo bag WHERE ticketNo=1762341772625";
         System.out.println("Using Case Expression ");
         fetchRows(handle,case_expr);
         String seq_trn_expr="SELECT seq_transform(l.bagInfo[],"+
                              "seq_transform("+
                                "$sq1.flightLegs[],"+
                                "seq_transform("+
                                  "$sq2.actions[],"+
                                  "{"+
                                   "\"at\" : $sq3.actionAt,"+
                                   "\"action\" : $sq3.actionCode,"+
                                   "\"flightNo\" : $sq2.flightNo,"+
                                   "\"tagNum\" : $sq1.tagNum"+
                                  "}"+
                               ")"+
                              ")"+
                           ") AS actions FROM baggageInfo l WHERE ticketNo=1762376407826";
         System.out.println("Using Sequence Transform Expressions ");
         fetchRows(handle,seq_trn_expr);

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
      String createTableDDL = "CREATE TABLE IF NOT EXISTS " + tableName +
                                                              "(ticketNo LONG," +
                                                              "fullName STRING," +
                                                              "gender STRING ," +
                                                              "contactPhone STRING ," +
                                                              "confNo STRING ," +
                                                              "bagInfo JSON, " +
                                                              "PRIMARY KEY(ticketNo))";

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
   * Add a Row in the table
   */
   private static void writeRows(NoSQLHandle handle, MapValue value) throws Exception {
      PutRequest putRequest =
            new PutRequest().setValue(value).setTableName(tableName);
      PutResult putResult = handle.put(putRequest);
   }
   //Fetch rows from the table
   private static void fetchRows(NoSQLHandle handle,String sqlstmt) throws Exception {

      try (
         QueryRequest queryRequest = new QueryRequest().setStatement(sqlstmt);
         QueryIterableResult results = handle.queryIterable(queryRequest)){
         for (MapValue res : results) {
            System.out.println("\t" + res);
         }
      }
   }
}
