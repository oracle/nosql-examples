CREATE TABLE IF NOT EXISTS PointsOfInterest (
    id INTEGER, poi JSON, 
PRIMARY KEY(id));

INSERT INTO PointsOfInterest VALUES (
    1,
    {
        "kind" : "city hall",
        "address" : { 
            "state" : "CA",
            "city" : "Campbell",
            "street" : "70 North 1st street"
        },
        "location" : {
            "type" : "point", 
            "coordinates": [-120.673828125,38.85682013474361]
        }
    }
);

INSERT INTO PointsOfInterest VALUES (
    2,
    {
        "kind" : "nature park",
        "name" : "portola redwoods state park",
        "address" : { 
            "state" : "CA",
            "city" : "Los Gatos",
            "street" : "15000 Skyline Blvd"
        },
        "location" : { 
            "type" : "polygon",
            
        "coordinates": [
          [
            [
              -122.18101501464844,
              37.2374353328716
            ],
            [
              -122.13775634765625,
              37.243448378654115
            ],
            [
              -122.15698242187499,
              37.28279464911045
            ],
            [
              -122.20642089843749,
              37.276238364942955
            ],
            [
              -122.22015380859374,
              37.2587521486561
            ],
            [
              -122.21054077148438,
              37.2401685949667
            ],
            [
              -122.17758178710939,
              37.2368886685595
            ],
            [
              -122.18101501464844,
              37.2374353328716
            ]
          ]
        ]
      }
    }
);

INSERT INTO PointsOfInterest VALUES (
    3,
    {
        "kind" : "gas station",
        "address" : { 
            "state" : "CA",
            "city" : "Campbell",
            "street" : "63 South 1st street"
        },
        "location" : {
            "type" : "point", 
             "coordinates": [-121.95922851562501,37.302460074782296]
        }
    }
);

INSERT INTO PointsOfInterest VALUES (
    4,
    {
        "kind" : "restaurant",
        "name" : "Effie's Restaurant and Bar"
        "address" : { 
            "state" : "CA",
            "city" : "Campbell",
            "street" : "80 Woodeard St"
        },
        "location" : {
            "type" : "point", 
             "coordinates": [-121.95922851562501,37.302460074782296]
        }
    }
);

INSERT INTO PointsOfInterest VALUES (
    5,
    {
        "kind" : "gas station",
        "address" : { 
            "state" : "CA",
            "city" : "Campbell",
            "street" : "33 North Avenue"
        },
        "location" : {
            "type" : "point", 
             "coordinates": [-121.91768646240233,37.292081740702365]
        }
    }
);

INSERT INTO PointsOfInterest VALUES (
    6,
    {
        "kind" : "hospital",
        "name" : "St. Marthas hospital",
        "address" : { 
            "state" : "CA",
            "city" : "Cupertino",
            "street" : "18000 West Blvd"
        },
        "location" : {
            "type" : "point", 
            "coordinates": [-122.04093933105469,37.32949164059004]
        }
    }
);

INSERT INTO PointsOfInterest VALUES (
    7,
    {
        "kind" : "restaurant",
        "name" : "Coach Sports Bar & Grill",
        "address" : { 
            "state" : "CA",
            "city" : "Campbell",
            "street" : "80 Edward St"
        },
        "location" : {
            "type" : "point", 
             "coordinates": [-121.94935798645021,37.281292217764246]
        }
    }
);

INSERT INTO PointsOfInterest VALUES (
    8,
    {
        "kind" : "hospital",
        "name" : "Memorial hospital",
        "address" : { 
            "state" : "CA",
            "city" : "Cupertino",
            "street" : "10500 South St"
        },
        "location" : {
            "type" : "point", 
            "coordinates": [-122.00763702392578,37.32526006760127]
        }
    }
);

INSERT INTO PointsOfInterest VALUES (
    9,
    {
        "kind" : "restaurant",
        "name" : "Ricos Taco",
        "address" : { 
            "state" : "CA",
            "city" : "Campbell",
            "street" : "80 East Boulevard St"
        },
        "location" : {
            "type" : "point", 
             "coordinates": [-121.95004463195802,37.27705793169547]
        }
    }
);



INSERT INTO PointsOfInterest VALUES (
    10,
    {
        "kind" : "nature park",
        "name" : "Yosemite national park",
        "address" : { 
            "state" : "CA",
            "city" : "Greeley Hill",
            "street" : "1549 East Lane"
        },
        "location" : { 
            "type" : "polygon",            
    
"coordinates": [
          [
            [
              -119.9652099609375,
              37.94419750075404
            ],
            [
              -120.0750732421875,
              37.61858263247881
            ],
            [
              -119.5477294921875,
              37.496652341233364
            ],
            [
              -119.22912597656249,
              37.75768707689704
            ],
            [
              -119.33898925781251,
              37.98750437106374
            ],
            [
              -119.9652099609375,
              37.94419750075404
            ]
          ]
        ]
      }
    }
);

INSERT INTO PointsOfInterest VALUES (
    11,
    {
        "kind" : "house",
        "county" : "Kinga",
        "contact" : "469 384 7612",
        "address" : { 
            "state" : "Texas",
            "city" : "Gatesville",
            "street" : "1549 North Lane"
        },
        "location" : { 
            "type" : "polygon",            
    
 "coordinates": [
          [
            [
              -97.93212890625,
              31.648705289976853
            ],
            [
              -98.4375,
              31.695455797778713
            ],
            [
              -98.44848632812499,
              31.541089879585808
            ],
            [
              -98.26171875,
              31.512995857454676
            ],
            [
              -97.943115234375,
              31.66740831708089
            ],
            [
              -97.93212890625,
              31.648705289976853
            ]
          ]
        ]
      }
    }
);
