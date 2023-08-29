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
    "firstName": "Angela",
    "lastName": "Mercel",
    "country" : "Germany",
    "contentStreamed": [
      {
        "showName" : "Casa de papel",
        "showId" : 26,
        "showtype" : "tvseries",
        "genres" : ["action", "crime", "spanish"], 
        "numSeasons" : 4,
        "seriesInfo": [
          {
            "seasonNum" : 1,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 20,
                "lengthMin": 85,
                "minWatched": 85,
                "date" : "2022-04-18"
              },
              {
                "episodeID": 30,
                "lengthMin": 60,
                "minWatched": 60,
                "date" : "2022-04-18"
              }
            ]
          },
          {
            "seasonNum": 2,
            "numEpisodes" : 4,
            "episodes": [
              {
                "episodeID": 40,
                "lengthMin": 50,
                "minWatched": 50,
                "date" : "2022-04-25"
              },
              {
                "episodeID": 50,
                "lengthMin": 45,
                "minWatched": 30,
                "date" : "2022-04-27"
              }
            ]
          }
        ]
      },
      {
        "showName": "Call My Agent",
        "showId": 15,
        "showtype": "tvseries",
        "genres" : ["comedy", "french"], 
        "numSeasons" : 2,
        "seriesInfo": [
          {
            "seasonNum" : 1,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 20,
                "lengthMin": 45,
                "minWatched": 45,
                "date" : "2022-03-07"
              },
              {
                "episodeID": 30,
                "lengthMin": 42,
                "minWatched": 42,
                "date" : "2022-03-08"
              }
            ]
          }
        ]
      }
    ]
  }
)
;

### row 2 ###

INSERT INTO stream_acct VALUES(
2,
{
    "firstName": "Emmanuel",
    "lastName": "Macron",
    "country" : "France",
    "contentStreamed": [
      {
        "showName": "Call My Agent",
        "showId": 15,
        "showtype": "tvseries",
        "genres" : ["comedy", "french"], 
        "numSeasons" : 2,
        "seriesInfo": [
          {
            "seasonNum" : 1,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 20,
                "lengthMin": 45,
                "minWatched": 45,
                "date": "2022-06-01"
              },
              {
                "episodeID": 30,
                "lengthMin": 42,
                "minWatched": 42,
                "date": "2022-06-01"
              }
            ]
          },
          {
            "seasonNum": 2,
            "numEpisodes" : 3,
            "episodes": [
              {
                "episodeID": 40,
                "lengthMin": 50,
                "minWatched": 50,
                "date": "2022-06-01"
              }
            ]
          }
        ]
      }
    ]
  }
)
;

### row 3 ###

INSERT INTO stream_acct VALUES(
3,
{
    "firstName": "Joe",
    "lastName": "Biden",
    "country" : "USA",
    "contentStreamed": [
      {
        "showName": "Call My Agent",
        "showId": 15,
        "showtype": "tvseries",
        "genres" : ["comedy", "french"], 
        "numSeasons" : 2,
        "seriesInfo": [
          {
            "seasonNum" : 1,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 20,
                "lengthMin": 45,
                "minWatched": 44,
                "date": "2022-05-19"
              },
              {
                "episodeID": 30,
                "lengthMin": 42,
                "minWatched": 42,
                "date": "2022-05-19"
              }
            ]
          },
          {
            "seasonNum": 2,
            "numEpisodes" : 3,
            "episodes": [
              {
                "episodeID": 40,
                "lengthMin": 50,
                "minWatched": 50,
                "date": "2022-05-20"
              },
              {
                "episodeID": 50,
                "lengthMin": 45,
                "minWatched": 45,
                "date": "2022-05-21"
              }
            ]
          }
        ]
      },
      {
        "showName": "Rita",
        "showId": 16,
        "showtype": "tvseries",
        "genres" : ["comedy", "drama", "danish"],
        "numSeasons" : 2,
        "seriesInfo": [
          {
            "seasonNum" : 1,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 20,
                "lengthMin": 65,
                "minWatched": 65,
                "date": "2022-03-18"
              },
              {
                "episodeID": 30,
                "lengthMin": 60,
                "minWatched": 60,
                "date": "2022-03-19"
              }
            ]
          },
          {
            "seasonNum": 2,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 40,
                "lengthMin": 50,
                "minWatched": 50,
                "date": "2022-05-05"
              },
              {
                "episodeID": 50,
                "lengthMin": 45,
                "minWatched": 45,
                "date": "2022-05-06"
              }
            ]
          }
        ]
      }
    ]
  }
)
;

### row 4 ###

INSERT INTO stream_acct VALUES(
4,
{
    "firstName" : "Kamala",
    "lastName" : "Harris",
    "country" : "USA",
    "contentStreamed": [
      {
        "showName": "Call My Agent",
        "showId": 15,
        "showtype": "tvseries",
        "genres" : ["comedy", "french"],
        "numSeasons" : 2,
        "seriesInfo": [
          {
            "seasonNum" : 1,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 20,
                "lengthMin": 45,
                "minWatched": 45,
                "date": "2022-01-18"
              },
              {
                "episodeID": 30,
                "lengthMin": 42,
                "minWatched": 42,
                "date": "2022-01-19"
              }
            ]
          },
          {
            "seasonNum": 2,
            "numEpisodes" : 3,
            "episodes": [
              {
                "episodeID": 40,
                "lengthMin": 50,
                "minWatched": 50,
                "date": "2022-02-18"
              },
              {
                "episodeID": 50,
                "lengthMin": 45,
                "minWatched": 45,
                "date": "2022-02-28"
              },
              {
                "episodeID": 60,
                "lengthMin": 55,
                "minWatched": 55,
                "date": "2022-04-02"
              }
            ]
          }
        ]
      },
      {
        "showName": "Rita",
        "showId": 16,
        "showtype": "tvseries",
        "genres" : ["comedy", "drama", "danish"], 
        "numSeasons" : 2,
        "seriesInfo": [
          {
            "seasonNum" : 1,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 20,
                "lengthMin": 65,
                "minWatched": 65,
                "date": "2022-01-18"
              },
              {
                "episodeID": 30,
                "lengthMin": 60,
                "minWatched": 60,
                "date": "2022-02-07"
              }
            ]
          },
          {
            "seasonNum": 2,
            "numEpisodes" : 2,
            "episodes": [
              {
                "episodeID": 40,
                "lengthMin": 50,
                "minWatched": 50,
                "date": "2022-02-08"
              },
              {
                "episodeID": 50,
                "lengthMin": 45,
                "minWatched": 45,
                "date": "2022-02-20"
              }
            ]
          }
        ]
      }
    ]
  }
)
;
