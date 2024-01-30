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

    statement = '''create table if not exists stream_acct (acct_Id INTEGER,
                                                           profile_name STRING,
                                                           account_expiry TIMESTAMP(1),
                                                           acct_data JSON,
                                                           primary key(acct_Id))'''
    request = TableRequest().set_statement(statement).set_table_limits(TableLimits(20, 10, 1))
    table_result = handle.do_table_request(request, 40000, 3000)
    table_result.wait_for_completion(handle, 40000, 3000)
    if (table_result.get_state() == State.ACTIVE):
        print('Created table: stream_acct')
    else:
        raise NameError('Table stream_acct is in an unexpected state ' + str(table_result.get_state()))

# create an index
def create_index(handle):

   statement = '''CREATE INDEX acct_episodes ON stream_acct (acct_data.contentStreamed[].seriesInfo[].episodes[]  AS ANYATOMIC)'''
   request = TableRequest().set_statement(statement)
   table_result = handle.do_table_request(request, 40000, 3000)
   table_result.wait_for_completion(handle, 40000, 3000)
   print('Index acct_episodes on the table stream_acct is created')

# drop the index
def drop_index(handle):
   statement = '''DROP INDEX acct_episodes ON stream_acct'''
   request = TableRequest().set_statement(statement)
   table_result = handle.do_table_request(request, 40000, 3000)
   table_result.wait_for_completion(handle, 40000, 3000)
   print('Index acct_episodes on the table stream_acct is dropped')

def main():
   handle = None
   # if cloud service uncomment this. else if onPremise comment this line
   handle = get_connection_cloud()
   # if onPremise uncomment this. else if cloud service comment this line
   # handle = get_connection_onprem()
   create_table(handle)
   create_index(handle)
   drop_index(handle)
   if handle is not None:
      handle.close()
   os._exit(0)

if __name__ == "__main__":
   main()
