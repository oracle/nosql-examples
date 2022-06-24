### CREATE table if not present ###
CREATE TABLE IF NOT EXISTS stream_acct(
acct_id INTEGER,
acct_data JSON, 
PRIMARY KEY(acct_id)
);

### row 1 ###
INSERT INTO stream_acct VALUES(
1,
{
   "firstName" : "John",
   "lastName" : "Sanders",
   "country" : "USA",
   "contentStreamed": [
      {
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
     },
     {
         "showName": "Rita",
         "showId": 16,
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
);

### row 2 ###

INSERT INTO stream_acct VALUES(
2,
{
   "firstName" : "Tim",
   "lastName" : "Greenberg",
   "country" : "USA",
   "contentStreamed": [
      {
         "showName": "Call My Agent",
         "showId": 12,
         "showType": "tvseries",
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
);

### row 3 ###

INSERT INTO stream_acct VALUES(
3,
{
   "firstName" : "Aniketh",
   "lastName" : "Shubham",
   "country" : "India",
   "contentStreamed": [
      {
         "showName": "Apprentice",
         "showId": 14,
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
);
