# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

import asyncio
from collections.abc import Iterable
from datetime import datetime, timezone
import re
import json
from typing import Any

from borneo import (
    DeleteRequest,
    GetRequest,
    PutRequest,
    QueryRequest,
    NoSQLHandleConfig,
    TableLimits,
    NoSQLHandle,
    TableRequest,
    GetTableRequest,
    TableNotFoundException
)
from borneo.kv import StoreAccessTokenProvider
from langgraph.store.base import (
    BaseStore,
    GetOp,
    Item,
    ListNamespacesOp,
    Op,
    PutOp,
    Result,
    SearchItem,
    SearchOp,
)


class OracleNoSqlDbStore(BaseStore):
    """LangGraph store backed by an Oracle NoSQL Database table."""

    def __init__(self,
                db_handle: NoSQLHandle,
                table_name: str = "langchainStore",
                debug: bool = False,
                drop_tables: bool = False,
                storeTableLimits: TableLimits = TableLimits(10, 10, 5)) -> None:
        """Initialize the database-backed store given a NoSQL handle."""
        self._table_name = table_name
        self._debug = debug
        self._drop_tables = drop_tables
        self._store_table_limits = storeTableLimits

        self._table_exists = False

        self._handle = db_handle
        if not self.create_tables(self._table_name, dropTables=self._drop_tables):
            raise RuntimeError(
                f"Failed to initialize Oracle NoSQL tables for '{self._table_name}'."
            )

    @staticmethod
    def create_from_db_config(
        db_config: NoSQLHandleConfig,
        table_name: str = "langchainStore",
        debug: bool = False,
        drop_tables: bool = False
    ) -> "OracleNoSqlDbStore":
        """Create an instance of OracleNoSqlDbStore from a NoSQLHandleConfig."""
        db_handle = NoSQLHandle(db_config)
        return OracleNoSqlDbStore(db_handle, table_name, debug, drop_tables)
    
    @staticmethod
    def create_from_db_endpoint(
        db_endpoint: str = "http://localhost:8080",
        table_name: str = "langchainStore",
        debug: bool = False,
        drop_tables: bool = False
    ) -> "OracleNoSqlDbStore":
        """Create an instance of OracleNoSqlDbStore from a database endpoint."""
        db_config = NoSQLHandleConfig(db_endpoint,  StoreAccessTokenProvider())
        return OracleNoSqlDbStore.create_from_db_config(db_config, table_name, debug, drop_tables)


    def create_tables(self, table_name: str, dropTables: bool = False) -> bool:
        """Create the Oracle NoSQL Database table if it doesn't exist.
           Only first time at init, drop the table if dropTables is True.
        """
        table_name = self._validate_table_name(table_name)
        if self._table_exists:
            return False  # Table already created, no need to create again
        if not self._handle:
            raise ValueError("NoSQL DB handle is not initialized.")

        if dropTables:
            if self._debug:
                print(f"   ...Dropping table '{table_name}' if it exists...")
            ddl = f"""DROP TABLE IF EXISTS {table_name}"""
            result = self._handle.table_request(
                TableRequest()
                .set_statement(ddl)
            )
            result.wait_for_completion(self._handle, 40000, 3000)
            if self._debug:
                print(f"Table '{table_name}' dropped.")
            self._table_exists = False

        try:
            request = GetTableRequest().set_table_name(table_name)
            self._handle.get_table(request)
            if self._debug:
                print(f"   ...Table '{table_name}' already exists.")
            self._table_exists = True
        except TableNotFoundException:
            if self._debug:
                print(f"   ...Creating table '{table_name}'...")
            ddl = f"""CREATE TABLE IF NOT EXISTS {table_name} (
                        key    string,
                        val_   JSON,
                        PRIMARY KEY(key)
                )"""
            result = self._handle.table_request(
                TableRequest()
                .set_statement(ddl)
                .set_table_limits(self._store_table_limits)
            )
            result.wait_for_completion(self._handle, 40000, 3000)
            if self._debug:
                print(f"Table '{table_name}' created.")
            self._table_exists = True

        if not self._table_exists:
            raise RuntimeError(
                f"Failed to initialize Oracle NoSQL tables for '{table_name}'."
            )
        return self._table_exists


    @staticmethod
    def _validate_table_name(table_name: str) -> str:
        if not isinstance(table_name, str) or not re.fullmatch(
            r"[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)*", table_name
        ):
            raise ValueError(
                "table name must contain only SQL identifier segments "
                "separated by dots."
            )
        return table_name


    @staticmethod
    def _item_key(namespace: tuple[str, ...], key: str) -> str:
        # JSON encoding avoids collisions between namespace components and keys.
        return json.dumps([namespace, key], ensure_ascii=True, separators=(",", ":"))

    @staticmethod
    def _item_from_row(row: dict) -> Item:
        value = row["val_"]
        return Item(
            namespace=tuple(value["namespace"]),
            key=value["key"],
            value=value["value"],
            created_at=datetime.fromisoformat(value["created_at"]),
            updated_at=datetime.fromisoformat(value["updated_at"]),
        )

    def _get_item(self, namespace: tuple[str, ...], key: str) -> Item | None:
        request = (GetRequest().set_table_name(self._table_name)
                   .set_key({"key": self._item_key(namespace, key)}))
        row = self._handle.get(request).get_value()
        return self._item_from_row(row) if row is not None else None

    def _items(self) -> Iterable[Item]:
        request = QueryRequest().set_statement(f"SELECT * FROM {self._table_name}")
        while True:
            for row in self._handle.query(request).get_results():
                yield self._item_from_row(row)
            if request.is_done():
                break

    @staticmethod
    def _matches_namespace(namespace: tuple[str, ...], conditions) -> bool:
        for condition in conditions or ():
            path = condition.path
            if len(path) > len(namespace):
                return False
            if condition.match_type == "prefix":
                candidate = namespace[:len(path)]
            elif condition.match_type == "suffix":
                candidate = namespace[len(namespace) - len(path):]
            else:
                raise ValueError(f"Unsupported namespace match type: {condition.match_type}")
            if any(pattern != "*" and pattern != part
                   for pattern, part in zip(path, candidate)):
                return False
        return True

    @staticmethod
    def _matches_filter_value(value: Any, condition: Any) -> bool:
        """Match LangGraph filters, including nested values and comparisons."""
        if isinstance(condition, dict):
            if any(key.startswith("$") for key in condition):
                return all(
                    OracleNoSqlDbStore._compare_filter_value(value, op, expected)
                    for op, expected in condition.items()
                )
            return isinstance(value, dict) and all(
                OracleNoSqlDbStore._matches_filter_value(value.get(key), expected)
                for key, expected in condition.items()
            )
        if isinstance(condition, (list, tuple)):
            return (
                isinstance(value, (list, tuple))
                and len(value) == len(condition)
                and all(
                    OracleNoSqlDbStore._matches_filter_value(actual, expected)
                    for actual, expected in zip(value, condition)
                )
            )
        return value == condition

    @staticmethod
    def _compare_filter_value(value: Any, op: str, expected: Any) -> bool:
        if op == "$eq":
            return value == expected
        if op == "$ne":
            return value != expected
        if op == "$gt":
            return float(value) > float(expected)
        if op == "$gte":
            return float(value) >= float(expected)
        if op == "$lt":
            return float(value) < float(expected)
        if op == "$lte":
            return float(value) <= float(expected)
        raise ValueError(f"Unsupported operator: {op}")

    def batch(self, ops: Iterable[Op]) -> list[Result]:
        """Execute operations synchronously in order against the database."""
        results: list[Result] = []
        for op in ops:
            if isinstance(op, PutOp):
                key = self._item_key(op.namespace, op.key)
                if op.value is None:
                    self._handle.delete(DeleteRequest().set_table_name(self._table_name)
                                        .set_key({"key": key}))
                else:
                    previous = self._get_item(op.namespace, op.key)
                    now = datetime.now(timezone.utc)
                    row = {"key": key, "val_": {
                        "namespace": list(op.namespace), "key": op.key,
                        "value": dict(op.value),
                        "created_at": (previous.created_at if previous else now).isoformat(),
                        "updated_at": now.isoformat(),
                    }}
                    self._handle.put(PutRequest().set_table_name(self._table_name)
                                     .set_value(row))
                results.append(None)
            elif isinstance(op, GetOp):
                results.append(self._get_item(op.namespace, op.key))
            elif isinstance(op, SearchOp):
                matches = [
                    SearchItem(namespace=item.namespace, key=item.key, value=item.value,
                               created_at=item.created_at, updated_at=item.updated_at)
                    for item in self._items()
                    if item.namespace[:len(op.namespace_prefix)] == op.namespace_prefix
                    and (op.filter is None or all(
                        self._matches_filter_value(item.value.get(k), v)
                        for k, v in op.filter.items()))
                ]
                results.append(matches[op.offset:op.offset + op.limit])
            elif isinstance(op, ListNamespacesOp):
                namespaces = {item.namespace for item in self._items()
                              if self._matches_namespace(item.namespace, op.match_conditions)}
                if op.max_depth is not None:
                    namespaces = {ns[:op.max_depth] for ns in namespaces}
                results.append(sorted(namespaces)[op.offset:op.offset + op.limit])
            else:
                raise ValueError(f"Unsupported operation type: {type(op)}")
        return results

    async def abatch(self, ops: Iterable[Op]) -> list[Result]:
        """Execute multiple operations asynchronously in a single batch."""
        return await asyncio.to_thread(self.batch, ops)
