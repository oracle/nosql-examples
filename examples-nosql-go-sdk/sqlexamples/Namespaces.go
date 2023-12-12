// Copyright (c) 2023, 2024 Oracle and/or its affiliates.
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
	cfg = nosqldb.Config{
		Endpoint: endpoint,
		Mode:     "onprem",
	}
	// If using a secure store, uncomment the lines below and pass the username, password of the store to Config
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

// Creates a namespace
func createNS(client *nosqldb.Client, err error) {
	stmt := fmt.Sprintf("CREATE NAMESPACE IF NOT EXISTS ns1")
	sysReq := &nosqldb.SystemRequest{
		Statement: stmt,
	}
	sysRes, err := client.DoSystemRequest(sysReq)
	_, err = sysRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing CREATE NAMESPACE request: %v\n", err)
		return
	}
	fmt.Println("Created Namespace ns1 ")
	return
}

// Drops a namespace
func dropNS(client *nosqldb.Client, err error) {
	stmt := fmt.Sprintf("DROP NAMESPACE ns1")
	sysReq := &nosqldb.SystemRequest{
		Statement: stmt,
	}
	sysRes, err := client.DoSystemRequest(sysReq)
	_, err = sysRes.WaitForCompletion(client, 60*time.Second, time.Second)
	if err != nil {
		fmt.Printf("Error finishing CREATE NAMESPACE request: %v\n", err)
		return
	}
	fmt.Println("Dropped Namespace ns1 ")
	return
}
func main() {
	client, err := createClient_onPrem()
	if err != nil {
		fmt.Printf("cannot create NoSQL client: %v\n", err)
		return
	}
	defer client.Close()
	createNS(client, err)
	dropNS(client, err)
}
