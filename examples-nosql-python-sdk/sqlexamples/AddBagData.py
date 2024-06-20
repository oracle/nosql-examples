# Copyright (c) 2023, 2024 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/
import os
from borneo import (Regions, NoSQLHandle, NoSQLHandleConfig, PutRequest,QueryRequest,
                    TableRequest, GetRequest, TableLimits, State)
from borneo.iam import SignatureProvider
from borneo.kv import StoreAccessTokenProvider

# Given a region, and compartment, instantiate a connection to the
# cloud service and return it
def get_connection_cloud():
   print("Connecting to the Oracle NoSQL Cloud Service")
   # replace the placeholder with your region identifier
   region = '<your_region_identifier>'
   provider = SignatureProvider()
   # If using the DEFAULT profile with the config file in default location
   # ~/.oci/config
   config = NoSQLHandleConfig(region, provider)
   # replace the placeholder with the ocid of your compartment
   config.set_default_compartment("<ocid_of_your_compartment>")
   return(NoSQLHandle(config))

# Given a endpoint, instantiate a connection to onPremise Oracle NoSQL database
def get_connection_onprem():
   # replace the placeholder with the name of your host
   kvstore_endpoint ='http://<hostname>:8080'
   provider = StoreAccessTokenProvider()
   # If using a secure store, uncomment the line below and pass the username,
   # password of the store to StoreAccessTokenProvider
   # provider = StoreAccessTokenProvider(username, password)
   return NoSQLHandle(NoSQLHandleConfig(kvstore_endpoint, provider))

# Create a table and set the table limits
def create_table(handle):
   statement = '''create table if not exists BaggageInfo (ticketNo LONG,
                                                           fullName STRING,
                                                           gender STRING,
                                                           contactPhone STRING,
                                                           confNo STRING,
                                                           bagInfo JSON,
                                                           primary key(ticketNo))'''
   request = TableRequest().set_statement(statement).set_table_limits(TableLimits(20, 10, 1))
   # Create the table, waiting for a total of 40000 milliseconds
   # and polling the service every 3000 milliseconds to see if table is active
   table_result = handle.do_table_request(request, 40000, 3000)
   table_result.wait_for_completion(handle, 40000, 3000)
   if (table_result.get_state() == State.ACTIVE):
      print('Created table: BaggageInfo')
   else:
      raise NameError('Table BaggageInfo is in an unexpected state ' + str(table_result.get_state()))

# Add a row of data to the table
def insert_record(handle,table_name,bag_data):
   request = PutRequest().set_table_name(table_name).set_value_from_json(bag_data)
   handle.put(request)
   print('Loaded a row into table: BaggageInfo')

def main():
   handle = None
   bag1='''{
      "ticketNo":"1762376407826",
      "fullName":"Dierdre Amador",
      "gender":"M",
      "contactPhone":"165-742-5715",
      "confNo":"ZG8Z5N",
      "bagInfo":[ {
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
   }'''
   bag2='''
   {
      "ticketNo":"1762344493810",
      "fullName":"Adam Phillips",
      "gender":"M",
      "contactPhone":"893-324-1064",
      "confNo":"LE6J4Z",
      "bagInfo":[ {
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
   }'''
   bag3='''
   {
      "ticketNo":"1762341772625",
      "fullName":"Gerard Greene",
      "gender":"M",
      "contactPhone":"395-837-3772",
      "confNo":"MC0E7R",
      "bagInfo":[ {
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
   }'''
   bag4='''
   {
      "ticketNo":"1762320369957",
      "fullName":"Lorenzo Phil",
      "gender":"M",
      "contactPhone":"364-610-4444",
      "confNo":"QI3V6Q",
      "bagInfo":[ {
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
   }'''
   # if cloud service uncomment this
   handle = get_connection_cloud()
   # if onPremise uncomment this
   #handle = get_connection_onprem()
   create_table(handle)
   insert_record(handle,'BaggageInfo',bag1)
   insert_record(handle,'BaggageInfo',bag2)
   insert_record(handle,'BaggageInfo',bag3)
   insert_record(handle,'BaggageInfo',bag4)
   if handle is not None:
      handle.close()
   
if __name__ == "__main__":
    main()
