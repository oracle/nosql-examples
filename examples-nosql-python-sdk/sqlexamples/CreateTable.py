# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
import os
from borneo import (Regions, NoSQLHandle, NoSQLHandleConfig, PutRequest,QueryRequest,
                    TableRequest, GetRequest, TableLimits, State)
from borneo.iam import SignatureProvider

# Given a region, and compartment, instantiate a connection to the
# cloud service and return it
def get_connection(region):
    print("Connecting to the Oracle NoSQL Cloud Service")
    provider = SignatureProvider();
    #If using the DEFAULT profile with the config file in default location ~/.oci/config
    config = NoSQLHandleConfig(region, provider)
    #replace with your compartment OCID
    config.set_default_compartment("<your compartment OCID>")
    return(NoSQLHandle(config))

def create_table(handle):

    statement = '''create table if not exists stream_acct (acct_Id INTEGER,
                                                           profile_name STRING,
                                                           account_expiry TIMESTAMP(1),
                                                           acct_data JSON,
                                                           primary key(acct_Id))'''
    print('Creating table: ' + statement)
    request = TableRequest().set_statement(statement).set_table_limits(TableLimits(20, 10, 1))
    # Ask the cloud service to create the table, waiting for a total of 40000 milliseconds
    # and polling the service every 3000 milliseconds to see if the table is active
    table_result = handle.do_table_request(request, 40000, 3000)
    table_result.wait_for_completion(handle, 40000, 3000)
    if (table_result.get_state() == State.ACTIVE):
        print('Created table: stream_acct')
    else:
        raise NameError('Table stream_acct is in an unexpected state ' + str(table_result.get_state()))

def main():
    handle = get_connection(Regions.US_ASHBURN_1)
    create_table(handle)
    os._exit(0)

if __name__ == "__main__":
    main()
