// Copyright (c) 2023 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package main

import (
	"fmt"
	"time"

	"github.com/oracle/nosql-go-sdk/nosqldb"
	"github.com/oracle/nosql-go-sdk/nosqldb/auth/iam"
	"github.com/oracle/nosql-go-sdk/nosqldb/common"
)

// createClient creates a client with the supplied configurations.
// This function encapsulates environmental differences and returns a
// client handle to use for data operations.
func createClient() (*nosqldb.Client, error) {
	var cfg nosqldb.Config
	region := "us-ashburn-1"
	//Replace the value of config file location and the ocid of your compartment
	sp, err := iam.NewSignatureProviderFromFile("<config_file_location>","","","<your_compartment_ocid>")
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
func createTable(client *nosqldb.Client, err error, tableName string)(){
	// Creates a table
	stmt := fmt.Sprintf("CREATE TABLE IF NOT EXISTS %s ("+
		"acct_Id INTEGER," +
		"profile_name STRING," +
		"account_expiry TIMESTAMP(1) ," +
		"acct_data JSON, " +
		"PRIMARY KEY(acct_Id))",tableName)
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
	fmt.Println("Created table: ", tableName)
	return
}
func main() {
	client, err := createClient()
	if err != nil {
		fmt.Printf("cannot create NoSQL client: %v\n", err)
		return
	}
	defer client.Close()
	tableName := "stream_acct"
  createTable(client, err,tableName)

}
