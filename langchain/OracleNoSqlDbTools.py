# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

import base64
import re
from collections.abc import Iterator, Sequence
from datetime import datetime as Datetime
from typing import Any
from langchain_core.runnables import RunnableConfig
from langgraph.checkpoint.base import (
    WRITES_IDX_MAP,
    BaseCheckpointSaver,
    ChannelVersions,
    Checkpoint,
    CheckpointMetadata,
    CheckpointTuple,
)
from borneo import MultiDeleteRequest, GetTableRequest, NoSQLHandle, NoSQLHandleConfig, QueryRequest, \
    TableRequest, PutRequest, GetRequest, PrepareRequest, WriteMultipleRequest, \
    GetResult
from borneo.kv import StoreAccessTokenProvider


class OracleNoSqlDbCheckpointer(BaseCheckpointSaver):
    # static method for creating a checkpointer from an endpoint string
    @staticmethod
    def create_from_db_endpoint(dbEndpoint: str,
                 tableName: str = "checkpoints",
                 drop_tables: bool = False,
                 debug: bool = False,
                 serde: Any = None):
        dbConfig = NoSQLHandleConfig(dbEndpoint,  StoreAccessTokenProvider())
        return OracleNoSqlDbCheckpointer(dbConfig, tableName, drop_tables, debug, serde)

    def __init__(self, dbConfig: NoSQLHandleConfig, 
                 tableName: str = "checkpoints",
                 drop_tables: bool = False,
                 debug: bool = False,
                 serde: Any = None):
        super().__init__(serde = serde)  # Use default serde
        tableName = self._validate_table_name(tableName)
        self._table_name = tableName
        self._table_created = False
        self._debug = debug

        self._handle = NoSQLHandle(dbConfig)
        self.create_table(tableName, drop_tables=drop_tables)

    def _query_request(
        self,
        statement: str,
        bind_variables: dict[str, Any] | None = None,
    ) -> QueryRequest:
        """Build a query request with values bound separately from its SQL."""
        if not bind_variables:
            return QueryRequest().set_statement(statement)

        declarations = []
        for name, value in bind_variables.items():
            if isinstance(value, str):
                sql_type = "STRING"
            elif type(value) is bool:
                sql_type = "BOOLEAN"
            elif type(value) is int:
                sql_type = "INTEGER"
            elif type(value) is float:
                sql_type = "DOUBLE"
            else:
                raise TypeError(
                    f"Unsupported bind value type for ${name}: {type(value).__name__}"
                )
            declarations.append(f"${name} {sql_type};")
        prepared = self._handle.prepare(
            PrepareRequest().set_statement(
                f"DECLARE {' '.join(declarations)} {statement}"
            )
        ).get_prepared_statement()
        for name, value in bind_variables.items():
            prepared.set_variable(f"${name}", value)
        return QueryRequest().set_prepared_statement(prepared)

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
    def _parent_config(row: dict[str, Any]) -> RunnableConfig | None:
        value = row.get("val_", {})
        parent_id = value.get("parent_id") or value.get("parent_checkpoint_id")
        if not parent_id:
            return None
        return {
            "configurable": {
                "thread_id": row.get("thread_id"),
                "checkpoint_ns": row.get("checkpoint_ns", ""),
                "checkpoint_id": parent_id,
            }
        }

    def _metadata_from_row(self, row: dict[str, Any]) -> CheckpointMetadata:
        """Read metadata maps, including rows written with the old encoding."""
        value = row.get("val_", {})
        metadata = value.get("metadata", {})
        if isinstance(metadata, dict):
            return metadata

        # Keep existing checkpoint tables readable after metadata switches to JSON.
        metadata_type = value.get("metadata_type")
        if isinstance(metadata, str) and isinstance(metadata_type, str):
            return self.serde.loads_typed(
                (metadata_type, base64.b64decode(metadata.encode("utf-8")))
            )
        return metadata

    def create_table(self, table_name: str, drop_tables: bool = False) -> bool:
        """Create the Oracle NoSQL Database table if it doesn't exist.
           Only first time at init, drop the table if drop_tables is True.
        """
        table_name = self._validate_table_name(table_name)
        if self._table_created:
            return False  # Table already created, no need to create again
        if not self._handle:
            raise ValueError("NoSQL DB handle is not initialized.")

        if drop_tables:
            try:
                if self._debug:
                    print(f"   ...Dropping table '{table_name}.writes' if it exists...")
                ddl = f"""DROP TABLE IF EXISTS {table_name}.writes"""
                result = self._handle.table_request(
                    TableRequest()
                    .set_statement(ddl)
                )
                result.wait_for_completion(self._handle, 40000, 3000)
                if self._debug:
                    print(f"Table '{table_name}.writes' dropped.")
                self._table_created = False
            except Exception as e:
                if self._debug:
                    print(f"   ...Error dropping table '{table_name}.writes': {e}")
                self._table_created = False
        
            try:
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
                self._table_created = False
            except Exception as e:
                if self._debug:
                    print(f"   ...Error dropping table '{table_name}': {e}")
                self._table_created = False

        parent_table_created = False
        child_table_created = False
        
        try:
            request = GetTableRequest().set_table_name(table_name)
            self._handle.get_table(request)
            if self._debug:
                print(f"   ...Table '{table_name}' already exists.")
            parent_table_created = True
        except Exception:
            try:
                if self._debug:
                    print(f"   ...Creating table '{table_name}'...")
                ddl = f"""CREATE TABLE IF NOT EXISTS {table_name} (
                            thread_id     string,
                            checkpoint_ns string,
                            checkpoint_id string,
                            val_        JSON,
                            PRIMARY KEY(SHARD(thread_id), checkpoint_ns, checkpoint_id)
                    )"""
                result = self._handle.table_request(
                    TableRequest()
                    .set_statement(ddl)
                )
                result.wait_for_completion(self._handle, 40000, 3000)
                if self._debug:
                    print(f"Table '{table_name}' created.")
                parent_table_created = True
            except Exception as e:
                if self._debug:
                    print(f"   ...Error creating table '{table_name}': {e}")
                parent_table_created = False

        try:
            request = GetTableRequest().set_table_name(table_name + ".writes")
            self._handle.get_table(request)
            if self._debug:
                print(f"   ...Table '{table_name}.writes' already exists.")
            child_table_created = True
        except Exception:
            try:
                if self._debug:
                    print(f"   ...Creating table '{table_name}.writes'...")
                ddl = f"""CREATE TABLE IF NOT EXISTS {table_name}.writes (
                            task_id       string,
                            task_path     string,
                            idx           INTEGER,
                            val_        JSON,
                            PRIMARY KEY(task_id, task_path, idx)
                    )"""
                result = self._handle.table_request(
                    TableRequest()
                    .set_statement(ddl)
                )
                result.wait_for_completion(self._handle, 40000, 3000)
                if self._debug:
                    print(f"Table '{table_name}.writes' created.")
                child_table_created = True
            except Exception as e:
                if self._debug:
                    print(f"   ...Error creating table '{table_name}.writes': {e}")
                child_table_created = False

        self._table_created = parent_table_created and child_table_created
        return self._table_created


    def put(
        self,
        config: RunnableConfig,
        checkpoint: Checkpoint,
        metadata: CheckpointMetadata,
        new_versions: ChannelVersions,
    ) -> RunnableConfig:
        """Store a checkpoint with its configuration and metadata.

        Args:
            config: Configuration for the checkpoint.
            checkpoint: The checkpoint to store.
            metadata: Additional metadata for the checkpoint.
            new_versions: New channel versions as of this write.

        Returns:
            RunnableConfig: Updated configuration after storing the checkpoint.

        Raises:
            NotImplementedError: Implement this method in your custom checkpoint saver.
        """
        thread_id = config.get("configurable", {}).get("thread_id")
        checkpoint_ns = config.get("configurable", {}).get("checkpoint_ns", "")
        checkpoint_id = checkpoint["id"]
        parent_id = config.get("configurable", {}).get("checkpoint_id")

        type_, blob = self.serde.dumps_typed(checkpoint)

        if self._debug:
            print(f"DBG: put: thread_id={thread_id}, checkpoint_ns={checkpoint_ns}, checkpoint_id={checkpoint_id}, parent_id={parent_id}, type_={type_}")
            print(f"DBG: put: blob size={len(blob)}")

        row = {
            "thread_id" : thread_id,
            "checkpoint_ns" : checkpoint_ns,
            "checkpoint_id" : checkpoint_id,
            "val_": {
                "parent_id": parent_id,
                "type": type_,
                "blob": base64.b64encode(blob).decode('utf-8'),
                "metadata": dict(metadata),
                "ts": Datetime.now().isoformat()
            }
        }

        put_request = PutRequest().set_table_name(self._table_name).set_value(row)

        put_result = self._handle.put(put_request)
        if self._debug:
            print(f"DBG: put Checkpoint '{checkpoint_id}' written to table '{self._table_name}' with result: {put_result}")

        return {
            "configurable": {
                **config.get("configurable", {}),
                "thread_id": thread_id,
                "checkpoint_ns": checkpoint_ns,
                "checkpoint_id": checkpoint_id,
            }
        }

    def put_writes(
        self,
        config: RunnableConfig,
        writes: Sequence[tuple[str, Any]],
        task_id: str,
        task_path: str = "",
    ) -> None:
        thread_id = config.get("configurable", {}).get("thread_id")
        checkpoint_ns = config.get("configurable", {}).get("checkpoint_ns", "")
        checkpoint_id = config.get("configurable", {}).get("checkpoint_id")

        if self._debug:
            print(f"DBG: put_writes: thread_id={thread_id}, checkpoint_ns={checkpoint_ns}, checkpoint_id={checkpoint_id}")

        if thread_id is None or checkpoint_ns is None:
            raise ValueError("Properties thread_id and checkpoint_ns must be provided in the config.")

        wmReq: WriteMultipleRequest = WriteMultipleRequest()
        wmReq.set_table_name(self._table_name + ".writes")
        ts = Datetime.now().isoformat()

        for idx, (channel, value) in enumerate(writes):
            type_, blob = self.serde.dumps_typed(value)
            final_idx = WRITES_IDX_MAP.get(channel, idx)

            row = {
                "thread_id": thread_id,
                "checkpoint_ns": checkpoint_ns,
                "checkpoint_id": checkpoint_id,
                "task_id": task_id,
                "task_path": task_path,
                "idx": final_idx,
                "val_": {
                    "channel": channel,
                    "type": type_,
                    "value": base64.b64encode(blob).decode('utf-8'),
                    "ts": ts
                }
            }

            wmReq.add(PutRequest().set_table_name(self._table_name + ".writes").set_value(row), False)

        wmRes = self._handle.write_multiple(wmReq)
        if self._debug:
            print(f"DBG: put_writes result: {wmRes}")
      

    def get_tuple(self, config: RunnableConfig) -> CheckpointTuple | None:
        """Fetch a checkpoint tuple using the given configuration.

        Args:
            config: Configuration specifying which checkpoint to retrieve.

        Returns:
            The requested checkpoint tuple, or `None` if not found.

        Raises:
            NotImplementedError: Implement this method in your custom checkpoint saver.
        """
        thread_id = config.get("configurable", {}).get("thread_id")
        checkpoint_ns = config.get("configurable", {}).get("checkpoint_ns", "")
        checkpoint_id = config.get("configurable", {}).get("checkpoint_id")

        if self._debug:
            print(f"DBG: get_tuple: thread_id={thread_id}, checkpoint_ns={checkpoint_ns}, checkpoint_id={checkpoint_id}")

        # read checkpoint
        if checkpoint_id:
            key =  { "thread_id": thread_id,
                     "checkpoint_ns": checkpoint_ns,
                     "checkpoint_id": checkpoint_id
                   }
            getRes: GetResult = self._handle.get(
                GetRequest().set_table_name(self._table_name).set_key(key)
            )
            row = getRes.get_value()
            if row is None:
                return None
        else:
            qReq = self._query_request(
                f"SELECT * FROM {self._table_name} "
                "WHERE thread_id = $thread_id "
                "AND checkpoint_ns = $checkpoint_ns "
                "ORDER BY checkpoint_id DESC LIMIT 1",
                {"thread_id": thread_id, "checkpoint_ns": checkpoint_ns},
            )
            row = None
            while True:
                qRes = self._handle.query(qReq)
                rows = qRes.get_results()
                if len(rows) > 0:
                    row = rows[0]
                    break
                if qReq.is_done():
                    break
            if row is None:
                return None

        # read writes associated with the checkpoint
        qReq = self._query_request(
            f"SELECT w.task_path, w.task_id, w.val_.channel as channel, w.val_.type as type, w.val_.value as value "
            f"FROM {self._table_name}.writes as w "
            "WHERE thread_id = $thread_id "
            "AND checkpoint_ns = $checkpoint_ns "
            "AND checkpoint_id = $checkpoint_id "
            "ORDER BY w.task_path, w.task_id, w.idx",
            {
                "thread_id": thread_id,
                "checkpoint_ns": checkpoint_ns,
                "checkpoint_id": row["checkpoint_id"],
            },
        )

        pending_writes = []
        while True:
            qRes = self._handle.query(qReq)
            qRecs = qRes.get_results()
            pending_writes.extend([
                (r["task_id"], r["channel"], self.serde.loads_typed((r["type"], base64.b64decode(r["value"]))))
                for r in qRecs
            ])
            if qReq.is_done():
                break

        checkpoint = self.serde.loads_typed((row["val_"]["type"], base64.b64decode(row["val_"]["blob"].encode('utf-8'))))
        metadata = self._metadata_from_row(row)


        return CheckpointTuple(
            config={
                "configurable": {
                    "thread_id": row["thread_id"],
                    "checkpoint_ns": row["checkpoint_ns"],
                    "checkpoint_id": row["checkpoint_id"]
                }
            },
            checkpoint = checkpoint,         
            metadata = metadata,             
            parent_config = self._parent_config(row),
            pending_writes = pending_writes,
        )

    def list(
        self,
        config: RunnableConfig | None,
        *,
        filter: dict[str, Any] | None = None,
        before: RunnableConfig | None = None,
        limit: int | None = 10,
    ) -> Iterator[CheckpointTuple]:
        if limit is not None and type(limit) is not int:
            raise TypeError("limit must be an integer or None")
        if limit is not None and limit <= 0:
            return iter([])  # Return an empty iterator if limit is non-positive
        
        thread_id = config.get("configurable", {}).get("thread_id")
        checkpoint_ns = config.get("configurable", {}).get("checkpoint_ns", "")

        if self._debug:
            print(f"DBG: list: thread_id={thread_id}, checkpoint_ns={checkpoint_ns}")

        checkpoint_id = None
        if before:
            checkpoint_id = before.get("configurable", {}).get("checkpoint_id", None)

        query = (
            f"SELECT * FROM {self._table_name} "
            "WHERE thread_id = $thread_id "
            "AND checkpoint_ns = $checkpoint_ns "
        )
        bind_variables = {"thread_id": thread_id, "checkpoint_ns": checkpoint_ns}
        if checkpoint_id:
            query += "AND checkpoint_id < $checkpoint_id "
            bind_variables["checkpoint_id"] = checkpoint_id

        has_filter = bool(filter)
        if has_filter:
            filter_predicates = []
            for index, (key, value) in enumerate(filter.items()):
                # Oracle NoSQL field paths use identifiers, so only push down
                # keys that can safely be represented as one. Other keys are
                # still matched against the decoded metadata below.
                if not isinstance(key, str) or not re.fullmatch(
                    r"[A-Za-z_][A-Za-z0-9_]*", key
                ):
                    continue
                if not isinstance(value, str) and type(value) not in (
                    int,
                    bool,
                    float,
                ):
                    continue

                bind_name = f"filter_{index}"
                bind_variables[bind_name] = value
                filter_predicates.append(
                    f"val_.metadata.{key} = ${bind_name}"
                )

            if filter_predicates:
                # Rows written before metadata became a JSON map still need to
                # be checked after deserialization.
                query += (
                    "AND (val_.metadata_type IS NOT NULL OR ("
                    + " AND ".join(filter_predicates)
                    + ")) "
                )

        query += "ORDER BY checkpoint_id DESC"
        # Apply the limit after metadata matching. The SQL query can include
        # legacy rows that need to be checked using their decoded metadata.
        if limit is not None and not has_filter:
            query += " LIMIT $limit"
            bind_variables["limit"] = limit

        qReq = self._query_request(query, bind_variables)
        
        while True:
            if self._debug:
                print(f"DBG: list: querying...")
            qRes = self._handle.query(qReq)

            qRecs = qRes.get_results()
            for r in qRecs:
                checkpoint = self.serde.loads_typed((r["val_"]["type"], base64.b64decode(r["val_"]["blob"].encode('utf-8'))))
                metadata = self._metadata_from_row(r)

                if has_filter and any(
                    metadata.get(key) != value for key, value in filter.items()
                ):
                    continue

                # yield  # make this an async generator
                yield CheckpointTuple(
                    config={
                        "configurable": {
                            "thread_id": thread_id,
                            "checkpoint_ns": r.get("checkpoint_ns"),
                            "checkpoint_id": r.get("checkpoint_id"),
                            "ts": r.get("val_", {}).get("ts")
                        }
                    },
                    checkpoint = checkpoint,
                    metadata = metadata,
                    parent_config = self._parent_config(r),
                    pending_writes = None,
                )

                if has_filter and limit is not None:
                    limit -= 1
                    if limit == 0:
                        return

            if qReq.is_done():
                break


    def delete_thread(
        self,
        thread_id: str,
    ) -> None:
        """Delete all checkpoints and writes associated with a specific thread ID.

        Args:
            thread_id: The thread ID whose checkpoints should be deleted.
        """
        if self._debug:
            print(f"DBG: delete_thread: thread_id={thread_id}")

        req = MultiDeleteRequest().set_table_name(self._table_name).set_key({ 
                        "thread_id": thread_id
                    })
        while True:
            delRes = self._handle.multi_delete(req)
            if delRes.get_continuation_key() is None:
                break
            req.set_continuation_key(delRes.get_continuation_key())


        if self._debug:
            print(f"  DBG: delete_thread: deleted {delRes.get_num_deletions()} checkpoints for '{thread_id}'")


        req = MultiDeleteRequest().set_table_name(self._table_name + ".writes").set_key({ 
                "thread_id": thread_id
            })

        while True:
            delRes = self._handle.multi_delete(req)
            if delRes.get_continuation_key() is None:
                break
            req.set_continuation_key(delRes.get_continuation_key())

        if self._debug:
            print(f"  DBG: delete_thread: deleted {delRes.get_num_deletions()} writes for '{thread_id}'")
