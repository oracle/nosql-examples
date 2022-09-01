// Copyright (c) 2022 Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
//

//first row

INSERT INTO stream_acct VALUES(
1,
{
   "firstName" : "John",
   "lastName" : "Sanders",
   "country" : "USA",
   "shows": [
      {
         "showName": "Call My Agent",
         "showId": 12,
         "type": "tvseries",
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
     },
     {
         "showName": "Rita",
         "showId": 16,
         "type": "tvseries",
         "numSeasons" : 1,
         "seriesInfo": [
            {
               "seasonNum" : 1,
               "numEpisodes" : 2,
               "episodes": [
                   { "episodeID" : 20, "lengthMin" : 65, "minWatched" : 65 },
                   { "episodeID" : 30, "lengthMin" : 60, "minWatched" : 60 }
                ]
            }
         ]
      }
    ]
  }
)
;
//second row
INSERT INTO stream_acct VALUES(
2,
{
   "firstName" : "Tim",
   "lastName" : "Greenberg",
   "country" : "USA",
   "shows": [
      {
         "showName": "Call My Agent",
         "showId": 12,
         "type": "tvseries",
         "numSeasons" : 2,
         "seriesInfo": [
            {
               "seasonNum" : 1,
               "numEpisodes" : 2,
               "episodes": [
                  { "episodeID" : 20, "lengthMin" : 38, "minWatched" : 36 },
                  { "episodeID" : 30, "lengthMin" : 40, "minWatched" : 40 }
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
     },
     {
         "showName": "Mr.Chef",
         "showId": 13,
         "showType": "tvseries",
         "numSeasons" : 1,
         "seriesInfo": [
            {
               "seasonNum" : 1,
               "numEpisodes" : 2,
               "episodes": [
                   { "episodeID" : 20, "lengthMin" : 65, "minWatched" : 65 },
                   { "episodeID" : 30, "lengthMin" : 60, "minWatched" : 60 }
                ]
            }
         ]
      }
    ]
  }
)
;
//third row
INSERT INTO stream_acct VALUES(
3,
{
   "firstName" : "Aniketh",
   "lastName" : "Shubham",
   "country" : "India",
   "shows": [
      {
         "showName": "Apprentice",
         "showId": 14,
         "type": "tvseries",
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
     },
     {
         "showName": "Mystery unfolded",
         "showId": 15,
         "showType": "tvseries",
         "numSeasons" : 1,
         "seriesInfo": [
            {
               "seasonNum" : 1,
               "numEpisodes" : 2,
               "episodes": [
                   { "episodeID" : 20, "lengthMin" : 65, "minWatched" : 65 },
                   { "episodeID" : 30, "lengthMin" : 60, "minWatched" : 60 }
                ]
            }
         ]
      }
    ]
  }
)
;
