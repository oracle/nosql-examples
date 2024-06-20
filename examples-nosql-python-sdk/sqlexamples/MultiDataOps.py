# Copyright (c) 2023, 2024 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/
import os
from borneo import (Regions, NoSQLHandle, NoSQLHandleConfig, PutRequest,QueryRequest,MultiDeleteRequest,
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
   statement = '''create table if not exists examplesAddress (id INTEGER,
                                                           address_line1 STRING,
                                                           address_line2 STRING,
                                                           pin INTEGER,
                                                           PRIMARY KEY(SHARD(pin), id))'''
   request = TableRequest().set_statement(statement).set_table_limits(TableLimits(20, 10, 1))
   # Create the table, waiting for a total of 40000 milliseconds
   # and polling the service every 3000 milliseconds to see if table is active
   table_result = handle.do_table_request(request, 40000, 3000)
   table_result.wait_for_completion(handle, 40000, 3000)
   if (table_result.get_state() == State.ACTIVE):
      print('Created table: examplesAddress')
   else:
      raise NameError('Table examplesAddress is in an unexpected state ' + str(table_result.get_state()))

# Add a row of data to the table
def insert_record(handle,table_name,idval,addr1,addr2,pinval):
   value = {'id': idval, 'address_line1': addr1, 'address_line2': addr2, 'pin': pinval}
   request = PutRequest().set_table_name(table_name).set_value(value)
   handle.put(request)
   print('Loaded a row into table: examplesAddress')

# Fetch data from the table
def fetch_rowCnt(handle,sqlstmt):
   request = QueryRequest().set_statement(sqlstmt)
   print('Row count for: ' + sqlstmt)
   result = handle.query(request)
   for r in result.get_results():
      print('\t' + str(r))

#delete multiple rows
def multirow_delete(handle,table_name,pinval):
   request = MultiDeleteRequest().set_table_name(table_name).set_key({'pin': pinval})
   result = handle.multi_delete(request)

def main():
   handle = None
   # if cloud service uncomment this
   handle = get_connection_cloud()
   # if onPremise uncomment this
   #handle = get_connection_onprem()
   create_table(handle)
   insert_record(handle,'examplesAddress',1,'10 Red Street','Apt 3',1234567)
   insert_record(handle,'examplesAddress',2,'2 Green Street','Street 9',1234567)
   insert_record(handle,'examplesAddress',3,'5 Blue Ave','Floor 9',1234567)
   insert_record(handle,'examplesAddress',4,'9 Yellow Boulevard','Apt 3',87654321)
   sqlstmt = 'select * from examplesAddress'
   fetch_rowCnt(handle,sqlstmt)
   multirow_delete(handle,'examplesAddress',1234567)
   fetch_rowCnt(handle,sqlstmt)
   if handle is not None:
      handle.close()
  
if __name__ == "__main__":
    main()
