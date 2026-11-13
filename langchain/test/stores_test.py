# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

import os
from uuid import uuid4

import pytest
from borneo import TableRequest
from langgraph.store.memory import InMemoryStore
from OracleNoSqlDbStore import OracleNoSqlDbStore


ORACLE_NOSQL_DB_ENDPOINT = os.getenv("ORACLE_NOSQL_DB_ENDPOINT", "http://localhost:8080")

pytestmark = pytest.mark.asyncio

@pytest.fixture
def store():
    table_name = f"test_lc_store_{uuid4().hex}"
    backend = OracleNoSqlDbStore.create_from_db_endpoint(
        ORACLE_NOSQL_DB_ENDPOINT, table_name=table_name,
        debug=False, drop_tables=False,
    )
    try:
        yield backend
    finally:
        try:
            result = backend._handle.table_request(
                TableRequest().set_statement(f"DROP TABLE IF EXISTS {table_name}")
            )
            result.wait_for_completion(backend._handle, 40000, 3000)
        finally:
            backend._handle.close()

@pytest.fixture
def reference():
    return InMemoryStore()

async def test_put_and_get(store, reference):
    ns = ("test", "ns")
    for s in [store, reference]:
        await s.aput(ns, "k1", {"val": 1})
        item = await s.aget(ns, "k1")
        assert item is not None
        assert item.value == {"val": 1}

async def test_delete(store, reference):
    ns = ("test", "ns")
    for s in [store, reference]:
        await s.aput(ns, "k1", {"val": 1})
        await s.adelete(ns, "k1")
        assert await s.aget(ns, "k1") is None

async def test_search_prefix(store, reference):
    for s in [store, reference]:
        await s.aput(("user", "memories"), "m1", {"text": "likes pizza"})
        await s.aput(("user", "memories"), "m2", {"text": "likes ice cream"})
        await s.aput(("user", "memories"), "m3", {"text": "likes sushi"})
        await s.aput(("user", "memories"), "m4", {"text": "likes pasta"})
        results = await s.asearch(("user",))
        assert any(r.key == "m1" for r in results)
        results = await s.asearch(("user","memories"))
        assert any(r.key == "m1" for r in results)
        results = await s.asearch(("user", "memories", "m1"))
        assert results == []
        assert (await s.aget(("user", "memories"), "m1")).key == "m1"

    s_results = await store.asearch(("user",))
    r_results = await reference.asearch(("user",))
    assert {r.key for r in s_results} == {r.key for r in r_results}

    s_results = await store.asearch(("user","memories"))
    r_results = await reference.asearch(("user","memories"))
    assert {r.key for r in s_results} == {r.key for r in r_results}

    s_results = await store.asearch(("user", "memories", "m1"))
    r_results = await reference.asearch(("user", "memories", "m1"))
    assert {r.key for r in s_results} == {r.key for r in r_results}
