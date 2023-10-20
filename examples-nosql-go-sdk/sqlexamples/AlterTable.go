// Copyright (c) 2020, 2023 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package main
import (
	"fmt"
	"time"

	"github.com/oracle/nosql-go-sdk/nosqldb"
	"github.com/oracle/nosql-go-sdk/nosqldb/auth/iam"
	"github.com/oracle/nosql-go-sdk/nosqldb/common"
)

// Creates a client with the supplied configurations.
// This function encapsulates environmental differences and returns a
// client handle to use for data operations.
func createClient_cloud() (*nosqldb.Client, error) {
	var cfg nosqldb.Config
	// replace the placeholder with your region identifier
	region := "<your_region_identifier>"
	// Replace the value of config file location and the ocid of your compartment
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
	// Creates a table
func createTable(client *nosqldb.Client, err error, tableName string)(){
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
	 fmt.Println("Created table ", tableName)
	 return
}
// alter an existing table and add a column
func alterTable(client *nosqldb.Client, err error, tableName string)(){
	stmt := fmt.Sprintf("ALTER TABLE %s (ADD acctName STRING)",tableName)
	tableReq := &nosqldb.TableRequest{
		Statement: stmt,
	}
	tableRes, err := client.DoTableRequest(tableReq)
	if err != nil {
		fmt.Printf("cannot initiate ALTER TABLE request: %v\n", err)
		return
	}
	// The alter table request is asynchronous, wait for table alteration to complete.
	_, err = tableRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing ALTER TABLE request: %v\n", err)
		return
	}
	fmt.Println("Altered table ", tableName)
	return
}
// drop an existing table
func dropTable(client *nosqldb.Client, err error, tableName string)(){
	stmt := fmt.Sprintf("DROP TABLE %s",tableName)
	tableReq := &nosqldb.TableRequest{
		Statement: stmt,
	}
	tableRes, err := client.DoTableRequest(tableReq)
	if err != nil {
		fmt.Printf("cannot initiate DROP TABLE request: %v\n", err)
		return
	}
	// The drop table request is asynchronous, wait for table drop to complete.
	_, err = tableRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing DROP TABLE request: %v\n", err)
		return
	}
	fmt.Println("Dropped table ", tableName)
	return
}

func main() {
	// if using cloud service uncomment the line below. else if using onPremises comment this line out
	client, err := createClient_cloud()
	// if using onPrem uncomment the line below, else if using cloud service, comment this line
   // client, err := createClient_onPrem()
	if err != nil {
		fmt.Printf("cannot create NoSQL client: %v\n", err)
		return
	}
	defer client.Close()
	tableName := "stream_acct"
  	createTable(client, err,tableName)
	alterTable(client, err,tableName)
	dropTable(client, err,tableName)
}
