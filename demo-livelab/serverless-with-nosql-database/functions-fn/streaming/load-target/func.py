#
# Copyright (c) 2021 Oracle, Inc.  All rights reserved.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
#
import os
import io
import json
import logging
import base64
from fdk import response

from borneo import (
    AuthorizationProvider, DeleteRequest, GetRequest,
    IllegalArgumentException, NoSQLHandle, NoSQLHandleConfig, PutRequest,
    QueryRequest, Regions, TableLimits, TableRequest)
from borneo.iam import SignatureProvider
from borneo.kv import StoreAccessTokenProvider

import traceback

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
        # return data 
        #
        return response.Response(ctx, status_code=200, response_data=logs, headers={"Content-Type": "text/plain"})
    except (Exception, ValueError) as e:
        # For demo purpose, I am ignoring all errors
        # During tests, I sent some no JSON messages to my Stream that were broken all
        # If there is an error, in the next iteration I will receive all pending message from the Stream
        # so continue to have the same error
        logger.error('Logging and ignore the error ' + str(e))
        return response.Response(ctx, status_code=200, response_data=traceback.format_exc(), headers={"Content-Type": "text/plain"})

def base64_decode(encoded):
    if not encoded: return
    base64_bytes = encoded.encode('utf-8')
    message_bytes = base64.b64decode(base64_bytes)
    return message_bytes.decode('utf-8')

def get_handle():
     provider = SignatureProvider.create_with_resource_principal()
     config = NoSQLHandleConfig(os.getenv('NOSQL_REGION'), provider).set_logger(None).set_default_compartment(os.getenv('NOSQL_COMPARTMENT_ID'))
     return NoSQLHandle(config)
     