// Copyright (c) 2023, 2024 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at
// https://oss.oracle.com/licenses/upl/
package main

import (
	"fmt"
	"time"

	"github.com/oracle/nosql-go-sdk/nosqldb"
	"github.com/oracle/nosql-go-sdk/nosqldb/auth/iam"
	"github.com/oracle/nosql-go-sdk/nosqldb/common"
	"github.com/oracle/nosql-go-sdk/nosqldb/types"
)

// Creates a client with the supplied configurations.
// This function encapsulates environmental differences and returns a
// client handle to use for data operations.
func createClient_cloud() (*nosqldb.Client, error) {
	var cfg nosqldb.Config
	// replace the placeholder with your actual region identifier
	region := "<your_region_identifier>"
	// Replace the placeholders with the actual value of config file location
	// and the ocid of your compartment
	sp, err := iam.NewSignatureProviderFromFile("<location_config_file>", "", "", "<your_compartment_ocid>")
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
	// replace the placeholder with the fullname of your host
	endpoint := "http://<hostname>:8080"
	cfg = nosqldb.Config{
		Endpoint: endpoint,
		Mode:     "onprem",
	}
	// If using a secure store, uncomment the lines below and pass the username,
	// password of the store to Config
	// cfg := nosqldb.Config{
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
func createTable(client *nosqldb.Client, err error, tableName string)(){
	stmt := fmt.Sprintf("CREATE TABLE IF NOT EXISTS %s ("+
		"ticketNo LONG," +
		"fullName STRING," +
		"gender STRING," +
		"contactPhone STRING," +
		"confNo STRING ," +
		"bagInfo JSON, " +
		"PRIMARY KEY(ticketNo))",tableName)
	tableReq := &nosqldb.TableRequest{
		   Statement: stmt,
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
	// The create table request is asynchronous, wait for table creation
	// to complete.
	_, err = tableRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing CREATE TABLE request: %v\n", err)
		return
	}
	fmt.Println("Created table ", tableName)
	return
}
//Add data to table
func insertData(client *nosqldb.Client, err error, tableName string, value1 *types.MapValue )(){
	putReq := &nosqldb.PutRequest{
		TableName: tableName,
		Value:     value1,
	}
	putRes, err := client.Put(putReq)
	if err != nil {
		fmt.Printf("failed to put single row: %v\n", err)
		return
	}
	fmt.Printf("Put row: %v\nresult: %v\n", putReq.Value.Map(), putRes)
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
	tableName := "BaggageInfo"
   createTable(client, err,tableName)
	//adding data
	value, err := types.NewMapValueFromJSON(`{
		"ticketNo":"1762376407826",
      "fullName":"Dierdre Amador",
      "gender":"M",
      "contactPhone":"165-742-5715",
      "confNo":"ZG8Z5N",
      "bagInfo":[ {
         "id" : "7903989918469",
         "tagNum" : "17657806240229",
         "routing" : "JFK/MAD",
         "lastActionCode" : "OFFLOAD",
         "lastActionDesc" : "OFFLOAD",
         "lastSeenStation" : "MAD",
         "flightLegs" : [ {
            "flightNo" : "BM495",
            "flightDate" : "2019-03-07T07:00:00Z",
            "fltRouteSrc" : "JFK",
            "fltRouteDest" : "MAD",
            "estimatedArrival" : "2019-03-07T14:00:00Z",
            "actions" : [ {
               "actionAt" : "MAD",
               "actionCode" : "Offload to Carousel at MAD",
               "actionTime" : "2019-03-07T13:54:00Z"
            }, {
               "actionAt" : "JFK",
               "actionCode" : "ONLOAD to MAD",
               "actionTime" : "2019-03-07T07:00:00Z"
            }, {
               "actionAt" : "JFK",
               "actionCode" : "BagTag Scan at JFK",
               "actionTime" : "2019-03-07T06:53:00Z"
            }, {
               "actionAt" : "JFK",
               "actionCode" : "Checkin at JFK",
               "actionTime" : "2019-03-07T05:03:00Z"
            } ]
         } ],
         "lastSeenTimeGmt" : "2019-03-07T13:51:00Z",
         "bagArrivalDate" : "2019-03-07T13:51:00Z"
      } ]
	}`)
	insertData(client, err,tableName,value)
	value1, err := types.NewMapValueFromJSON(`{
	   "ticketNo":"1762344493810",
      "fullName":"Adam Phillips",
      "gender":"M",
      "contactPhone":"893-324-1064",
      "confNo":"LE6J4Z",
      "bagInfo":[ {
         "id" : "79039899165297",
         "tagNum" : "17657806255240",
         "routing" : "MIA/LAX/MEL",
         "lastActionCode" : "OFFLOAD",
         "lastActionDesc" : "OFFLOAD",
         "lastSeenStation" : "MEL",
         "flightLegs" : [ {
            "flightNo" : "BM604",
            "flightDate" : "2019-02-01T06:00:00Z",
            "fltRouteSrc" : "MIA",
            "fltRouteDest" : "LAX",
            "estimatedArrival" : "2019-02-01T11:00:00Z",
            "actions" : [ {
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
            } ]
         }, {
            "flightNo" : "BM667",
            "flightDate" : "2019-02-01T06:13:00Z",
            "fltRouteSrc" : "LAX",
            "fltRouteDest" : "MEL",
            "estimatedArrival" : "2019-02-01T16:15:00Z",
            "actions" : [ {
               "actionAt" : "MEL",
               "actionCode" : "Offload to Carousel at MEL",
               "actionTime" : "2019-02-01T16:15:00Z"
            }, {
               "actionAt" : "LAX",
               "actionCode" : "ONLOAD to MEL",
               "actionTime" : "2019-02-01T15:35:00Z"
            }, {
               "actionAt" : "LAX",
               "actionCode" : "OFFLOAD from LAX",
               "actionTime" : "2019-02-01T15:18:00Z"
            } ]
         } ],
         "lastSeenTimeGmt" : "2019-02-01T16:13:00Z",
         "bagArrivalDate" : "2019-02-01T16:13:00Z"
      } ]
	}`)
	insertData(client, err,tableName,value1)
	value2, err := types.NewMapValueFromJSON(`{
		"ticketNo":"1762341772625",
      "fullName":"Gerard Greene",
      "gender":"M",
      "contactPhone":"395-837-3772",
      "confNo":"MC0E7R",
      "bagInfo":[ {
         "id" : "79039899152842",
         "tagNum" : "1765780626568",
         "routing" : "SFO/IST/ATH/JTR",
         "lastActionCode" : "OFFLOAD",
         "lastActionDesc" : "OFFLOAD",
         "lastSeenStation" : "JTR",
         "flightLegs" : [ {
            "flightNo" : "BM318",
            "flightDate" : "2019-03-07T04:00:00Z",
            "fltRouteSrc" : "SFO",
            "fltRouteDest" : "IST",
            "estimatedArrival" : "2019-03-07T17:00:00Z",
            "actions" : [ {
               "actionAt" : "SFO",
               "actionCode" : "ONLOAD to IST",
               "actionTime" : "2019-03-07T04:08:00Z"
            }, {
               "actionAt" : "SFO",
               "actionCode" : "BagTag Scan at SFO",
               "actionTime" : "2019-03-07T03:53:00Z"
            }, {
               "actionAt" : "SFO",
               "actionCode" : "Checkin at SFO",
               "actionTime" : "2019-03-07T02:20:00Z"
            } ]
         }, {
            "flightNo" : "BM696",
            "flightDate" : "2019-03-07T05:08:00Z",
            "fltRouteSrc" : "IST",
            "fltRouteDest" : "ATH",
            "estimatedArrival" : "2019-03-08T04:10:00Z",
            "actions" : [ {
               "actionAt" : "IST",
               "actionCode" : "ONLOAD to ATH",
               "actionTime" : "2019-03-08T04:55:00Z"
            }, {
               "actionAt" : "IST",
               "actionCode" : "BagTag Scan at IST",
               "actionTime" : "2019-03-08T04:34:00Z"
            }, {
               "actionAt" : "IST",
               "actionCode" : "OFFLOAD from IST",
               "actionTime" : "2019-03-08T04:47:00Z"
            } ]
         }, {
            "flightNo" : "BM665",
            "flightDate" : "2019-03-07T04:08:00Z",
            "fltRouteSrc" : "ATH",
            "fltRouteDest" : "JTR",
            "estimatedArrival" : "2019-03-07T16:10:00Z",
            "actions" : [ {
               "actionAt" : "JTR",
               "actionCode" : "Offload to Carousel at JTR",
               "actionTime" : "2019-03-07T16:09:00Z"
            }, {
               "actionAt" : "ATH",
               "actionCode" : "ONLOAD to JTR",
               "actionTime" : "2019-03-07T15:51:00Z"
            }, {
               "actionAt" : "ATH",
               "actionCode" : "OFFLOAD from ATH",
               "actionTime" : "2019-03-07T15:43:00Z"
            } ]
         } ],
         "lastSeenTimeGmt" : "2019-03-07T16:01:00Z",
         "bagArrivalDate" : "2019-03-07T16:01:00Z"
      } ]
	}`)
	insertData(client, err,tableName,value2)
	value3, err := types.NewMapValueFromJSON(`{
		"ticketNo":"1762320369957",
      "fullName":"Lorenzo Phil",
      "gender":"M",
      "contactPhone":"364-610-4444",
      "confNo":"QI3V6Q",
      "bagInfo":[ {
         "id" : "79039899187755",
         "tagNum" : "17657806240001",
         "routing" : "SFO/IST/ATH/JTR",
         "lastActionCode" : "OFFLOAD",
         "lastActionDesc" : "OFFLOAD",
         "lastSeenStation" : "JTR",
         "flightLegs" : [ {
            "flightNo" : "BM318",
            "flightDate" : "2019-03-12T03:00:00Z",
            "fltRouteSrc" : "SFO",
            "fltRouteDest" : "IST",
            "estimatedArrival" : "2019-03-12T16:00:00Z",
            "actions" : [ {
               "actionAt" : "SFO",
               "actionCode" : "ONLOAD to IST",
               "actionTime" : "2019-03-12T03:11:00Z"
            }, {
               "actionAt" : "SFO",
               "actionCode" : "BagTag Scan at SFO",
               "actionTime" : "2019-03-12T02:49:00Z"
            }, {
               "actionAt" : "SFO",
               "actionCode" : "Checkin at SFO",
               "actionTime" : "2019-03-12T01:50:00Z"
            } ]
         }, {
            "flightNo" : "BM696",
            "flightDate" : "2019-03-12T04:11:00Z",
            "fltRouteSrc" : "IST",
            "fltRouteDest" : "ATH",
            "estimatedArrival" : "2019-03-13T03:14:00Z",
            "actions" : [ {
               "actionAt" : "IST",
               "actionCode" : "ONLOAD to ATH",
               "actionTime" : "2019-03-13T04:10:00Z"
            }, {
               "actionAt" : "IST",
               "actionCode" : "BagTag Scan at IST",
               "actionTime" : "2019-03-13T03:56:00Z"
            }, {
               "actionAt" : "IST",
               "actionCode" : "OFFLOAD from IST",
               "actionTime" : "2019-03-13T03:59:00Z"
            } ]
         }, {
            "flightNo" : "BM665",
            "flightDate" : "2019-03-12T03:11:00Z",
            "fltRouteSrc" : "ATH",
            "fltRouteDest" : "JTR",
            "estimatedArrival" : "2019-03-12T15:12:00Z",
            "actions" : [ {
               "actionAt" : "JTR",
               "actionCode" : "Offload to Carousel at JTR",
               "actionTime" : "2019-03-12T15:06:00Z"
            }, {
               "actionAt" : "ATH",
               "actionCode" : "ONLOAD to JTR",
               "actionTime" : "2019-03-12T14:16:00Z"
            }, {
               "actionAt" : "ATH",
               "actionCode" : "OFFLOAD from ATH",
               "actionTime" : "2019-03-12T14:13:00Z"
            } ]
         } ],
         "lastSeenTimeGmt" : "2019-03-12T15:05:00Z",
         "bagArrivalDate" : "2019-03-12T15:05:00Z"
      },
      {
         "id" : "79039899197755",
         "tagNum" : "17657806340001",
         "routing" : "SFO/IST/ATH/JTR",
         "lastActionCode" : "OFFLOAD",
         "lastActionDesc" : "OFFLOAD",
         "lastSeenStation" : "JTR",
         "flightLegs" : [ {
            "flightNo" : "BM318",
            "flightDate" : "2019-03-12T03:00:00Z",
            "fltRouteSrc" : "SFO",
            "fltRouteDest" : "IST",
            "estimatedArrival" : "2019-03-12T16:40:00Z",
            "actions" : [ {
               "actionAt" : "SFO",
               "actionCode" : "ONLOAD to IST",
               "actionTime" : "2019-03-12T03:14:00Z"
            }, {
               "actionAt" : "SFO",
               "actionCode" : "BagTag Scan at SFO",
               "actionTime" : "2019-03-12T02:50:00Z"
            }, {
               "actionAt" : "SFO",
               "actionCode" : "Checkin at SFO",
               "actionTime" : "2019-03-12T01:58:00Z"
            } ]
         }, {
            "flightNo" : "BM696",
            "flightDate" : "2019-03-12T04:11:00Z",
            "fltRouteSrc" : "IST",
            "fltRouteDest" : "ATH",
            "estimatedArrival" : "2019-03-13T03:18:00Z",
            "actions" : [ {
               "actionAt" : "IST",
               "actionCode" : "ONLOAD to ATH",
               "actionTime" : "2019-03-13T04:17:00Z"
            }, {
               "actionAt" : "IST",
               "actionCode" : "BagTag Scan at IST",
               "actionTime" : "2019-03-13T03:59:00Z"
            }, {
               "actionAt" : "IST",
               "actionCode" : "OFFLOAD from IST",
               "actionTime" : "2019-03-13T03:48:00Z"
            } ]
         }, {
            "flightNo" : "BM665",
            "flightDate" : "2019-03-12T03:11:00Z",
            "fltRouteSrc" : "ATH",
            "fltRouteDest" : "JTR",
            "estimatedArrival" : "2019-03-12T15:12:00Z",
            "actions" : [ {
               "actionAt" : "JTR",
               "actionCode" : "Offload to Carousel at JTR",
               "actionTime" : "2019-03-12T15:06:00Z"
            }, {
               "actionAt" : "ATH",
               "actionCode" : "ONLOAD to JTR",
               "actionTime" : "2019-03-12T14:16:00Z"
            }, {
               "actionAt" : "ATH",
               "actionCode" : "OFFLOAD from ATH",
               "actionTime" : "2019-03-12T14:23:00Z"
            } ]
         } ],
         "lastSeenTimeGmt" : "2019-03-12T16:05:00Z",
         "bagArrivalDate" : "2019-03-12T16:25:00Z"
      } ]
	}`)
	insertData(client, err,tableName,value3)
	fmt.Printf("Put row succeeded: \n")
}
