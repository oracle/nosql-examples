// Copyright (c) 2025 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package main

import (
    "fmt"
        //"encoding/json"
        "reflect"
        "net/http"
        "github.com/gin-gonic/gin"
    "github.com/oracle/nosql-go-sdk/nosqldb"
    "github.com/oracle/nosql-go-sdk/nosqldb/auth/iam"
    "github.com/oracle/nosql-go-sdk/nosqldb/common"
    "github.com/oracle/nosql-go-sdk/nosqldb/types"
)


// album represents data about a record album.
type album struct {
    ID     string  `json:"id"`
    Title  string  `json:"title" binding:"required"`
    Artist string  `json:"artist"  binding:"required"`
    Price  float64 `json:"price"`
}

// albums slice to seed record album data.
var albums = []album{
    {ID: "1", Title: "Blue Train", Artist: "John Coltrane", Price: 56.99},
    {ID: "2", Title: "Jeru", Artist: "Gerry Mulligan", Price: 17.99},
    {ID: "3", Title: "Sarah Vaughan and Clifford Brown", Artist: "Sarah Vaughan", Price: 39.99},
}

//var client, err = createClient()
var client, err = createClientOnPremise()

// createClient creates a client with the supplied configurations.
func createClient() (*nosqldb.Client, error) {
    var cfg nosqldb.Config
    region := "us-ashburn-1"
    //Replace the value of config file location and the ocid of your compartment
    sp, err := iam.NewSignatureProviderFromFile("~/.oci/config","","","ocid1.compartment.oc1..aaaaaaaa4mlehopmvdluv2wjcdp4tnh2ypjz3nhhpahb4ss7yvxaa3be3diq")
    if err != nil {
        return nil, fmt.Errorf("cannot create a Signature Provider: %v", err)
    }
    cfg = nosqldb.Config{
        Mode:   "cloud",
        Region: common.Region(region),
        AuthorizationProvider: sp,
    }
    client, err := nosqldb.NewClient(cfg)
    return client, err
}

// createClient creates a client with the supplied configurations.
func createClientOnPremise() (*nosqldb.Client, error) {
    var cfg nosqldb.Config
    cfg = nosqldb.Config{
        Endpoint: "http://localhost:8080",
        Mode:     "onprem",
    }
    client, err := nosqldb.NewClient(cfg)
    return client, err
}

func main() {
    defer client.Close()
    router := gin.Default()
    router.GET("/albums", getAlbums)
    router.GET("/albums/:id", getAlbumByID)
    router.POST("/albums", postAlbums)
    router.Run("localhost:8080")
}

func getAlbums(c *gin.Context) {
    //c.IndentedJSON(http.StatusOK, albums)

    stmt := fmt.Sprintf("SELECT * FROM go_gin_albums" )
    queryReq := &nosqldb.QueryRequest{
        Statement:   stmt,
        StructType:  reflect.TypeOf((*album)(nil)).Elem(),
    }
    var albumsr []album
    for {
        queryRes, err := client.Query(queryReq)
        if err != nil {
            break
        }
        res, err := queryRes.GetStructResults()
        if err != nil {
            break
        }

        //albumsr = append(albumsr, res...)
        for i := 0; i < len(res); i++ {
            if v, ok := res[i].(*album); ok {
                albumsr = append(albumsr, *v)
            }
        }
        if queryReq.IsDone() {
           break;
        }
    }
    c.IndentedJSON(http.StatusOK, albumsr)
}

// postAlbums adds an album from JSON received in the request body.
func postAlbums(c *gin.Context) {
    var newAlbum album

    // Call BindJSON to bind the received JSON to newAlbum.
    if err := c.BindJSON(&newAlbum); err != nil {
        fmt.Println("Error binding")
        return
    }
    // Add the new album to the slice.
    // albums = append(albums, newAlbum)
    // c.IndentedJSON(http.StatusCreated, newAlbum)


    /*
    u, err := json.Marshal(newAlbum)
    if err != nil {
       panic(err)
    }
    fmt.Println(string(u))
    value, err := types.NewMapValueFromJSON(string(u))
    fmt.Println(value)

    putReq := &nosqldb.PutRequest{
        TableName: "go_gin_albums",
        Value:     value,
    }
    */
    fmt.Println(newAlbum)
    putReq := &nosqldb.PutRequest{
        TableName:   "go_gin_albums",
        StructValue: newAlbum,
    }

    putRes, err2 := client.Put(putReq)
    if err2 != nil {
       fmt.Printf("failed to put single row: %v\n", err)
       return
    }
    fmt.Println(putRes)
    c.IndentedJSON(http.StatusCreated, newAlbum)
}

// getAlbumByID locates the album whose ID value matches the id parameter sent by the client, then returns that album as a response.
func getAlbumByID(c *gin.Context ) {
    // Loop through the list of albums, looking for
    // an album whose ID value matches the parameter.
    // for _, a := range albums {
    //   if a.ID == id {
    //       c.IndentedJSON(http.StatusOK, a)
    //       return
    //   }
    // }
    // c.IndentedJSON(http.StatusNotFound, gin.H{"message": "album not found"})


    key := &types.MapValue{}
    key.Put("id", c.Param("id"))
    /*
    getReq := &nosqldb.GetRequest{
       TableName: "go_gin_albums",
       Key:       key,
    }
    getRes, err := client.Get(getReq)
    */

    getReq := &nosqldb.GetRequest{
        TableName: "go_gin_albums",
        Key:       key,
        StructType:  reflect.TypeOf((*album)(nil)).Elem(),
    }
    getRes, err := client.Get(getReq)
    fmt.Printf("Got row: %v\n", getRes.Version)


    if err != nil {
          c.IndentedJSON(http.StatusNotFound, gin.H{"message": err})
    }
    if ! getRes.RowExists() {
          c.IndentedJSON(http.StatusNotFound, gin.H{"message": "Album not found"})
    }

    if getRes.Value != nil {
           c.IndentedJSON(http.StatusOK, getRes.Value)
    }
    if getRes.StructValue != nil {
           c.IndentedJSON(http.StatusOK, getRes.StructValue)
    }
}
