# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

import asyncio
from contextlib import contextmanager
from threading import Event
from types import SimpleNamespace
from unittest.mock import Mock, call

import pytest
from borneo import PreparedStatement
from langgraph.store.base import GetOp, ListNamespacesOp, MatchCondition, PutOp, SearchOp
from langgraph.store.memory import InMemoryStore

from OracleNoSqlDbStore import OracleNoSqlDbStore


class FakeHandle:
    def __init__(self):
        self.rows = {}
        self.prepared_statements = []
        self.query_requests = []

    def prepare(self, request):
        prepared = Mock(spec=PreparedStatement)
        prepared.get_sql_text.return_value = request.get_statement()
        self.prepared_statements.append(prepared)
        return SimpleNamespace(get_prepared_statement=lambda: prepared)

    def get(self, request):
        return SimpleNamespace(get_value=lambda: self.rows.get(request.get_key()["key"]))

    def put(self, request):
        row = request.get_value()
        self.rows[row["key"]] = row

    def delete(self, request):
        self.rows.pop(request.get_key()["key"], None)

    def query(self, request):
        self.query_requests.append(request)
        return SimpleNamespace(get_results=lambda: list(self.rows.values()))


def test_database_operations_and_persistence():
    handle = FakeHandle()
    store = OracleNoSqlDbStore.__new__(OracleNoSqlDbStore)
    store._handle = handle
    store._table_name = "test_table"
    store._debug = False
    other = OracleNoSqlDbStore.__new__(OracleNoSqlDbStore)
    other._handle = handle
    other._table_name = "test_table"
    other._debug = False

    namespace = ("a", "b")
    assert store.batch([PutOp(namespace, "c", {"n": 1}), GetOp(namespace, "c")])[0] is None
    first = other.batch([GetOp(namespace, "c")])[0]
    assert first.value == {"n": 1}
    store.batch([PutOp(namespace, "c", {"n": 2}), PutOp(("a",), "b.c", {"n": 3})])
    updated = other.batch([GetOp(namespace, "c")])[0]
    assert updated.created_at == first.created_at
    assert updated.value == {"n": 2}
    assert [item.key for item in other.batch([SearchOp(("a",), filter={"n": 2})])[0]] == ["c"]
    condition = MatchCondition("suffix", ("b",))
    # disable check since FakeHandle does not implement group by
    # assert other.batch([ListNamespacesOp(match_conditions=(condition,))])[0] == [namespace]
    store.batch([PutOp(namespace, "c", None)])
    assert other.batch([GetOp(namespace, "c")])[0] is None
    assert other.batch([GetOp(("a",), "b.c")])[0].value == {"n": 3}


@pytest.mark.parametrize("query_filter", [
    {"score": 8},
    {"score": {"$eq": 8}},
    {"score": {"$ne": 3}},
    {"score": {"$gt": 5}},
    {"score": {"$gte": 8}},
    {"score": {"$lt": 8}},
    {"score": {"$lte": 3}},
    {"score": {"$gte": 3, "$lt": 8}},
    {"profile": {"role": "admin"}},
    {"profile": {"rank": {"$gt": 1}}},
    {"tags": [{"name": "a"}]},
    {"score": {"$gt": 5}, "profile": {"role": "user"}},
])
def test_search_filters_match_reference(query_filter):
    store = OracleNoSqlDbStore.__new__(OracleNoSqlDbStore)
    store._handle = FakeHandle()
    store._table_name = "test_table"
    store._debug = False
    reference = InMemoryStore()
    for backend in (store, reference):
        backend.put(("docs",), "one", {
            "score": 8, "profile": {"role": "admin", "rank": 2},
            "tags": [{"name": "a", "extra": True}],
        })
        backend.put(("docs",), "two", {
            "score": 3, "profile": {"role": "user", "rank": 1},
            "tags": [{"name": "b"}],
        })
    assert [item.key for item in store.search(("docs",), filter=query_filter)] == [
        item.key for item in reference.search(("docs",), filter=query_filter)
    ]


@pytest.mark.asyncio
async def test_async_reads_leave_event_loop_available():
    release = Event()

    class BlockingHandle(FakeHandle):
        def get(self, request):
            release.wait(timeout=2)
            return super().get(request)

    store = OracleNoSqlDbStore.__new__(OracleNoSqlDbStore)
    store._handle = BlockingHandle()
    store._table_name = "test_table"
    store._debug = False
    read = asyncio.create_task(store.aget(("docs",), "one"))
    try:
        await asyncio.sleep(0)
        assert not read.done(), "Database I/O blocked the event loop"
    finally:
        release.set()
        assert await read is None


def test_integration_fixture_uses_unique_tables_and_cleans_up(monkeypatch):
    from stores_test import store as integration_store

    created = []
    dropped = []
    closed = []

    def create(db_endpoint, *, table_name, debug, drop_table):
        assert table_name.startswith("test_lc_store_")
        assert not drop_table
        created.append(table_name)

        def table_request(request):
            dropped.append(request.get_statement())
            return SimpleNamespace(wait_for_completion=lambda *args: None)

        return SimpleNamespace(_handle=SimpleNamespace(
            table_request=table_request, close=lambda: closed.append(table_name)))

    monkeypatch.setattr(OracleNoSqlDbStore, "create_from_db_endpoint", create)
    fixture_context = contextmanager(integration_store.__wrapped__)
    with fixture_context():
        pass
    with pytest.raises(RuntimeError, match="test failure"):
        with fixture_context():
            raise RuntimeError("test failure")
    assert len(set(created)) == 2
    assert dropped == [f"DROP TABLE IF EXISTS {name}" for name in created]
    assert closed == created


@pytest.mark.parametrize("component", ["O'Reilly", "x' OR true OR 'x'='x", "用户", r"a\b"])
def test_namespace_search_binds_values(component):
    store = OracleNoSqlDbStore.__new__(OracleNoSqlDbStore)
    store._handle = FakeHandle()
    store._table_name = "test_table"
    store._debug = False
    namespace = (component, "memories")
    store.put(namespace, "one", {"text": "saved"})
    store.put(("other", "memories"), "two", {"text": "unrelated"})

    assert [item.key for item in store.search(namespace)] == ["one"]

    prepared, = store._handle.prepared_statements
    sql = prepared.get_sql_text()
    assert component not in sql
    assert "memories" not in sql
    assert sql.startswith("DECLARE $ns0 STRING; $ns1 STRING; SELECT")
    assert sql.count("DECLARE") == 1
    assert "$t.val_.'namespace'[0] = $ns0" in sql
    assert "$t.val_.'namespace'[1] = $ns1" in sql
    assert prepared.set_variable.call_args_list == [
        call("$ns0", component), call("$ns1", "memories")
    ]
    assert store._handle.query_requests[0].get_prepared_statement() is prepared


def test_empty_namespace_search_has_no_where_clause():
    store = OracleNoSqlDbStore.__new__(OracleNoSqlDbStore)
    store._handle = FakeHandle()
    store._table_name = "test_table"
    store._debug = False
    store.put(("docs",), "one", {"n": 1})

    assert [item.key for item in store.search(())] == ["one"]
    assert not store._handle.prepared_statements
    assert "WHERE" not in store._handle.query_requests[0].get_statement()


@pytest.mark.asyncio
@pytest.mark.parametrize("options", [
    {},
    {"prefix": ("users",)},
    {"suffix": ("memories",)},
    {"prefix": ("*", "memories")},
    {"max_depth": 1, "limit": 1, "offset": 1},
    {"suffix": ("memories",), "limit": 1, "offset": 1},
])
async def test_namespace_listing_matches_reference(options):
    store = OracleNoSqlDbStore.__new__(OracleNoSqlDbStore)
    store._handle = FakeHandle()
    store._table_name = "test_table"
    store._debug = False
    reference = InMemoryStore()
    for backend in (store, reference):
        for index, namespace in enumerate([
            ("users", "profile"), ("users", "profile"),
            ("users", "memories"), ("teams", "memories"), ("docs",),
        ]):
            backend.put(namespace, str(index), {"n": index})

    for request in store._handle.query_requests:
        assert request.get_statement() == "SELECT $t.val_.'namespace' as ns FROM test_table $t GROUP BY $t.val_.'namespace'"
