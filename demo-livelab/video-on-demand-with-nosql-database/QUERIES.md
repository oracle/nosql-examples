# demo-tv-streaming-app
Demo TV streaming application using GraphQL and NoSQL

## TEST using https://studio.apollographql.com/sandbox 

Note: In order to manage certificates and SSL, I am using the following url after creating an API Gateway 
https://lc22qxcred2zq4ciqms2tzzxv4.apigateway.us-ashburn-1.oci.customer-oci.com/

This API Gateway is connected to a current deployment using OKE (Oracle Kubernetes Engine)



GraphQL queries
````
query ExampleQuery {
  streams {
    id
    info {
      firstName
      lastName
      country
      shows {
        showName
        showId
        showType
        numSeasons
        seriesInfo {
          seasonNum
          numEpisodes
          episodes {
            episodeID
            lengthMin
            minWatched
          }
        }
      }
    }
  }
}


query Stream($streamId: Int) {
  user1:stream(id: $streamId) {
    id
    info {
      firstName
      lastName
      country
      shows {
        showName
        showId
        showType
        numSeasons
        seriesInfo {
          seasonNum
          numEpisodes
          episodes {
            episodeID
            lengthMin
            minWatched
          }
        }
      }
    }
  }
}


query Stream($streamId: Int) {
  user1:stream(id: $streamId) {
    id
    info {
      firstName
      lastName
      country
      shows {
        showName
        showId
        showType
        numSeasons
        seriesInfo {
          seasonNum
          numEpisodes
          episodes {
            episodeID
            lengthMin
            minWatched
          }
        }
      }
    }
  }
  user2: stream(id: 2) {
    id 
    }
}


query Stream ($contentDirective: Boolean!){
    user1: stream (id: 1) {
        id
        ...contentStreamed @include(if: $contentDirective)
    }
    user2: stream(id: 2) {
        id
        ...contentStreamed @include(if: $contentDirective)
    }
}

fragment contentStreamed on Stream {
    info
    {
      shows {  
        showName
        showId
        showType
        numSeasons    
      }
    }
}

query PeopleWatching($country: String!) {
  peopleWatching(country: $country) {
    showId
    cnt
  }
}

query WatchTime {
  watchTime {
    showName
    seasonNum
    length
  }
}

query streamByLastName($lastName: String) {  
    user1:streamByLastName(lastName:$lastName) 
    {    id    
         info {      
           firstName
           lastName
           country
           shows {
             showName
           }
         }
    }
}
	
````
GraphQL queries variable
````
{
  "streamId": 1,
  "country":"USA",
  "contentDirective": true,
  "lastName":"vega"  
}
````

GraphQL mutations and queries variable
````
mutation CreateStream($input: StreamEntry) {
  createStream(input: $input) {
    id
    info {
      firstName
      lastName
      country
      shows {
           showName
      }
	}
  }
}



{
  "input": {
    "firstName": "dario",
    "lastName": "vega",
    "country": "France",
  }
}

mutation Mutation($updateStreamId: Int, $input: showsEntry) {
  updateStream(id: $updateStreamId, input: $input) {
    id
    info {
      firstName
      lastName
      country
      shows {
        showName
      }
    }
  }
}

{
  "updateStreamId":1001,
  "input": {
         "showName": "Call My Agent",
         "showId": 12,
         "showType": "tvseries",
         "numSeasons" : 2,
         "seriesInfo": [
            {
               "seasonNum" : 1,
               "numEpisodes" : 2,
               "episodes": [
                  { "episodeID" : 20, "lengthMin" : 40, "minWatched" : 40 },
                  { "episodeID" : 30, "lengthMin" : 42, "minWatched" : 42 }
               ]
            },
            {
               "seasonNum": 2,
               "numEpisodes" : 2,
               "episodes": [
                  { "episodeID" : 20, "lengthMin" : 50, "minWatched" : 50 },
                  { "episodeID" : 30, "lengthMin" : 46, "minWatched" : 46 }
               ]
            }
        ]
     }
}

mutation DeleteStream($deleteStreamId: Int) {
  deleteStream(id: $deleteStreamId) {
    id
  }
}

{
  "lastName": "vega",
  "deleteStreamId": 1001
}

````

