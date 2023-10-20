// Copyright (c) 2020, 2023 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package main
import (
	"fmt"
	"time"

	"github.com/oracle/nosql-go-sdk/nosqldb"
)

// Creates a client with the supplied configurations for onPremise database
func createClient_onPrem() (*nosqldb.Client, error) {
	var cfg nosqldb.Config
	// replace the placeholder with the fullname of your host
	endpoint := "http://<hostname>:8080"
	cfg= nosqldb.Config{
      Endpoint: endpoint,
      Mode:     "onprem",
   }
	// If using a secure store pass the username, password of the store to Config
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
// Creates a remote and a local region
func crtRegion(client *nosqldb.Client, err error)(){
	// Create a remote region
	stmt := fmt.Sprintf("CREATE REGION LON")
	sysReq := &nosqldb.SystemRequest{
		Statement: stmt,
	}
	sysRes, err := client.DoSystemRequest(sysReq)
	_, err = sysRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing CREATE REGION request: %v\n", err)
		return
	}
	fmt.Println("Created REGION LON ")
	// Create a local region
	stmt1 := fmt.Sprintf("SET LOCAL REGION FRA")
	sysReq1 := &nosqldb.SystemRequest{
		Statement: stmt1,
	}
	sysRes1, err1 := client.DoSystemRequest(sysReq1)
	_, err1 = sysRes1.WaitForCompletion(client, 60*time.Second, time.Second)
	if err1 != nil {
		fmt.Printf("Error finishing CREATE REGION request: %v\n", err)
		return
	}
	fmt.Println("Created REGION FRA ")
	return
}
// creates a table in a specific region
func crtTabInRegion(client *nosqldb.Client, err error, tableName string)(){
	stmt := fmt.Sprintf("CREATE TABLE IF NOT EXISTS %s ("+
		"acct_Id INTEGER," +
		"profile_name STRING," +
		"account_expiry TIMESTAMP(1) ," +
		"acct_data JSON, " +
		"PRIMARY KEY(acct_Id)) IN REGIONS FRA",tableName)
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
// drops a table from a region
func drpTabInRegion(client *nosqldb.Client, err error, tableName string)(){
	stmt := fmt.Sprintf("DROP TABLE %s",tableName)
	tableReq := &nosqldb.TableRequest{
		Statement: stmt,
	}
	tableRes, err := client.DoTableRequest(tableReq)
	if err != nil {
		fmt.Printf("cannot initiate DROP TABLE request: %v\n", err)
		return
	}
	_, err = tableRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing DROP TABLE request: %v\n", err)
		return
	}
	fmt.Println("Dropped table ", tableName)
	return
}
// drop a region
func dropRegion(client *nosqldb.Client, err error)(){
	stmt := fmt.Sprintf("DROP REGION LON")
	sysReq := &nosqldb.SystemRequest{
		Statement: stmt,
	}
	sysRes, err := client.DoSystemRequest(sysReq)
	_, err = sysRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing DROP REGION request: %v\n", err)
		return
	}
	fmt.Println("Dropped REGION LON ")
	return
}
func main() {
	client, err := createClient_onPrem()
	if err != nil {
		fmt.Printf("cannot create NoSQL client: %v\n", err)
		return
	}
	defer client.Close()
	crtRegion(client, err)
	tableName := "stream_acct"
	crtTabInRegion(client, err,tableName)
	drpTabInRegion(client, err,tableName)
	dropRegion(client, err)
}
