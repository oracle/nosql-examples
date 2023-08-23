### CREATE table ticket if not present ###
CREATE TABLE IF NOT EXISTS ticket(ticketNo LONG, confNo STRING, PRIMARY KEY(ticketNo));

### CREATE table ticket.baginfo if not present ###
CREATE TABLE IF NOT EXISTS ticket.bagInfo(id LONG,tagNum LONG,routing STRING,lastActionCode STRING,lastActionDesc STRING,lastSeenStation STRING,lastSeenTimeGmt TIMESTAMP(4),bagArrivalDate TIMESTAMP(4), PRIMARY KEY(id));

### CREATE table ticket.bagInfo.flightLegs if not present ###
CREATE TABLE IF NOT EXISTS ticket.bagInfo.flightLegs(flightNo STRING, flightDate TIMESTAMP(4),fltRouteSrc STRING,fltRouteDest STRING,estimatedArrival TIMESTAMP(4), actions JSON, PRIMARY KEY(flightNo));

### CREATE table ticket.passengerInfo if not present ###
CREATE TABLE IF NOT EXISTS ticket.passengerInfo(contactPhone STRING, fullName STRING,gender STRING, PRIMARY KEY(contactPhone));

###Insert into ticket table ###
INSERT INTO ticket VALUES(1762344493810,"LE6J4Z");
INSERT INTO ticket VALUES(1762392135540,"DN3I4Q");
INSERT INTO ticket VALUES(1762376407826,"ZG8Z5N");
INSERT INTO ticket VALUES(1762355527825,"HJ4J4P");
INSERT INTO ticket VALUES(1762324912391,"LN0C8R");

###Insert into ticket.bagInfo table ###
INSERT INTO ticket.bagInfo VALUES(1762344493810,79039899165297,17657806255240,"MIA/LAX/MEL","OFFLOAD","OFFLOAD","MEL","2019-02-01T16:13:00Z","2019-02-01T16:13:00Z");
INSERT INTO ticket.bagInfo VALUES(1762392135540,79039899156435,17657806224224,"GRU/ORD/SEA","OFFLOAD","OFFLOAD","SEA","2019-02-15T21:21:00Z","2019-02-15T21:21:00Z");
INSERT INTO ticket.bagInfo VALUES(1762376407826,7903989918469,17657806240229,"JFK/MAD","OFFLOAD","OFFLOAD","MAD","2019-03-07T13:51:00Z","2019-03-07T13:51:00Z");
INSERT INTO ticket.bagInfo VALUES(1762355527825,79039899197492,17657806232501,"BZN/SEA/CDG/MXP","OFFLOAD","OFFLOAD","MXP","2019-03-22T10:17:00Z","2019-03-22T10:17:00Z");
INSERT INTO ticket.bagInfo VALUES(1762324912391,79039899168383,1765780623244,"MXP/CDG/SLC/BZN","OFFLOAD","OFFLOAD","BZN","2019-03-15T10:13:00Z","2019-03-15T10:13:00Z");

###Insert into ticket.bagInfo.flightLegs table ###
INSERT INTO ticket.bagInfo.flightLegs VALUES(1762344493810,79039899165297,"BM604","2019-02-01T06:00:00Z","MIA","LAX","2019-02-01T11:00:00Z",[ {
         "actionAt" : "MIA",
         "actionCode" : "ONLOAD to LAX",
         "actionTime" : "2019-02-01T06:13:00Z"
       }, {
         "actionAt" : "MIA",
         "actionCode" : "BagTag Scan at MIA",
         "actionTime" : "2019-02-01T05:47:00Z"
       }, {
         "actionAt" : "MIA",
         "actionCode" : "Checkin at MIA",
         "actionTime" : "2019-02-01T04:38:00Z"
       } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762344493810,79039899165297,"BM667","2019-02-01T06:13:00Z","LAX","MEL","2019-02-01T16:15:00Z", [ {
         "actionAt" : "MEL",
         "actionCode" : "Offload to Carousel at MEL",
         "actionTime" : "2019-02-01T16:15:00Z"
       }, {
         "actionAt" : "LAX",
         "actionCode" : "ONLOAD to MEL",
         "actionTime" : "2019-02-01T15:35:00Z"
       }, {
         "actionAt" : "LAX",
         "actionCode" : "OFFLOAD from LAX",
         "actionTime" : "2019-02-01T15:18:00Z"
       } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762392135540,79039899156435,"BM79","2019-02-15T01:00:00Z","GRU","ORD","2019-02-15T11:00:00Z", [ {
         "actionAt" : "GRU",
         "actionCode" : "ONLOAD to ORD",
         "actionTime" : "2019-02-15T01:21:00Z"
       }, {
         "actionAt" : "GRU",
         "actionCode" : "BagTag Scan at GRU",
         "actionTime" : "2019-02-15T00:55:00Z"
       }, {
         "actionAt" : "GRU",
         "actionCode" : "Checkin at GRU",
         "actionTime" : "2019-02-14T23:49:00Z"
       } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762392135540,79039899156435,"BM907","2019-02-15T01:21:00Z","ORD","SEA","2019-02-15T21:22:00Z", [ {
        "actionAt" : "SEA",
        "actionCode" : "Offload to Carousel at SEA",
        "actionTime" : "2019-02-15T21:16:00Z"
      }, {
        "actionAt" : "ORD",
        "actionCode" : "ONLOAD to SEA",
        "actionTime" : "2019-02-15T20:52:00Z"
      }, {
        "actionAt" : "ORD",
        "actionCode" : "OFFLOAD from ORD",
        "actionTime" : "2019-02-15T20:44:00Z"
      } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762376407826,7903989918469,"BM495","2019-03-07T07:00:00Z","JFK","MAD","2019-03-07T14:00:00Z", [ {
        "actionAt" : "MAD",
        "actionCode" : "Offload to Carousel at MAD",
        "actionTime" : "2019-03-07T13:54:00Z"
      }, {
        "actionAt" : "JFK",
        "actionCode" : "ONLOAD to MAD",
        "actionTime" : "2019-03-07T07:00:00Z"
      }, {
        "actionAt" : "JFK",
        "actionCode" : "BagTag Scan at JFK",
        "actionTime" : "2019-03-07T06:53:00Z"
      }, {
        "actionAt" : "JFK",
        "actionCode" : "Checkin at JFK",
        "actionTime" : "2019-03-07T05:03:00Z"
     } ]);


INSERT INTO ticket.bagInfo.flightLegs VALUES(1762355527825,79039899197492,"BM704","2019-03-22T07:00:00Z","BZN","SEA","2019-03-22T09:00:00Z", [ {
        "actionAt" : "BZN",
        "actionCode" : "ONLOAD to SEA",
        "actionTime" : "2019-03-22T07:23:00Z"
      }, {
        "actionAt" : "BZN",
        "actionCode" : "BagTag Scan at BZN",
        "actionTime" : "2019-03-22T06:58:00Z"
      }, {
        "actionAt" : "BZN",
        "actionCode" : "Checkin at BZN",
        "actionTime" : "2019-03-22T05:20:00Z"
      } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762355527825,79039899197492,"BM578","2019-03-22T07:23:00Z","SEA","CDG","2019-03-21T23:24:00Z", [ {
        "actionAt" : "SEA",
        "actionCode" : "ONLOAD to CDG",
        "actionTime" : "2019-03-22T11:26:00Z"
      }, {
        "actionAt" : "SEA",
        "actionCode" : "BagTag Scan at SEA",
        "actionTime" : "2019-03-22T10:57:00Z"
      }, {
        "actionAt" : "SEA",
        "actionCode" : "OFFLOAD from SEA",
        "actionTime" : "2019-03-22T11:07:00Z"
      } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762355527825,79039899197492,"BM386","2019-03-22T07:23:00Z","CDG","MXP","2019-03-22T10:24:00Z", [ {
        "actionAt" : "MXP",
        "actionCode" : "Offload to Carousel at MXP",
        "actionTime" : "2019-03-22T10:15:00Z"
      }, {
        "actionAt" : "CDG",
        "actionCode" : "ONLOAD to MXP",
        "actionTime" : "2019-03-22T10:09:00Z"
      }, {
        "actionAt" : "CDG",
        "actionCode" : "OFFLOAD from CDG",
        "actionTime" : "2019-03-22T10:01:00Z"
      } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762324912391,79039899168383,"BM936","2019-03-15T08:00:00Z","MXP","CDG","2019-03-15T09:00:00Z", [ {
        "actionAt" : "MXP",
        "actionCode" : "ONLOAD to CDG",
        "actionTime" : "2019-03-15T08:13:00Z"
      }, {
        "actionAt" : "MXP",
        "actionCode" : "BagTag Scan at MXP",
        "actionTime" : "2019-03-15T07:48:00Z"
      }, {
        "actionAt" : "MXP",
        "actionCode" : "Checkin at MXP",
        "actionTime" : "2019-03-15T07:38:00Z"
      } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762324912391,79039899168383,"BM490","2019-03-15T08:13:00Z","CDG","SLC","2019-03-15T10:14:00Z",[ {
        "actionAt" : "CDG",
        "actionCode" : "ONLOAD to SLC",
        "actionTime" : "2019-03-15T09:42:00Z"
      }, {
        "actionAt" : "CDG",
        "actionCode" : "BagTag Scan at CDG",
        "actionTime" : "2019-03-15T09:17:00Z"
      }, {
        "actionAt" : "CDG",
        "actionCode" : "OFFLOAD from CDG",
        "actionTime" : "2019-03-15T09:19:00Z"
      } ]);

INSERT INTO ticket.bagInfo.flightLegs VALUES(1762324912391,79039899168383,"BM170","2019-03-15T08:13:00Z","SLC","BZN","2019-03-15T10:14:00Z",[ {
        "actionAt" : "BZN",
        "actionCode" : "Offload to Carousel at BZN",
        "actionTime" : "2019-03-15T10:13:00Z"
      }, {
        "actionAt" : "SLC",
        "actionCode" : "ONLOAD to BZN",
        "actionTime" : "2019-03-15T10:06:00Z"
      }, {
        "actionAt" : "SLC",
        "actionCode" : "OFFLOAD from SLC",
        "actionTime" : "2019-03-15T09:59:00Z"
      } ]);

###Insert into ticket.passengerInfo table ###
INSERT INTO ticket.passengerInfo VALUES(1762344493810,"893-324-1064","Adam Phillips","M");
INSERT INTO ticket.passengerInfo VALUES(1762392135540,"421-272-8082","Adelaide Willard","M");
INSERT INTO ticket.passengerInfo VALUES(1762376407826,"165-742-5715","Dierdre Amador","M");
INSERT INTO ticket.passengerInfo VALUES(1762355527825,"289-564-3497","Doris Martin","F");
INSERT INTO ticket.passengerInfo VALUES(1762324912391,"600-918-8404","Elane Lemons","F");
