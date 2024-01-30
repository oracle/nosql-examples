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
		"id INTEGER," +
		"address_line1 STRING," +
		"address_line2 STRING," +
		"pin INTEGER ," +
		"PRIMARY KEY(SHARD(pin), id))",tableName)
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
	fmt.Printf("Put row: result: %v\n", putRes)
}
//fetch data from the table
func fetchRowCnt(client *nosqldb.Client, err error, tableName string, querystmt string)(){
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
	fmt.Printf("Number of query results is : %d\n", len(results))

}
//delete multiple rows
func delMulRows(client *nosqldb.Client, err error, tableName string,pinval int)(){
	shardKey := &types.MapValue{}
	shardKey.Put("pin", pinval)
	multiDelReq := &nosqldb.MultiDeleteRequest{
		TableName: tableName,
		Key:       shardKey,
	}
	multiDelRes, err := client.MultiDelete(multiDelReq)
	if err != nil {
		fmt.Printf("failed to delete multiple rows: %v", err)
		return
	}
	fmt.Printf("MultiDelete result=%v\n", multiDelRes)
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
	tableName := "examplesAddress"
	createTable(client, err,tableName)
	//adding data
	value, err := types.NewMapValueFromJSON(`{
		"id":            1,
		"pin":           1234567,
		"address_line1": "10 Red Street",
		"address_line2": "Apt 3"
	}`)
	insertData(client, err,tableName,value)
	value1, err := types.NewMapValueFromJSON(`{
		"id":            2,
		"pin":           1234567,
		"address_line1": "2 Green Street",
		"address_line2": "Street 9"
	}`)
	insertData(client, err,tableName,value1)
	value2, err := types.NewMapValueFromJSON(`{
		"id":            3,
		"pin":           1234567,
		"address_line1": "5 Blue Ave",
		"address_line2": "Floor 2"
	}`)
	insertData(client, err,tableName,value2)
	value3, err := types.NewMapValueFromJSON(`{
		"id":            4,
		"pin":           87654321,
		"address_line1": "9 Yellow Boulevard",
		"address_line2": "Apt 3"
	}`)
	insertData(client, err,tableName,value3)
	fmt.Printf("Put row succeeded: \n")
	stmt1 := "select * from examplesAddress"
	fetchRowCnt(client, err,tableName,stmt1)
	delMulRows(client, err,tableName,1234567)
	fetchRowCnt(client, err,tableName,stmt1)
}
