### CREATE table if not present ###
CREATE TABLE IF NOT EXISTS BaggageInfo (
ticketNo LONG,
fullName STRING,
gender STRING,
contactPhone STRING,
confNo STRING,
bagInfo JSON,
PRIMARY KEY (ticketNo)
);

### row 1 ###
INSERT INTO BaggageInfo VALUES(
1762344493810,
"Adam Phillips",
"M",
"893-324-1064",
"LE6J4Z",
[ {
    "id" : "79039899165297",
    "tagNum" : "17657806255240",
    "routing" : "MIA/LAX/MEL",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "MEL",
    "flightLegs" : [ {
      "flightNo" : "BM604",
      "flightDate" : "2019-02-01T06:00:00Z",
      "fltRouteSrc" : "MIA",
      "fltRouteDest" : "LAX",
      "estimatedArrival" : "2019-02-01T11:00:00Z",
      "actions" : [ {
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
      } ]
    }, {
      "flightNo" : "BM667",
      "flightDate" : "2019-02-01T06:13:00Z",
      "fltRouteSrc" : "LAX",
      "fltRouteDest" : "MEL",
      "estimatedArrival" : "2019-02-01T16:15:00Z",
      "actions" : [ {
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
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-01T16:13:00Z",
    "bagArrivalDate" : "2019-02-01T16:13:00Z"
  } ]
);

### row 2 ###
INSERT INTO BaggageInfo VALUES(
1762392135540,
"Adelaide Willard",
"M",
"421-272-8082",
"DN3I4Q",
[{
    "id" : "79039899156435",
    "tagNum" : "17657806224224",
    "routing" : "GRU/ORD/SEA",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "SEA",
    "flightLegs" : [ {
      "flightNo" : "BM79",
      "flightDate" : "2019-02-15T01:00:00Z",
      "fltRouteSrc" : "GRU",
      "fltRouteDest" : "ORD",
      "estimatedArrival" : "2019-02-15T11:00:00Z",
      "actions" : [ {
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
      } ]
    }, {
      "flightNo" : "BM907",
      "flightDate" : "2019-02-15T01:21:00Z",
      "fltRouteSrc" : "ORD",
      "fltRouteDest" : "SEA",
      "estimatedArrival" : "2019-02-15T21:22:00Z",
      "actions" : [ {
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
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-15T21:21:00Z",
    "bagArrivalDate" : "2019-02-15T21:21:00Z"
  } ]
);

### row 3 ###
INSERT INTO BaggageInfo VALUES(
1762376407826,
"Dierdre Amador",
"M",
"165-742-5715",
"ZG8Z5N",
[ {
    "id" : "7903989918469",
    "tagNum" : "17657806240229",
    "routing" : "JFK/MAD",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "MAD",
    "flightLegs" : [ {
      "flightNo" : "BM495",
      "flightDate" : "2019-03-07T07:00:00Z",
      "fltRouteSrc" : "JFK",
      "fltRouteDest" : "MAD",
      "estimatedArrival" : "2019-03-07T14:00:00Z",
      "actions" : [ {
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
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-07T13:51:00Z",
    "bagArrivalDate" : "2019-03-07T13:51:00Z"
  } ]
);

### row 4 ###
INSERT INTO BaggageInfo VALUES(
1762355527825,
"Doris Martin",
"F",
"289-564-3497",
"HJ4J4P",
[{
    "id" : "79039899197492",
    "tagNum" : "17657806232501",
    "routing" : "BZN/SEA/CDG/MXP",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "MXP",
    "flightLegs" : [ {
      "flightNo" : "BM704",
      "flightDate" : "2019-03-22T07:00:00Z",
      "fltRouteSrc" : "BZN",
      "fltRouteDest" : "SEA",
      "estimatedArrival" : "2019-03-22T09:00:00Z",
      "actions" : [ {
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
      } ]
    }, {
      "flightNo" : "BM578",
      "flightDate" : "2019-03-22T07:23:00Z",
      "fltRouteSrc" : "SEA",
      "fltRouteDest" : "CDG",
      "estimatedArrival" : "2019-03-21T23:24:00Z",
      "actions" : [ {
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
      } ]
    }, {
      "flightNo" : "BM386",
      "flightDate" : "2019-03-22T07:23:00Z",
      "fltRouteSrc" : "CDG",
      "fltRouteDest" : "MXP",
      "estimatedArrival" : "2019-03-22T10:24:00Z",
      "actions" : [ {
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
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-22T10:17:00Z",
    "bagArrivalDate" : "2019-03-22T10:17:00Z"
}]
);

### row 5 ###
INSERT INTO BaggageInfo VALUES(
1762324912391,
"Elane Lemons",
"F",
"600-918-8404",
"LN0C8R",
[ {
    "id" : "79039899168383",
    "tagNum" : "1765780623244",
    "routing" : "MXP/CDG/SLC/BZN",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "BZN",
    "flightLegs" : [ {
      "flightNo" : "BM936",
      "flightDate" : "2019-03-15T08:00:00Z",
      "fltRouteSrc" : "MXP",
      "fltRouteDest" : "CDG",
      "estimatedArrival" : "2019-03-15T09:00:00Z",
      "actions" : [ {
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
      } ]
    }, {
      "flightNo" : "BM490",
      "flightDate" : "2019-03-15T08:13:00Z",
      "fltRouteSrc" : "CDG",
      "fltRouteDest" : "SLC",
      "estimatedArrival" : "2019-03-15T10:14:00Z",
      "actions" : [ {
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
      } ]
    }, {
      "flightNo" : "BM170",
      "flightDate" : "2019-03-15T08:13:00Z",
      "fltRouteSrc" : "SLC",
      "fltRouteDest" : "BZN",
      "estimatedArrival" : "2019-03-15T10:14:00Z",
      "actions" : [ {
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
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-15T10:13:00Z",
    "bagArrivalDate" : "2019-03-15T10:13:00Z"
  } ]
);


### row 6 ###
INSERT INTO BaggageInfo VALUES(
1762350390409,
"Fallon Clements",
"M",
"849-731-1334",
"XT1O7T",
[ {
    "id" : "79039899117466",
    "tagNum" : "17657806255507",
    "routing" : "MXP/CDG/SLC/BZN",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "BZN",
    "flightLegs" : [ {
      "flightNo" : "BM936",
      "flightDate" : "2019-02-21T12:00:00Z",
      "fltRouteSrc" : "MXP",
      "fltRouteDest" : "CDG",
      "estimatedArrival" : "2019-02-21T13:00:00Z",
      "actions" : [ {
        "actionAt" : "MXP",
        "actionCode" : "ONLOAD to CDG",
        "actionTime" : "2019-02-21T12:19:00Z"
      }, {
        "actionAt" : "MXP",
        "actionCode" : "BagTag Scan at MXP",
        "actionTime" : "2019-02-20T23:51:00Z"
      }, {
        "actionAt" : "MXP",
        "actionCode" : "Checkin at MXP",
        "actionTime" : "2019-02-21T10:51:00Z"
      } ]
    }, {
      "flightNo" : "BM490",
      "flightDate" : "2019-02-21T12:19:00Z",
      "fltRouteSrc" : "CDG",
      "fltRouteDest" : "SLC",
      "estimatedArrival" : "2019-02-21T14:19:00Z",
      "actions" : [ {
        "actionAt" : "CDG",
        "actionCode" : "ONLOAD to SLC",
        "actionTime" : "2019-02-21T14:05:00Z"
      }, {
        "actionAt" : "CDG",
        "actionCode" : "BagTag Scan at CDG",
        "actionTime" : "2019-02-21T13:41:00Z"
      }, {
        "actionAt" : "CDG",
        "actionCode" : "OFFLOAD from CDG",
        "actionTime" : "2019-02-21T13:49:00Z"
      } ]
    }, {
      "flightNo" : "BM170",
      "flightDate" : "2019-02-21T12:19:00Z",
      "fltRouteSrc" : "SLC",
      "fltRouteDest" : "BZN",
      "estimatedArrival" : "2019-02-21T14:20:00Z",
      "actions" : [ {
        "actionAt" : "BZN",
        "actionCode" : "Offload to Carousel at BZN",
        "actionTime" : "2019-02-21T14:15:00Z"
      }, {
        "actionAt" : "SLC",
        "actionCode" : "ONLOAD to BZN",
        "actionTime" : "2019-02-21T14:34:00Z"
      }, {
        "actionAt" : "SLC",
        "actionCode" : "OFFLOAD from SLC",
        "actionTime" : "2019-02-21T14:11:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-21T14:08:00Z",
    "bagArrivalDate" : "2019-02-21T14:08:00Z"
  } ]
);

### row 7 ###
INSERT INTO BaggageInfo VALUES(
1762341772625,
"Gerard Greene",
"M",
"395-837-3772",
"MC0E7R",
[ {
    "id" : "79039899152842",
    "tagNum" : "1765780626568",
    "routing" : "SFO/IST/ATH/JTR",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "JTR",
    "flightLegs" : [ {
      "flightNo" : "BM318",
      "flightDate" : "2019-03-07T04:00:00Z",
      "fltRouteSrc" : "SFO",
      "fltRouteDest" : "IST",
      "estimatedArrival" : "2019-03-07T17:00:00Z",
      "actions" : [ {
        "actionAt" : "SFO",
        "actionCode" : "ONLOAD to IST",
        "actionTime" : "2019-03-07T04:08:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "BagTag Scan at SFO",
        "actionTime" : "2019-03-07T03:53:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "Checkin at SFO",
        "actionTime" : "2019-03-07T02:20:00Z"
      } ]
    }, {
      "flightNo" : "BM696",
      "flightDate" : "2019-03-07T05:08:00Z",
      "fltRouteSrc" : "IST",
      "fltRouteDest" : "ATH",
      "estimatedArrival" : "2019-03-08T04:10:00Z",
      "actions" : [ {
        "actionAt" : "IST",
        "actionCode" : "ONLOAD to ATH",
        "actionTime" : "2019-03-08T04:55:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "BagTag Scan at IST",
        "actionTime" : "2019-03-08T04:34:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "OFFLOAD from IST",
        "actionTime" : "2019-03-08T04:47:00Z"
      } ]
    }, {
      "flightNo" : "BM665",
      "flightDate" : "2019-03-07T04:08:00Z",
      "fltRouteSrc" : "ATH",
      "fltRouteDest" : "JTR",
      "estimatedArrival" : "2019-03-07T16:10:00Z",
      "actions" : [ {
        "actionAt" : "JTR",
        "actionCode" : "Offload to Carousel at JTR",
        "actionTime" : "2019-03-07T16:09:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "ONLOAD to JTR",
        "actionTime" : "2019-03-07T15:51:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "OFFLOAD from ATH",
        "actionTime" : "2019-03-07T15:43:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-07T16:01:00Z",
    "bagArrivalDate" : "2019-03-07T16:01:00Z"
  } ]
);


### row 8 ###
INSERT INTO BaggageInfo VALUES(
176234463813,
"Henry Jenkins",
"F",
"960-428-3843",
"MZ2S5R",
[ {
    "id" : "79039899129693",
    "tagNum" : "17657806216554",
    "routing" : "SFO/ORD/FRA",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "FRA",
    "flightLegs" : [ {
      "flightNo" : "BM572",
      "flightDate" : "2019-03-02T05:00:00Z",
      "fltRouteSrc" : "SFO",
      "fltRouteDest" : "ORD",
      "estimatedArrival" : "2019-03-02T09:00:00Z",
      "actions" : [ {
        "actionAt" : "SFO",
        "actionCode" : "ONLOAD to ORD",
        "actionTime" : "2019-03-02T05:24:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "BagTag Scan at SFO",
        "actionTime" : "2019-03-02T04:52:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "Checkin at SFO",
        "actionTime" : "2019-03-02T03:28:00Z"
      } ]
    }, {
      "flightNo" : "BM582",
      "flightDate" : "2019-03-02T05:24:00Z",
      "fltRouteSrc" : "ORD",
      "fltRouteDest" : "FRA",
      "estimatedArrival" : "2019-03-02T13:24:00Z",
      "actions" : [ {
        "actionAt" : "FRA",
        "actionCode" : "Offload to Carousel at FRA",
        "actionTime" : "2019-03-02T13:20:00Z"
      }, {
        "actionAt" : "ORD",
        "actionCode" : "ONLOAD to FRA",
        "actionTime" : "2019-03-02T12:54:00Z"
      }, {
        "actionAt" : "ORD",
        "actionCode" : "OFFLOAD from ORD",
        "actionTime" : "2019-03-02T12:30:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-02T13:18:00Z",
    "bagArrivalDate" : "2019-03-02T13:18:00Z"
  } ]
);

### row 9 ###
INSERT INTO BaggageInfo VALUES(
1762383911861,
"Joanne Diaz",
"F",
"334-679-5105",
"CG6O1M",
[ {
    "id" : "7903989911419",
    "tagNum" : "17657806292518",
    "routing" : "MIA/LAX/MEL",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "MEL",
    "flightLegs" : [ {
      "flightNo" : "BM604",
      "flightDate" : "2019-02-16T06:00:00Z",
      "fltRouteSrc" : "MIA",
      "fltRouteDest" : "LAX",
      "estimatedArrival" : "2019-02-16T11:00:00Z",
      "actions" : [ {
        "actionAt" : "MIA",
        "actionCode" : "ONLOAD to LAX",
        "actionTime" : "2019-02-16T06:24:00Z"
      }, {
        "actionAt" : "MIA",
        "actionCode" : "BagTag Scan at MIA",
        "actionTime" : "2019-02-16T05:55:00Z"
      }, {
        "actionAt" : "MIA",
        "actionCode" : "Checkin at MIA",
        "actionTime" : "2019-02-16T05:27:00Z"
      } ]
    }, {
      "flightNo" : "BM667",
      "flightDate" : "2019-02-16T06:24:00Z",
      "fltRouteSrc" : "LAX",
      "fltRouteDest" : "MEL",
      "estimatedArrival" : "2019-02-16T16:26:00Z",
      "actions" : [ {
        "actionAt" : "MEL",
        "actionCode" : "Offload to Carousel at MEL",
        "actionTime" : "2019-02-16T16:22:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "ONLOAD to MEL",
        "actionTime" : "2019-02-16T16:15:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "OFFLOAD from LAX",
        "actionTime" : "2019-02-16T16:00:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-16T16:13:00Z",
    "bagArrivalDate" : "2019-02-16T16:13:00Z"
  } ]
);


### row 10 ###
INSERT INTO BaggageInfo VALUES(
1762377974281,
"Kendal Biddle",
"F",
"619-956-8760",
"PQ1M8N",
[ {
    "id" : "79039899189080",
    "tagNum" : "17657806296887",
    "routing" : "JFK/IST/VIE",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "VIE",
    "flightLegs" : [ {
      "flightNo" : "BM127",
      "flightDate" : "2019-03-04T06:00:00Z",
      "fltRouteSrc" : "JFK",
      "fltRouteDest" : "IST",
      "estimatedArrival" : "2019-03-04T22:00:00Z",
      "actions" : [ {
        "actionAt" : "JFK",
        "actionCode" : "ONLOAD to IST",
        "actionTime" : "2019-03-04T06:02:00Z"
      }, {
        "actionAt" : "JFK",
        "actionCode" : "BagTag Scan at JFK",
        "actionTime" : "2019-03-04T05:47:00Z"
      }, {
        "actionAt" : "JFK",
        "actionCode" : "Checkin at JFK",
        "actionTime" : "2019-03-04T05:16:00Z"
      } ]
    }, {
      "flightNo" : "BM431",
      "flightDate" : "2019-03-04T07:02:00Z",
      "fltRouteSrc" : "IST",
      "fltRouteDest" : "VIE",
      "estimatedArrival" : "2019-03-05T12:02:00Z",
      "actions" : [ {
        "actionAt" : "VIE",
        "actionCode" : "Offload to Carousel at VIE",
        "actionTime" : "2019-03-04T23:58:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "ONLOAD to VIE",
        "actionTime" : "2019-03-05T12:55:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "OFFLOAD from IST",
        "actionTime" : "2019-03-05T12:55:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-05T12:00:00Z",
    "bagArrivalDate" : "2019-03-05T12:00:00Z"
  } ]
);

### row 11 ###
INSERT INTO BaggageInfo VALUES(
1762355854464,
"Lisbeth Wampler",
"M",
"796-709-9501",
"BO5G3H",
[ {
    "id" : "79039899169442",
    "tagNum" : "17657806292229",
    "routing" : "LAX/TPE/SGN",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "SGN",
    "flightLegs" : [ {
      "flightNo" : "BM720",
      "flightDate" : "2019-02-09T06:00:00Z",
      "fltRouteSrc" : "LAX",
      "fltRouteDest" : "TPE",
      "estimatedArrival" : "2019-02-10T10:00:00Z",
      "actions" : [ {
        "actionAt" : "LAX",
        "actionCode" : "ONLOAD to TPE",
        "actionTime" : "2019-02-09T06:01:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "BagTag Scan at LAX",
        "actionTime" : "2019-02-09T05:46:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "Checkin at LAX",
        "actionTime" : "2019-02-09T04:38:00Z"
      } ]
    }, {
      "flightNo" : "BM668",
      "flightDate" : "2019-02-09T20:01:00Z",
      "fltRouteSrc" : "TPE",
      "fltRouteDest" : "SGN",
      "estimatedArrival" : "2019-02-10T10:01:00Z",
      "actions" : [ {
        "actionAt" : "SGN",
        "actionCode" : "Offload to Carousel at SGN",
        "actionTime" : "2019-02-10T09:58:00Z"
      }, {
        "actionAt" : "TPE",
        "actionCode" : "ONLOAD to SGN",
        "actionTime" : "2019-02-10T23:44:00Z"
      }, {
        "actionAt" : "TPE",
        "actionCode" : "OFFLOAD from TPE",
        "actionTime" : "2019-02-10T23:40:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-10T10:01:00Z",
    "bagArrivalDate" : "2019-02-10T10:01:00Z"
  } ]
);

### row 12 ###
INSERT INTO BaggageInfo VALUES(
1762320569757,
"Lucinda Beckman",
"M",
"364-610-4444",
"QI3V6Q",
[ {
    "id" : "79039899187755",
    "tagNum" : "17657806240001",
    "routing" : "SFO/IST/ATH/JTR",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "JTR",
    "flightLegs" : [ {
      "flightNo" : "BM318",
      "flightDate" : "2019-03-12T03:00:00Z",
      "fltRouteSrc" : "SFO",
      "fltRouteDest" : "IST",
      "estimatedArrival" : "2019-03-12T16:00:00Z",
      "actions" : [ {
        "actionAt" : "SFO",
        "actionCode" : "ONLOAD to IST",
        "actionTime" : "2019-03-12T03:11:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "BagTag Scan at SFO",
        "actionTime" : "2019-03-12T02:49:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "Checkin at SFO",
        "actionTime" : "2019-03-12T01:50:00Z"
      } ]
    }, {
      "flightNo" : "BM696",
      "flightDate" : "2019-03-12T04:11:00Z",
      "fltRouteSrc" : "IST",
      "fltRouteDest" : "ATH",
      "estimatedArrival" : "2019-03-13T03:14:00Z",
      "actions" : [ {
        "actionAt" : "IST",
        "actionCode" : "ONLOAD to ATH",
        "actionTime" : "2019-03-13T04:10:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "BagTag Scan at IST",
        "actionTime" : "2019-03-13T03:56:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "OFFLOAD from IST",
        "actionTime" : "2019-03-13T03:59:00Z"
      } ]
    }, {
      "flightNo" : "BM665",
      "flightDate" : "2019-03-12T03:11:00Z",
      "fltRouteSrc" : "ATH",
      "fltRouteDest" : "JTR",
      "estimatedArrival" : "2019-03-12T15:12:00Z",
      "actions" : [ {
        "actionAt" : "JTR",
        "actionCode" : "Offload to Carousel at JTR",
        "actionTime" : "2019-03-12T15:06:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "ONLOAD to JTR",
        "actionTime" : "2019-03-12T14:16:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "OFFLOAD from ATH",
        "actionTime" : "2019-03-12T14:13:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-12T15:05:00Z",
    "bagArrivalDate" : "2019-03-12T15:05:00Z"
  } ]
);


### row 13 ###
INSERT INTO BaggageInfo VALUES(
1762340683564,
"Mary Watson",
"F",
"131-183-0560",
"KN4D1L",
[ {
    "id" : "7903989918647",
    "tagNum" : "17657806299833",
    "routing" : "YYZ/HKG/BLR",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "BLR",
    "flightLegs" : [ {
      "flightNo" : "BM267",
      "flightDate" : "2019-03-13T00:00:00Z",
      "fltRouteSrc" : "YYZ",
      "fltRouteDest" : "HKG",
      "estimatedArrival" : "2019-03-13T15:00:00Z",
      "actions" : [ {
        "actionAt" : "YYZ",
        "actionCode" : "ONLOAD to HKG",
        "actionTime" : "2019-03-13T00:21:00Z"
      }, {
        "actionAt" : "YYZ",
        "actionCode" : "BagTag Scan at YYZ",
        "actionTime" : "2019-03-12T23:58:00Z"
      }, {
        "actionAt" : "YYZ",
        "actionCode" : "Checkin at YYZ",
        "actionTime" : "2019-03-12T23:10:00Z"
      } ]
    }, {
      "flightNo" : "BM115",
      "flightDate" : "2019-03-13T00:21:00Z",
      "fltRouteSrc" : "HKG",
      "fltRouteDest" : "BLR",
      "estimatedArrival" : "2019-03-14T06:22:00Z",
      "actions" : [ {
        "actionAt" : "BLR",
        "actionCode" : "Offload to Carousel at BLR",
        "actionTime" : "2019-03-14T06:20:00Z"
      }, {
        "actionAt" : "HKG",
        "actionCode" : "ONLOAD to BLR",
        "actionTime" : "2019-03-14T05:42:00Z"
      }, {
        "actionAt" : "HKG",
        "actionCode" : "OFFLOAD from HKG",
        "actionTime" : "2019-03-14T05:38:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-14T06:22:00Z",
    "bagArrivalDate" : "2019-03-14T06:22:00Z"
  } ]
);

### row 14 ###
INSERT INTO BaggageInfo VALUES(
1762330498104,
"Michelle Payne",
"F",
"575-781-6240",
"RL3J4Q",
[ {
    "id" : "79039899186259",
    "tagNum" : "17657806247861",
    "routing" : "SFO/IST/ATH/JTR",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "JTR",
    "flightLegs" : [ {
      "flightNo" : "BM318",
      "flightDate" : "2019-02-02T12:00:00Z",
      "fltRouteSrc" : "SFO",
      "fltRouteDest" : "IST",
      "estimatedArrival" : "2019-02-03T01:00:00Z",
      "actions" : [ {
        "actionAt" : "SFO",
        "actionCode" : "ONLOAD to IST",
        "actionTime" : "2019-02-02T12:10:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "BagTag Scan at SFO",
        "actionTime" : "2019-02-02T11:47:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "Checkin at SFO",
        "actionTime" : "2019-02-02T10:01:00Z"
      } ]
    }, {
      "flightNo" : "BM696",
      "flightDate" : "2019-02-02T13:10:00Z",
      "fltRouteSrc" : "IST",
      "fltRouteDest" : "ATH",
      "estimatedArrival" : "2019-02-03T12:12:00Z",
      "actions" : [ {
        "actionAt" : "IST",
        "actionCode" : "ONLOAD to ATH",
        "actionTime" : "2019-02-03T13:06:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "BagTag Scan at IST",
        "actionTime" : "2019-02-03T12:48:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "OFFLOAD from IST",
        "actionTime" : "2019-02-03T13:00:00Z"
      } ]
    }, {
      "flightNo" : "BM665",
      "flightDate" : "2019-02-02T12:10:00Z",
      "fltRouteSrc" : "ATH",
      "fltRouteDest" : "JTR",
      "estimatedArrival" : "2019-02-03T00:12:00Z",
      "actions" : [ {
        "actionAt" : "JTR",
        "actionCode" : "Offload to Carousel at JTR",
        "actionTime" : "2019-02-03T00:06:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "ONLOAD to JTR",
        "actionTime" : "2019-02-03T00:13:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "OFFLOAD from ATH",
        "actionTime" : "2019-02-03T00:10:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-02T23:59:00Z",
    "bagArrivalDate" : "2019-02-02T23:59:00Z"
  } ]
);


### row 15 ###
INSERT INTO BaggageInfo VALUES(
1762348904343,
"Omar Harvey",
"F",
"978-191-8550",
"OH2F8U",
[ {
    "id" : "79039899149056",
    "tagNum" : "17657806234185",
    "routing" : "MEL/LAX/MIA",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "MIA",
    "flightLegs" : [ {
      "flightNo" : "BM114",
      "flightDate" : "2019-03-01T12:00:00Z",
      "fltRouteSrc" : "MEL",
      "fltRouteDest" : "LAX",
      "estimatedArrival" : "2019-03-02T02:00:00Z",
      "actions" : [ {
        "actionAt" : "MEL",
        "actionCode" : "ONLOAD to LAX",
        "actionTime" : "2019-03-01T12:20:00Z"
      }, {
        "actionAt" : "MEL",
        "actionCode" : "BagTag Scan at MEL",
        "actionTime" : "2019-03-01T11:52:00Z"
      }, {
        "actionAt" : "MEL",
        "actionCode" : "Checkin at MEL",
        "actionTime" : "2019-03-01T11:43:00Z"
      } ]
    }, {
      "flightNo" : "BM866",
      "flightDate" : "2019-03-01T12:20:00Z",
      "fltRouteSrc" : "LAX",
      "fltRouteDest" : "MIA",
      "estimatedArrival" : "2019-03-02T16:21:00Z",
      "actions" : [ {
        "actionAt" : "MIA",
        "actionCode" : "Offload to Carousel at MIA",
        "actionTime" : "2019-03-02T16:18:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "ONLOAD to MIA",
        "actionTime" : "2019-03-02T16:12:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "OFFLOAD from LAX",
        "actionTime" : "2019-03-02T16:02:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-02T16:09:00Z",
    "bagArrivalDate" : "2019-03-02T16:09:00Z"
  } ]
);

### row 16 ###
INSERT INTO BaggageInfo VALUES(
1762399766476,
"Raymond Griffin",
"F",
"567-710-9972",
"XT6K7M",
[ {
    "id" : "79039899179672",
    "tagNum" : "17657806243578",
    "routing" : "MSQ/FRA/HKG",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "HKG",
    "flightLegs" : [ {
      "flightNo" : "BM365",
      "flightDate" : "2019-02-03T04:00:00Z",
      "fltRouteSrc" : "MSQ",
      "fltRouteDest" : "FRA",
      "estimatedArrival" : "2019-02-03T06:00:00Z",
      "actions" : [ {
        "actionAt" : "MSQ",
        "actionCode" : "ONLOAD to FRA",
        "actionTime" : "2019-02-03T04:21:00Z"
      }, {
        "actionAt" : "MSQ",
        "actionCode" : "BagTag Scan at MSQ",
        "actionTime" : "2019-02-03T03:49:00Z"
      }, {
        "actionAt" : "MSQ",
        "actionCode" : "Checkin at MSQ",
        "actionTime" : "2019-02-03T02:50:00Z"
      } ]
    }, {
      "flightNo" : "BM313",
      "flightDate" : "2019-02-03T04:21:00Z",
      "fltRouteSrc" : "FRA",
      "fltRouteDest" : "HKG",
      "estimatedArrival" : "2019-02-03T08:22:00Z",
      "actions" : [ {
        "actionAt" : "HKG",
        "actionCode" : "Offload to Carousel at HKG",
        "actionTime" : "2019-02-03T08:15:00Z"
      }, {
        "actionAt" : "FRA",
        "actionCode" : "ONLOAD to HKG",
        "actionTime" : "2019-02-03T07:36:00Z"
      }, {
        "actionAt" : "FRA",
        "actionCode" : "OFFLOAD from FRA",
        "actionTime" : "2019-02-03T07:23:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-03T08:09:00Z",
    "bagArrivalDate" : "2019-02-03T08:09:00Z"
  } ]
);


### row 17 ###
INSERT INTO BaggageInfo VALUES(
1762311547917,
"Rosalia Triplett",
"F",
"368-769-5636",
"FH7G1W",
[ {
    "id" : "79039899194559",
    "tagNum" : "17657806215913",
    "routing" : "JFK/IST/VIE",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "VIE",
    "flightLegs" : [ {
      "flightNo" : "BM127",
      "flightDate" : "2019-02-11T01:00:00Z",
      "fltRouteSrc" : "JFK",
      "fltRouteDest" : "IST",
      "estimatedArrival" : "2019-02-11T17:00:00Z",
      "actions" : [ {
        "actionAt" : "JFK",
        "actionCode" : "ONLOAD to IST",
        "actionTime" : "2019-02-11T01:12:00Z"
      }, {
        "actionAt" : "JFK",
        "actionCode" : "BagTag Scan at JFK",
        "actionTime" : "2019-02-11T00:48:00Z"
      }, {
        "actionAt" : "JFK",
        "actionCode" : "Checkin at JFK",
        "actionTime" : "2019-02-10T23:39:00Z"
      } ]
    }, {
      "flightNo" : "BM431",
      "flightDate" : "2019-02-11T02:12:00Z",
      "fltRouteSrc" : "IST",
      "fltRouteDest" : "VIE",
      "estimatedArrival" : "2019-02-12T07:12:00Z",
      "actions" : [ {
        "actionAt" : "VIE",
        "actionCode" : "Offload to Carousel at VIE",
        "actionTime" : "2019-02-12T07:08:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "ONLOAD to VIE",
        "actionTime" : "2019-02-12T07:52:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "OFFLOAD from IST",
        "actionTime" : "2019-02-12T07:31:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-12T07:04:00Z",
    "bagArrivalDate" : "2019-02-12T07:04:00Z"
  } ]
);


### row 18 ###
INSERT INTO BaggageInfo VALUES(
1762357254392,
"Teena Colley",
"M",
"539-097-5220",
"TX1P7E",
[ {
    "id" : "79039899153973",
    "tagNum" : "17657806255823",
    "routing" : "MSQ/FRA/HKG",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "HKG",
    "flightLegs" : [ {
      "flightNo" : "BM365",
      "flightDate" : "2019-02-13T07:00:00Z",
      "fltRouteSrc" : "MSQ",
      "fltRouteDest" : "FRA",
      "estimatedArrival" : "2019-02-13T09:00:00Z",
      "actions" : [ {
        "actionAt" : "MSQ",
        "actionCode" : "ONLOAD to FRA",
        "actionTime" : "2019-02-13T07:17:00Z"
      }, {
        "actionAt" : "MSQ",
        "actionCode" : "BagTag Scan at MSQ",
        "actionTime" : "2019-02-13T06:52:00Z"
      }, {
        "actionAt" : "MSQ",
        "actionCode" : "Checkin at MSQ",
        "actionTime" : "2019-02-13T06:11:00Z"
      } ]
    }, {
      "flightNo" : "BM313",
      "flightDate" : "2019-02-13T07:17:00Z",
      "fltRouteSrc" : "FRA",
      "fltRouteDest" : "HKG",
      "estimatedArrival" : "2019-02-13T11:18:00Z",
      "actions" : [ {
        "actionAt" : "HKG",
        "actionCode" : "Offload to Carousel at HKG",
        "actionTime" : "2019-02-13T11:15:00Z"
      }, {
        "actionAt" : "FRA",
        "actionCode" : "ONLOAD to HKG",
        "actionTime" : "2019-02-13T10:39:00Z"
      }, {
        "actionAt" : "FRA",
        "actionCode" : "OFFLOAD from FRA",
        "actionTime" : "2019-02-13T10:37:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-13T11:15:00Z",
    "bagArrivalDate" : "2019-02-13T11:15:00Z"
  } ]
);

### row 19 ###
INSERT INTO BaggageInfo VALUES(
1762390789239,
"Zina Christenson",
"M",
"987-210-3029",
"QB1O0J",
[ {
    "id" : "79039899145397",
    "tagNum" : "17657806228676",
    "routing" : "MIA/LAX/MEL",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "MEL",
    "flightLegs" : [ {
      "flightNo" : "BM604",
      "flightDate" : "2019-02-04T00:00:00Z",
      "fltRouteSrc" : "MIA",
      "fltRouteDest" : "LAX",
      "estimatedArrival" : "2019-02-04T05:00:00Z",
      "actions" : [ {
        "actionAt" : "MIA",
        "actionCode" : "ONLOAD to LAX",
        "actionTime" : "2019-02-04T00:12:00Z"
      }, {
        "actionAt" : "MIA",
        "actionCode" : "BagTag Scan at MIA",
        "actionTime" : "2019-02-03T23:52:00Z"
      }, {
        "actionAt" : "MIA",
        "actionCode" : "Checkin at MIA",
        "actionTime" : "2019-02-03T23:55:00Z"
      } ]
    }, {
      "flightNo" : "BM667",
      "flightDate" : "2019-02-04T00:12:00Z",
      "fltRouteSrc" : "LAX",
      "fltRouteDest" : "MEL",
      "estimatedArrival" : "2019-02-04T10:14:00Z",
      "actions" : [ {
        "actionAt" : "MEL",
        "actionCode" : "Offload to Carousel at MEL",
        "actionTime" : "2019-02-04T10:06:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "ONLOAD to MEL",
        "actionTime" : "2019-02-04T09:41:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "OFFLOAD from LAX",
        "actionTime" : "2019-02-04T09:38:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-04T10:08:00Z",
    "bagArrivalDate" : "2019-02-04T10:08:00Z"
  } ]
);

### row 20 ###
INSERT INTO BaggageInfo VALUES(
1762340579411,
"Zulema Martindale",
"F",
"666-302-0028",
"CR2C8MY",
[ {
    "id" : "79039899150438",
    "tagNum" : "17657806288937",
    "routing" : "MIA/LAX/MEL",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "MEL",
    "flightLegs" : [ {
      "flightNo" : "BM604",
      "flightDate" : "2019-02-25T10:00:00Z",
      "fltRouteSrc" : "MIA",
      "fltRouteDest" : "LAX",
      "estimatedArrival" : "2019-02-25T15:00:00Z",
      "actions" : [ {
        "actionAt" : "MIA",
        "actionCode" : "ONLOAD to LAX",
        "actionTime" : "2019-02-25T10:23:00Z"
      }, {
        "actionAt" : "MIA",
        "actionCode" : "BagTag Scan at MIA",
        "actionTime" : "2019-02-25T09:54:00Z"
      }, {
        "actionAt" : "MIA",
        "actionCode" : "Checkin at MIA",
        "actionTime" : "2019-02-25T09:58:00Z"
      } ]
    }, {
      "flightNo" : "BM667",
      "flightDate" : "2019-02-25T10:23:00Z",
      "fltRouteSrc" : "LAX",
      "fltRouteDest" : "MEL",
      "estimatedArrival" : "2019-02-25T20:25:00Z",
      "actions" : [ {
        "actionAt" : "MEL",
        "actionCode" : "Offload to Carousel at MEL",
        "actionTime" : "2019-02-25T20:16:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "ONLOAD to MEL",
        "actionTime" : "2019-02-25T08:39:00Z"
      }, {
        "actionAt" : "LAX",
        "actionCode" : "OFFLOAD from LAX",
        "actionTime" : "2019-02-25T08:22:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-02-25T20:15:00Z",
    "bagArrivalDate" : "2019-02-25T20:15:00Z"
  } ]
);

### row 21 ###
INSERT INTO BaggageInfo VALUES(
1762320369957,
"Lorenzo Phil",
"M",
"364-610-4444",
"QI3V6Q",
[ {
    "id" : "79039899187755",
    "tagNum" : "17657806240001",
    "routing" : "SFO/IST/ATH/JTR",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "JTR",
    "flightLegs" : [ {
      "flightNo" : "BM318",
      "flightDate" : "2019-03-12T03:00:00Z",
      "fltRouteSrc" : "SFO",
      "fltRouteDest" : "IST",
      "estimatedArrival" : "2019-03-12T16:00:00Z",
      "actions" : [ {
        "actionAt" : "SFO",
        "actionCode" : "ONLOAD to IST",
        "actionTime" : "2019-03-12T03:11:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "BagTag Scan at SFO",
        "actionTime" : "2019-03-12T02:49:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "Checkin at SFO",
        "actionTime" : "2019-03-12T01:50:00Z"
      } ]
    }, {
      "flightNo" : "BM696",
      "flightDate" : "2019-03-12T04:11:00Z",
      "fltRouteSrc" : "IST",
      "fltRouteDest" : "ATH",
      "estimatedArrival" : "2019-03-13T03:14:00Z",
      "actions" : [ {
        "actionAt" : "IST",
        "actionCode" : "ONLOAD to ATH",
        "actionTime" : "2019-03-13T04:10:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "BagTag Scan at IST",
        "actionTime" : "2019-03-13T03:56:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "OFFLOAD from IST",
        "actionTime" : "2019-03-13T03:59:00Z"
      } ]
    }, {
      "flightNo" : "BM665",
      "flightDate" : "2019-03-12T03:11:00Z",
      "fltRouteSrc" : "ATH",
      "fltRouteDest" : "JTR",
      "estimatedArrival" : "2019-03-12T15:12:00Z",
      "actions" : [ {
        "actionAt" : "JTR",
        "actionCode" : "Offload to Carousel at JTR",
        "actionTime" : "2019-03-12T15:06:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "ONLOAD to JTR",
        "actionTime" : "2019-03-12T14:16:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "OFFLOAD from ATH",
        "actionTime" : "2019-03-12T14:13:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-12T15:05:00Z",
    "bagArrivalDate" : "2019-03-12T15:05:00Z"
  },
  {
    "id" : "79039899197755",
    "tagNum" : "17657806340001",
    "routing" : "SFO/IST/ATH/JTR",
    "lastActionCode" : "OFFLOAD",
    "lastActionDesc" : "OFFLOAD",
    "lastSeenStation" : "JTR",
    "flightLegs" : [ {
      "flightNo" : "BM318",
      "flightDate" : "2019-03-12T03:00:00Z",
      "fltRouteSrc" : "SFO",
      "fltRouteDest" : "IST",
      "estimatedArrival" : "2019-03-12T16:40:00Z",
      "actions" : [ {
        "actionAt" : "SFO",
        "actionCode" : "ONLOAD to IST",
        "actionTime" : "2019-03-12T03:14:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "BagTag Scan at SFO",
        "actionTime" : "2019-03-12T02:50:00Z"
      }, {
        "actionAt" : "SFO",
        "actionCode" : "Checkin at SFO",
        "actionTime" : "2019-03-12T01:58:00Z"
      } ]
    }, {
      "flightNo" : "BM696",
      "flightDate" : "2019-03-12T04:11:00Z",
      "fltRouteSrc" : "IST",
      "fltRouteDest" : "ATH",
      "estimatedArrival" : "2019-03-13T03:18:00Z",
      "actions" : [ {
        "actionAt" : "IST",
        "actionCode" : "ONLOAD to ATH",
        "actionTime" : "2019-03-13T04:17:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "BagTag Scan at IST",
        "actionTime" : "2019-03-13T03:59:00Z"
      }, {
        "actionAt" : "IST",
        "actionCode" : "OFFLOAD from IST",
        "actionTime" : "2019-03-13T03:48:00Z"
      } ]
    }, {
      "flightNo" : "BM665",
      "flightDate" : "2019-03-12T03:11:00Z",
      "fltRouteSrc" : "ATH",
      "fltRouteDest" : "JTR",
      "estimatedArrival" : "2019-03-12T15:12:00Z",
      "actions" : [ {
        "actionAt" : "JTR",
        "actionCode" : "Offload to Carousel at JTR",
        "actionTime" : "2019-03-12T15:06:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "ONLOAD to JTR",
        "actionTime" : "2019-03-12T14:16:00Z"
      }, {
        "actionAt" : "ATH",
        "actionCode" : "OFFLOAD from ATH",
        "actionTime" : "2019-03-12T14:23:00Z"
      } ]
    } ],
    "lastSeenTimeGmt" : "2019-03-12T16:05:00Z",
    "bagArrivalDate" : "2019-03-12T16:25:00Z"
  } ]
);