# Copyright (c) 2019, 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
import os
from borneo import (Regions, NoSQLHandle, NoSQLHandleConfig, PutRequest,QueryRequest,
                    TableRequest, SystemRequest, GetRequest, TableLimits, State)
from borneo.iam import SignatureProvider
from borneo.kv import StoreAccessTokenProvider

# Given a endpoint, instantiate a connection to the onPremise Oracle NoSQL database
def get_connection_onprem():
   #replace the placeholder with the name of your local host
   kvstore_endpoint ='http://<hostname>:8080'
   provider = StoreAccessTokenProvider()
   #If using a secure store pass the username, password of the store to StoreAccessTokenProvider
   #provider = StoreAccessTokenProvider(username, password)
   return NoSQLHandle(NoSQLHandleConfig(kvstore_endpoint, provider))

# create a remote and local region
def create_region(handle):
   #Create a remote region
   statement = '''CREATE REGION LON'''
   sysreq = SystemRequest().set_statement(statement)
   sys_result = handle.system_request(sysreq)
   sys_result.wait_for_completion(handle, 40000, 3000)
   print('Remote region LON is created')
   #Create a local region
   statement1 = '''SET LOCAL REGION FRA'''
   sysreq1 = SystemRequest().set_statement(statement1)
   sys_result1 = handle.system_request(sysreq1)
   sys_result1.wait_for_completion(handle, 40000, 3000)
   print('Local region FRA is created')

#Create a table in the local region
def create_tab_region(handle):
   statement = '''create table if not exists stream_acct (acct_Id INTEGER,
                                                           profile_name STRING,
                                                           account_expiry TIMESTAMP(1),
                                                           acct_data JSON,
                                                           primary key(acct_Id)) IN REGIONS FRA'''
   request = TableRequest().set_statement(statement).set_table_limits(TableLimits(20, 10, 1))
   # Ask the cloud service to create the table, waiting for a total of 40000 milliseconds
   # and polling the service every 3000 milliseconds to see if the table is active
   table_result = handle.do_table_request(request, 40000, 3000)
   table_result.wait_for_completion(handle, 40000, 3000)
   if (table_result.get_state() == State.ACTIVE):
      print('Created table: stream_acct')
   else:
      raise NameError('Table stream_acct is in an unexpected state ' + str(table_result.get_state()))

#Drop the table from a region
def drop_tab_region(handle):
   statement = '''DROP TABLE stream_acct'''
   request = TableRequest().set_statement(statement)
   table_result = handle.do_table_request(request, 40000, 3000)
   table_result.wait_for_completion(handle, 40000, 3000)
   print('Dropped table: stream_acct')

#Drop the region
def drop_region(handle):
   statement = '''DROP REGION LON'''
   sysreq = SystemRequest().set_statement(statement)
   # Ask the cloud service to create the table, waiting for a total of 40000 milliseconds
   # and polling the service every 3000 milliseconds to see if the table is active
   sys_result = handle.system_request(sysreq)
   sys_result.wait_for_completion(handle, 40000, 3000)
   print('Region LON is dropped')

def main():
   handle = get_connection_onprem()
   create_region(handle)
   create_tab_region(handle)
   drop_tab_region(handle)
   drop_region(handle)
   os._exit(0)

if __name__ == "__main__":
    main()
