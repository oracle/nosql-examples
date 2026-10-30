import os

import pytest
from langgraph.checkpoint.conformance import checkpointer_test, validate

from OracleNoSqlDbTools import OracleNoSqlDbCheckpointer

ORACLE_NOSQL_DB_ENDPOINT = os.getenv("ORACLE_NOSQL_DB_ENDPOINT", "http://localhost:8080")

@checkpointer_test(name="MyCheckpointer")
async def my_checkpointer():
    yield OracleNoSqlDbCheckpointer.create_from_db_endpoint(ORACLE_NOSQL_DB_ENDPOINT, debug=True, drop_tables=False)

@pytest.mark.asyncio
async def test_conformance():
    report = await validate(my_checkpointer)
    report.print_report()
    assert report.passed_all_base()
