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
   #replace the placeholder with your region identifier
   region = '<your_region_identifier>'
   provider = SignatureProvider()
   #If using the DEFAULT profile with the config file in default location
   # ~/.oci/config
   config = NoSQLHandleConfig(region, provider)
   #replace the placeholder with the ocid of your compartment
   config.set_default_compartment("<ocid_of_your_compartment>")
   return(NoSQLHandle(config))

# Given a endpoint, instantiate a connection to onPremise Oracle NoSQL database
def get_connection_onprem():
   #replace the placeholder with the name of your host
   kvstore_endpoint ='http://<hostname>:8080'
   provider = StoreAccessTokenProvider()
   #If using a secure store, uncomment the line below and pass the username,
   # password of the store to StoreAccessTokenProvider
   #provider = StoreAccessTokenProvider(username, password)
   return NoSQLHandle(NoSQLHandleConfig(kvstore_endpoint, provider))

# Create a table and set the table limits
def create_table(handle,statement,table_flag,table_name):
   if (table_flag == 'true'):
      request = TableRequest().set_statement(statement).set_table_limits(TableLimits(20, 10, 1))
   else:
      request = TableRequest().set_statement(statement)

   # Create the table, waiting for a total of 40000 milliseconds
   # and polling the service every 3000 milliseconds to see if table is active
   table_result = handle.do_table_request(request, 40000, 3000)
   table_result.wait_for_completion(handle, 40000, 3000)
   if (table_result.get_state() == State.ACTIVE):
      print('Created table: ',table_name)
   else:
      raise NameError('Table is in an unexpected state ' + str(table_result.get_state()))

def insert_record(handle,table_name,tbl_data):
   request = PutRequest().set_table_name(table_name).set_value_from_json(tbl_data)
   handle.put(request)
   print('Loaded a row into table:',table_name)

# Fetch data from the table based on joins
def fetch_data(handle,sqlstmt):
   request = QueryRequest().set_statement(sqlstmt)
   print('Query results for: ' + sqlstmt)
   result = handle.query(request)
   for r in result.get_results():
      print('\t' + str(r))

def main():
   handle = None
   # if cloud service uncomment this
   handle = get_connection_cloud()
   # if onPremise uncomment this
   # handle = get_connection_onprem()
   tbl_name='ticket'
   regtbl_crtstmt = '''create table if not exists ticket (ticketNo LONG,
                                                          confNo STRING,
                                                          PRIMARY KEY(ticketNo))'''
   childtbl_name='bagInfo'
   childtbl_crtstmt = '''create table if not exists ticket.bagInfo (id LONG,
                                                                    tagNum LONG,
                                                                    routing STRING,
                                                                    lastActionCode STRING,
                                                                    lastActionDesc STRING,
                                                                    lastSeenStation STRING,
                                                                    lastSeenTimeGmt TIMESTAMP(4),
                                                                    bagArrivalDate TIMESTAMP(4),
                                                                    PRIMARY KEY(id))'''
   desctbl_crtstmt = '''create table if not exists ticket.bagInfo.flightLegs (flightNo STRING,
                                                                               flightDate TIMESTAMP(4),
                                                                               fltRouteSrc STRING,
                                                                               fltRouteDest STRING,
                                                                               estimatedArrival TIMESTAMP(4),
                                                                               actions JSON,
                                                                               PRIMARY KEY(flightNo))'''
   desctbl_name='flightLegs'
   create_table(handle,regtbl_crtstmt,'true',tbl_name)
   create_table(handle,childtbl_crtstmt,'false',childtbl_name)
   create_table(handle,desctbl_crtstmt,'false',desctbl_name)
   data1='''{
      "ticketNo": "1762344493810",
      "confNo" : "LE6J4Z"
   }'''
   data2='''{
      "ticketNo": "1762344493810",
      "id":"79039899165297",
      "tagNum":"17657806255240",
      "routing":"MIA/LAX/MEL",
      "lastActionCode":"OFFLOAD",
      "lastActionDesc":"OFFLOAD",
      "lastSeenStation":"MEL",
      "lastSeenTimeGmt":"2019-02-01T16:13:00Z",
      "bagArrivalDate":"2019-02-01T16:13:00Z"
   }'''
   data3='''{
      "ticketNo":"1762344493810",
      "id":"79039899165297",
      "flightNo":"BM604",
      "flightDate":"2019-02-01T06:00:00Z",
      "fltRouteSrc":"MIA",
      "fltRouteDest":"LAX",
      "estimatedArrival":"2019-02-01T11:00:00Z",
      "actions":[ {
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
   }'''
   insert_record(handle,'ticket',data1)
   insert_record(handle,'ticket.bagInfo',data2)
   insert_record(handle,'ticket.bagInfo.flightLegs',data3)
   sql_stmt_loj='SELECT * FROM ticket a LEFT OUTER JOIN ticket.bagInfo.flightLegs b ON a.ticketNo=b.ticketNo'
   print('Fetching data using Left Outer Join ')
   fetch_data(handle,sql_stmt_loj)
   sql_stmt_nt='SELECT * FROM NESTED TABLES (ticket a descendants(ticket.bagInfo.flightLegs b))'
   print('Fetching data using NESTED TABLES ')
   fetch_data(handle,sql_stmt_nt)
   sql_stmt_ij='SELECT * FROM ticket a, ticket.bagInfo.flightLegs b WHERE a.ticketNo=b.ticketNo'
   print('Fetching data using Inner Join ')
   fetch_data(handle,sql_stmt_ij)      
   if handle is not None:
      handle.close()
  
if __name__ == "__main__":
   main()
