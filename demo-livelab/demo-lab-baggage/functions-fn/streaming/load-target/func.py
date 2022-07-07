#
# Copyright (c) 2021 Oracle, Inc.  All rights reserved.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
#
import os
import io
import oci
import json
import requests
import logging
import base64
import pandas as pd
from fdk import response

import borneo
import sys
from borneo import (
    AuthorizationProvider, DeleteRequest, GetRequest,
    IllegalArgumentException, NoSQLHandle, NoSQLHandleConfig, PutRequest,
    QueryRequest, Regions, TableLimits, TableRequest)
from borneo.iam import SignatureProvider
from borneo.kv import StoreAccessTokenProvider

def handler(ctx, data: io.BytesIO=None):
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    try:
        store_handle = get_handle()

        logs = json.loads(data.getvalue())
        logger.info('Received {} entries.'.format(len(logs)))

        for item in logs:
            if 'value' in item:
                item['value'] = base64_decode(item['value'])

            if 'key' in item:
                item['key'] = base64_decode(item['key'])


        #
        # For demo purpose, we are inserting the data as received, no processing
        # Put rows in NoSQL
        #
        request = PutRequest().set_table_name('demoKeyVal')
        for item in logs:
            if 'value' in item:
               value = { 'value': json.loads(item['value'])}
               request.set_value(value)
               store_handle.put(request)

        request = PutRequest().set_table_name('demo')
        for item in logs:
            if 'value' in item:
               request.set_value_from_json(item['value'])
               store_handle.put(request)
        #
        # return data in CSV mode
        #
        df = pd.json_normalize(logs)
        csv_result = df.to_csv(index=False)
        return response.Response(ctx, status_code=200, response_data=csv_result, headers={"Content-Type": "text/csv"})

    except (Exception, ValueError) as e:
        # For demo purpose, I am ignoring all errors
        # During tests, I sent some no JSON messages to my Stream that were broken all
        # If there is an error, in the next iteration I will receive all pending message from the Stream
        # so continue to have the same error
        logger.info('Logging and ignore the error')
        logger.error(str(e))

def base64_decode(encoded):
    if not encoded: return
    base64_bytes = encoded.encode('utf-8')
    message_bytes = base64.b64decode(base64_bytes)
    return message_bytes.decode('utf-8')

def get_handle():
     provider = borneo.iam.SignatureProvider.create_with_resource_principal()
     compartment_id = provider.get_resource_principal_claim(borneo.ResourcePrincipalClaimKeys.COMPARTMENT_ID_CLAIM_KEY)

     config = borneo.NoSQLHandleConfig(os.getenv('NOSQL_REGION'), provider).set_logger(None).set_default_compartment(compartment_id)
     return borneo.NoSQLHandle(config)
