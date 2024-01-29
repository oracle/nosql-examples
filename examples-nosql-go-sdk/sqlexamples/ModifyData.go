// Copyright (c) 2023, 2024 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at
// https://oss.oracle.com/licenses/upl/
package main

import (
	"fmt"
	"github.com/oracle/nosql-go-sdk/nosqldb"
	"github.com/oracle/nosql-go-sdk/nosqldb/auth/iam"
	"github.com/oracle/nosql-go-sdk/nosqldb/common"
	"github.com/oracle/nosql-go-sdk/nosqldb/jsonutil"
	"github.com/oracle/nosql-go-sdk/nosqldb/types"
	"time"
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
	//replace the placeholder with the fullname of your host
	endpoint := "http://<hostname>:8080"
	cfg = nosqldb.Config{
		Endpoint: endpoint,
		Mode:     "onprem",
	}
	// If using a secure store, uncomment the lines below and pass the username,
	// password of the store to Config
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
func createTable(client *nosqldb.Client, err error, tableName string) {
	stmt := fmt.Sprintf("CREATE TABLE IF NOT EXISTS %s ("+
		"acct_Id INTEGER,"+
		"profile_name STRING,"+
		"account_expiry TIMESTAMP(1) ,"+
		"acct_data JSON, "+
		"PRIMARY KEY(acct_Id))", tableName)
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
func insertData(client *nosqldb.Client, err error, tableName string, value1 *types.MapValue) {
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

//update data in the table
func updateRows(client *nosqldb.Client, err error, tableName string, querystmt string) {
	prepReq := &nosqldb.PrepareRequest{
		Statement: querystmt,
	}
	prepRes, err := client.Prepare(prepReq)
	if err != nil {
		fmt.Printf("Prepare failed: %v\n", err)
		return
	}
	queryReq := &nosqldb.QueryRequest{
		PreparedStatement: &prepRes.PreparedStatement}
	var results []*types.MapValue
	for {
		queryRes, err := client.Query(queryReq)
		if err != nil {
			fmt.Printf("Upsert failed: %v\n", err)
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
	fmt.Printf("Updated data in the table: \n")
}

//upsert data in the table
func upsertRows(client *nosqldb.Client, err error, tableName string, querystmt string) {
	prepReq := &nosqldb.PrepareRequest{
		Statement: querystmt,
	}
	prepRes, err := client.Prepare(prepReq)
	if err != nil {
		fmt.Printf("Prepare failed: %v\n", err)
		return
	}
	queryReq := &nosqldb.QueryRequest{
		PreparedStatement: &prepRes.PreparedStatement}
	var results []*types.MapValue
	for {
		queryRes, err := client.Query(queryReq)
		if err != nil {
			fmt.Printf("Upsert failed: %v\n", err)
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

//delete with primary key
func delRow(client *nosqldb.Client, err error, tableName string) {
	key := &types.MapValue{}
	key.Put("acct_Id", 1)
	delReq := &nosqldb.DeleteRequest{
		TableName: tableName,
		Key:       key,
	}
	delRes, err := client.Delete(delReq)
	if err != nil {
		fmt.Printf("failed to delete a row: %v", err)
		return
	}
	if delRes.Success {
		fmt.Println("Delete succeeded")
	}
}

//delete rows based on a filter condition
func deleteRows(client *nosqldb.Client, err error, tableName string, querystmt string) {
	prepReq := &nosqldb.PrepareRequest{
		Statement: querystmt,
	}
	prepRes, err := client.Prepare(prepReq)
	if err != nil {
		fmt.Printf("Prepare failed: %v\n", err)
		return
	}
	queryReq := &nosqldb.QueryRequest{
		PreparedStatement: &prepRes.PreparedStatement}
	var results []*types.MapValue
	for {
		queryRes, err := client.Query(queryReq)
		if err != nil {
			fmt.Printf("Upsert failed: %v\n", err)
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
	fmt.Printf("Deleetd data from the table: %v\n", tableName)
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
	tableName := "stream_acct"
	createTable(client, err, tableName)
	//adding data
	value, err := types.NewMapValueFromJSON(`{
		"acct_Id": 1,
		"profile_name": "AP",
		"account_expiry": "2023-10-18",
		"acct_data": {
			"firstName": "Adam",
			"lastName": "Phillips",
			"country": "Germany",
			"contentStreamed": [{
				"showName": "At the Ranch",
				"showId": 26,
				"showtype": "tvseries",
				"genres": ["action", "crime", "spanish"],
				"numSeasons": 4,
				"seriesInfo": [{
					"seasonNum": 1,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 20,
						"episodeName": "Season 1 episode 1",
						"lengthMin": 85,
						"minWatched": 85,
						"date": "2022-04-18"
					},
					{
						"episodeID": 30,
						"lengthMin": 60,
						"episodeName": "Season 1 episode 2",
						"minWatched": 60,
						"date": "2022 - 04 - 18 "
					}]
				},
				{
					"seasonNum": 2,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 40,
						"episodeName": "Season 2 episode 1",
						"lengthMin": 50,
						"minWatched": 50,
						"date": "2022-04-25"
					},
					{
						"episodeID": 50,
						"episodeName": "Season 2 episode 2",
						"lengthMin": 45,
						"minWatched": 30,
						"date": "2022-04-27"
					}]
				},
				{
					"seasonNum": 3,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 60,
						"episodeName": "Season 3 episode 1",
						"lengthMin": 50,
						"minWatched": 50,
						"date": "2022-04-25"
					},
					{
						"episodeID": 70,
						"episodeName": "Season 3 episode 2",
						"lengthMin": 45,
						"minWatched": 30,
						"date": "2022 - 04 - 27 "
					}]
				}]
			},
			{
				"showName": "Bienvenu",
				"showId": 15,
				"showtype": "tvseries",
				"genres": ["comedy", "french"],
				"numSeasons": 2,
				"seriesInfo": [{
					"seasonNum": 1,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 20,
						"episodeName": "Bonjour",
						"lengthMin": 45,
						"minWatched": 45,
						"date": "2022-03-07"
					},
					{
						"episodeID": 30,
						"episodeName": "Merci",
						"lengthMin": 42,
						"minWatched": 42,
						"date": "2022-03-08"
					}]
				}]
			}
		]}
	}`)
	insertData(client, err, tableName, value)
	value1, err := types.NewMapValueFromJSON(`{
		"acct_Id":2,
		"profile_name":"Adwi",
		"account_expiry":"2023-10-31",
		"acct_data": {
			"firstName": "Adelaide",
			"lastName": "Willard",
			"country": "France",
			"contentStreamed": [{
				"showName" : "Bienvenu",
				"showId" : 15,
				"showtype" : "tvseries",
				"genres" : ["comedy", "french"],
				"numSeasons" : 2,
				"seriesInfo": [ {
					"seasonNum" : 1,
					"numEpisodes" : 2,
					"episodes": [ {
						"episodeID": 22,
						"episodeName" : "Season 1 episode 1",
						"lengthMin": 65,
						"minWatched": 65,
						"date" : "2022-04-18"
					},
					{
						"episodeID": 32,
						"lengthMin": 60,
						"episodeName" : "Season 1 episode 2",
						"minWatched": 60,
						"date" : "2022-04-18"
					}]
				},
				{
					"seasonNum": 2,
					"numEpisodes" :3,
					"episodes": [{
						"episodeID": 42,
						"episodeName" : "Season 2 episode 1",
						"lengthMin": 50,
						"minWatched": 50,
						"date" : "2022-04-25"
					} ]
				}]
			}
		]}
	}`)
	insertData(client, err, tableName, value1)
	value2, err := types.NewMapValueFromJSON(`{
		"acct_Id":3,
		"profile_name":"Dee",
		"account_expiry":"2023-11-28",
		"acct_data":
			{"firstName": "Dierdre",
			"lastName": "Amador",
			"country": "USA",
			"contentStreamed": [{
				"showName" : "Bienvenu",
				"showId" : 15,
				"showtype" : "tvseries",
				"genres" : ["comedy", "french"],
				"numSeasons" : 2,
				"seriesInfo": [ {
					"seasonNum" : 1,
					"numEpisodes" : 2,
					"episodes": [ {
						"episodeID": 23,
						"episodeName" : "Season 1 episode 1",
						"lengthMin": 45,
						"minWatched": 40,
						"date": "2022-08-18"
					},
					{
						"episodeID": 33,
						"lengthMin": 60,
						"episodeName" : "Season 1 episode 2",
						"minWatched": 50,
						"date" : "2022-08-19"
					}]
				},
				{
					"seasonNum": 2,
					"numEpisodes" : 3,
					"episodes": [{
						"episodeID": 43,
						"episodeName" : "Season 2 episode 1",
						"lengthMin": 50,
						"minWatched": 50,
						"date" : "2022-08-25"
					},
					{
						"episodeID": 53,
						"episodeName" : "Season 2 episode 2",
						"lengthMin": 45,
						"minWatched": 30,
						"date" : "2022-08-27"
					}
				]}]
			},
			{
				"showName": "Dane",
				"showId": 16,
				"showtype": "tvseries",
				"genres" : ["comedy", "drama","danish"],
				"numSeasons" : 2,
				"seriesInfo": [{
					"seasonNum" : 1,
					"numEpisodes" : 2,
					"episodes": [{
						"episodeID": 24,
						"episodeName" : "Bonjour",
						"lengthMin": 45,
						"minWatched": 45,
						"date" : "2022-06-07"
					},
					{
						"episodeID": 34,
						"episodeName" : "Merci",
						"lengthMin": 42,
						"minWatched": 42,
						"date" : "2022-06-08"
					}]
				}]
			}]
		}
	}`)
	insertData(client, err, tableName, value2)
	fmt.Printf("Put row succeeded: \n")
	upsert_data := `UPSERT INTO stream_acct VALUES
	(
		1,
		"AP",
		"2023-10-18",
		{
			"firstName": "Adam",
			"lastName": "Phillips",
			"country": "Germany",
			"contentStreamed": [{
				"showName": "At the Ranch",
				"showId": 26,
				"showtype": "tvseries",
				"genres": ["action", "crime", "spanish"],
				"numSeasons": 4,
				"seriesInfo": [{
					"seasonNum": 1,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 20,
						"episodeName": "Season 1 episode 1",
						"lengthMin": 75,
						"minWatched": 75,
						"date": "2022-04-18"
					},
					{
						"episodeID": 30,
						"lengthMin": 60,
						"episodeName": "Season 1 episode 2",
						"minWatched": 40,
						"date": "2022 - 04 - 18 "
					}]
				},
				{
					"seasonNum": 2,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 40,
						"episodeName": "Season 2 episode 1",
						"lengthMin": 40,
						"minWatched": 30,
						"date": "2022-04-25"
					},
					{
						"episodeID": 50,
						"episodeName": "Season 2 episode 2",
						"lengthMin": 45,
						"minWatched": 30,
						"date": "2022-04-27"
					}]
				},
				{
					"seasonNum": 3,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 60,
						"episodeName": "Season 3 episode 1",
						"lengthMin": 20,
						"minWatched": 20,
						"date": "2022-04-25"
					},
					{
						"episodeID": 70,
						"episodeName": "Season 3 episode 2",
						"lengthMin": 45,
						"minWatched": 30,
						"date": "2022 - 04 - 27 "
					}]
				}]
			},
			{
				"showName": "Bienvenu",
				"showId": 15,
				"showtype": "tvseries",
				"genres": ["comedy", "french"],
				"numSeasons": 2,
				"seriesInfo": [{
					"seasonNum": 1,
					"numEpisodes": 2,
					"episodes": [{
						"episodeID": 20,
						"episodeName": "Bonjour",
						"lengthMin": 45,
						"minWatched": 45,
						"date": "2022-03-07"
					},
					{
						"episodeID": 30,
						"episodeName": "Merci",
						"lengthMin": 42,
						"minWatched": 42,
						"date": "2022-03-08"
					}]
				}]
			}]
		}
	) RETURNING *`
	upsertRows(client, err, tableName, upsert_data)
	updt_stmt := "UPDATE stream_acct SET account_expiry='2023-12-28T00:00:00.0Z' WHERE acct_Id=3"
	updateRows(client, err, tableName, updt_stmt)
	upd_json_addnode := `UPDATE stream_acct acct1 ADD acct1.acct_data.contentStreamed.seriesInfo[1].episodes {
	   "date" : "2022-04-26",
	   "episodeID" : 43,
	   "episodeName" : "Season 2 episode 2",
	   "lengthMin" : 45,
	   "minWatched" : 45} WHERE acct_Id=2 RETURNING *`
	updateRows(client, err, tableName, upd_json_addnode)
	upd_json_delnode := `UPDATE stream_acct acct1 REMOVE acct1.acct_data.contentStreamed.seriesInfo[1].episodes[1] WHERE acct_Id=2 RETURNING *`
	updateRows(client, err, tableName, upd_json_delnode)
	delRow(client, err, tableName)
	delete_stmt := `DELETE FROM stream_acct acct1 WHERE acct1.acct_data.firstName="Adelaide" AND acct1.acct_data.lastName="Willard"`
	deleteRows(client, err, tableName, delete_stmt)
}
