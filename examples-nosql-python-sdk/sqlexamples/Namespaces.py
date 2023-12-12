# Copyright (c) 2023, 2024 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
import os
from borneo import (Regions, NoSQLHandle, NoSQLHandleConfig, PutRequest,QueryRequest,
                    TableRequest, SystemRequest, GetRequest, TableLimits, State)
from borneo.iam import SignatureProvider
from borneo.kv import StoreAccessTokenProvider

# Given a endpoint, instantiate a connection to the onPremise Oracle NoSQL database
def get_connection_onprem():
   # replace the placeholder with the name of your host
   kvstore_endpoint ='http://<hostname>:8080'
   provider = StoreAccessTokenProvider()
   # If using a secure store, uncomment the line below and pass the username, password of the store to StoreAccessTokenProvider
   # provider = StoreAccessTokenProvider(username, password)
   return NoSQLHandle(NoSQLHandleConfig(kvstore_endpoint, provider))

# Creates a namespace
def create_ns(handle):
   statement = '''CREATE NAMESPACE IF NOT EXISTS ns1'''
   sysreq = SystemRequest().set_statement(statement)
   sys_result = handle.system_request(sysreq)
   sys_result.wait_for_completion(handle, 40000, 3000)
   print('Created namespace: ns1')

# Drop a namespace
def drop_ns(handle):
   statement = '''DROP NAMESPACE ns1'''
   sysreq = SystemRequest().set_statement(statement)
   sys_result = handle.system_request(sysreq)
   sys_result.wait_for_completion(handle, 40000, 3000)
   print('Namespace: ns1 is dropped')

def main():
   handle = get_connection_onprem()
   create_ns(handle)
   drop_ns(handle)
   os._exit(0)

if __name__ == "__main__":
    main()
