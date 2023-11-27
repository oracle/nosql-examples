// Copyright (c) 2020, 2023 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package main

import (
	"fmt"
	"time"

	"github.com/oracle/nosql-go-sdk/nosqldb"
	"github.com/oracle/nosql-go-sdk/nosqldb/auth/iam"
	"github.com/oracle/nosql-go-sdk/nosqldb/common"
	"github.com/oracle/nosql-go-sdk/nosqldb/jsonutil"
	"github.com/oracle/nosql-go-sdk/nosqldb/types"
)

// Creates a client with the supplied configurations.
// This function encapsulates environmental differences and returns a
// client handle to use for data operations.
func createClient_cloud() (*nosqldb.Client, error) {
   var cfg nosqldb.Config
	//replace the placeholder with your region identifier
	region := "<your_region_identifier>"
	//Replace the value of config file location and the ocid of your compartment
	sp, err := iam.NewSignatureProviderFromFile("<location_config_file>","","","<your_compartment_ocid>")
	if err != nil {
		return nil, fmt.Errorf("cannot create a Signature Provider: %v", err)
	}
	cfg = nosqldb.Config{
	        Mode:                  "cloud",
	     	  Region:                common.Region(region),
			  AuthorizationProvider: sp,
	}
   client, err := nosqldb.NewClient(cfg)
	return client, err
}
// Creates a client with the supplied configurations for onPremise database
func createClient_onPrem() (*nosqldb.Client, error) {
	var cfg nosqldb.Config
	//replace the placeholder with the fullname of your host
	endpoint := "http://<hostname>:8080"
	cfg= nosqldb.Config{
      Endpoint: endpoint,
      Mode:     "onprem",
   }
	//If using a secure store pass the username, password of the store to Config
	//cfg := nosqldb.Config{
   //    Mode:     "onprem",
   //    Username: "<username>",
   //    Password: []byte("<password>"),
   // Specify InsecureSkipVerify
   //    HTTPConfig: httputil.HTTPConfig{
   //        InsecureSkipVerify: true,
   //    },
   client, err := nosqldb.NewClient(cfg)
	return client, err
}
// Creates a table
func createTable(client *nosqldb.Client, err error, tableName string, sql_stmt string, reg_table string)(){
   if reg_table == "true"{
      tableReq := &nosqldb.TableRequest{
         Statement: sql_stmt,
         TableLimits: &nosqldb.TableLimits{
            ReadUnits:  20,
   		   WriteUnits: 20,
   		   StorageGB:  1,
         },
      }
      tableRes, err := client.DoTableRequest(tableReq)
      if err != nil {
         fmt.Printf("cannot initiate CREATE TABLE request: %v\n", err)
         return
      }
		// The create table request is asynchronous, wait for table creation to complete.
      _, err = tableRes.WaitForCompletion(client, 60*time.Second, time.Second)
      if err != nil {
         fmt.Printf("Error finishing CREATE TABLE request: %v\n", err)
         return
      }
      fmt.Println("Created table ", tableName)
      return
   }
   tableReq := &nosqldb.TableRequest{
	              Statement: sql_stmt,
   }
   tableRes, err := client.DoTableRequest(tableReq)
   if err != nil {
      fmt.Printf("cannot initiate CREATE TABLE request: %v\n", err)
      return
   }
   // The create table request is asynchronous, wait for table creation to complete.
   _, err = tableRes.WaitForCompletion(client, 60*time.Second, time.Second)
   if err != nil {
      fmt.Printf("Error finishing CREATE TABLE request: %v\n", err)
      return
   }
   fmt.Println("Created table ", tableName)
   return
}
//Add data to table
func addData(client *nosqldb.Client, err error, tableName string, row_data string)(){
   value, err := types.NewMapValueFromJSON(row_data)
   if err != nil {
      fmt.Errorf("failed to create value from JSON: %v", err)
      return
   }
   putReq := &nosqldb.PutRequest{
      TableName: tableName,
      Value:     value,
   }
   putRes, err := client.Put(putReq)
   if err != nil {
      fmt.Errorf("failed to put a row: %v", err)
      return
   }
   fmt.Printf("Put row from JSON: %v\nresult: %v\n", putReq.Value.Map(), (putRes))
}
//fetch data from the table
func fetchData(client *nosqldb.Client, err error, querystmt string)(){
   prepReq := &nosqldb.PrepareRequest{
      Statement: querystmt,
   }
   prepRes, err := client.Prepare(prepReq)
   if err != nil {
      fmt.Printf("Prepare failed: %v\n", err)
      return
   }
   queryReq := &nosqldb.QueryRequest{
      PreparedStatement: &prepRes.PreparedStatement,   }
   var results []*types.MapValue
   for {
      queryRes, err := client.Query(queryReq)
      if err != nil {
         fmt.Printf("Query failed: %v\n", err)
         return
      }
      res, err := queryRes.GetResults()
      if err != nil {
         fmt.Printf("GetResults() failed: %v\n", err)
         return
      }
      results = append(results, res...)
      if queryReq.IsDone() {
         break
      }
   }
   for i, r := range results {
      fmt.Printf("\t%d: %s\n", i+1, jsonutil.AsJSON(r.Map()))
   }
}

func main() {
   //if using cloud service uncomment the line below
   client, err := createClient_cloud()
   //if using onPrem uncomment the line below
   //client, err := createClient_onPrem()
   if err != nil {
      fmt.Printf("cannot create NoSQL client: %v\n", err)
      return
   }
   defer client.Close()
   regTableName := "ticket"
   childTableName := "ticket.bagInfo"
   descTableName := "ticket.bagInfo.flightLegs"

   regtbl_crtstmt := fmt.Sprintf("CREATE TABLE IF NOT EXISTS %s ("+
      "ticketNo LONG," +
      "confNo STRING," +
      "PRIMARY KEY(ticketNo))",regTableName)

   childtbl_crtstmt := fmt.Sprintf("CREATE TABLE IF NOT EXISTS %s ("+
      "id LONG," +
      "tagNum LONG," +
      "routing STRING," +
      "lastActionCode STRING," +
      "lastActionDesc STRING," +
      "lastSeenStation STRING," +
      "lastSeenTimeGmt TIMESTAMP(4)," +
      "bagArrivalDate TIMESTAMP(4)," +
      "PRIMARY KEY(id))",childTableName)

   desctbl_crtstmt := fmt.Sprintf("CREATE TABLE IF NOT EXISTS %s ("+
      "flightNo STRING," +
      "flightDate TIMESTAMP(4)," +
      "fltRouteSrc STRING," +
      "fltRouteDest STRING," +
      "estimatedArrival TIMESTAMP(4)," +
      "actions JSON," +
      "PRIMARY KEY(flightNo))",descTableName)

   regtbl_data :=`{
      "ticketNo": "1762344493810",
      "confNo" : "LE6J4Z"
   }`

   childtbl_data :=`{
      "ticketNo":"1762344493810",
      "id":"79039899165297",
      "tagNum":"17657806255240",
      "routing":"MIA/LAX/MEL",
      "lastActionCode":"OFFLOAD",
      "lastActionDesc":"OFFLOAD",
      "lastSeenStation":"MEL",
      "lastSeenTimeGmt":"2019-02-01T16:13:00Z",
      "bagArrivalDate":"2019-02-01T16:13:00Z"
   }`

   desctbl_data := `{
      "ticketNo":"1762344493810",
      "id":"79039899165297",
      "flightNo":"BM604",
      "flightDate":"2019-02-01T06:00:00Z",
      "fltRouteSrc":"MIA",
      "fltRouteDest":"LAX",
      "estimatedArrival":"2019-02-01T11:00:00Z",
      "actions":"[ {
         "actionAt" : "MIA",
         "actionCode" : "ONLOAD to LAX",
         "actionTime" : "2019-02-01T06:13:00Z"
      }, {
         "actionAt" : "MIA",
         "actionCode" : "BagTag Scan at MIA",
         "actionTime" : "2019-02-01T05:47:00Z"
      }, {
         "actionAt" : "MIA",
         "actionCode" : "Checkin at MIA",
         "actionTime" : "2019-02-01T04:38:00Z"
      } ]"
   }`
   querystmt:= "SELECT * FROM ticket a LEFT OUTER JOIN ticket.bagInfo.flightLegs b ON a.ticketNo=b.ticketNo"
   createTable(client, err,regTableName, regtbl_crtstmt, "true")
   createTable(client, err,childTableName, childtbl_crtstmt, "false")
   createTable(client, err,descTableName, desctbl_crtstmt, "false")
   addData(client, err,regTableName,regtbl_data)
   addData(client, err,childTableName,childtbl_data)
   addData(client, err,descTableName,desctbl_data)
   fetchData(client, err,querystmt)
}
