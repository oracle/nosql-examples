# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

import os

import pytest
from langgraph.checkpoint.conformance import checkpointer_test, validate

from OracleNoSqlDbCheckpointer import OracleNoSqlDbCheckpointer

ORACLE_NOSQL_DB_ENDPOINT = os.getenv("ORACLE_NOSQL_DB_ENDPOINT", "http://localhost:8080")

@checkpointer_test(name="MyCheckpointer")
async def my_checkpointer():
    yield OracleNoSqlDbCheckpointer.create_from_db_endpoint(ORACLE_NOSQL_DB_ENDPOINT, table_name="test_lc_checkpointer", debug=False, drop_tables=False)

@pytest.mark.asyncio
async def test_conformance():
    report = await validate(my_checkpointer)
    report.print_report()
    assert report.passed_all_base()
