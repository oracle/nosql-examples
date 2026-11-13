# LangChain and NoSQL Database Integration

This example illustrates the use of LangChain to save and retrieve an LLM 
context state (checkpoints) into Oracle NoSQL Database. 

Integrating LangChain with Oracle NoSQL Database is accomplished by developing 
a LangChain component that saves and restores state using Oracle NoSQL's 
Python APIs.
 
Source code implements LangChain's Checkpointer interface.

### Requirements

The following packages are required:
 - A LangChain supported LLM
 - Python 3.12+
 - Oracle NoSQL Python SDK (Borneo) 5.5.0+
 - LangChain 1.3.1+
 - LangChain OpenAI 1.7.2+
 - LangGraph 1.2.1+
 - LangGraph Checkpoint 4.1.0+
 - For Oracle NoSQL Cloud Service you will need OCI 2.175.0+

Install them using:

```shell
python3.14 -m venv venv
source ./venv/bin/activate
pip install borneo langchain langgraph langgraph-checkpoint oci langchain_openai
```

Make sure the NoSQL DB and Model are configured corectly.
For example a local on-preemises DB and local LMStudio:

```shell
export ORACLE_NOSQL_DB_ENDPOINT="http://localhost:8080"
export LMSTUDIO_MODEL="lfm2.5-1.2b-thinking-mlx"
export LMSTUDIO_BASE_URL="http://localhost:1234/v1"
```

Run the example
```shell
python example.py
```

## Documentation

The LangChan agent has to be created with an ```OracleNoSqlDbCheckpointer``` 
and/or an ```OracleNoSqlDbStore``` as in the example below.

The ```OracleNoSqlDbCheckpointer``` constructor has these parameters:
  - db_handle - the handle for the NoSQL Database as a [NoSQLHandle](https://nosql-python-sdk.readthedocs.io/en/stable/api/borneo.NoSQLHandleConfig.html#borneo.NoSQLHandle)
  - table_name - The parent table name to use for checkpoints. It uses 2 tables: a checkpoint parent table by default "lc_checkpoints" and a child table "lc_checkpoints.writes". 
  - drop_tables - Drops the current tables if True. By default is False.
  - debug - If verbose output is required.
  - serde - Serialization / deserialization instance
  - checkpointsTableLimits - cloud only TableLimits if a new table is created, defaults to provisioned 5 reads/ses, 5 writes/sec, 5 GB,
  - writesTableLimits -  - cloud only TableLimits if a new table is created, defaults to provisioned 10 reads/ses, 10 writes/sec, 5 GB

Use ```OracleNoSqlDbCheckpointer.create_from_db_connection_string()``` to connect to the store using its connection string (see ). Or use ```OracleNoSqlDbCheckpointer.create_from_db_endpoint()``` method to connect to an on-premices, usually local, non-secured instance.
  - dbEndpoint - the endpoint of the NoSQL Database, by default uses local on-premises: "http://localhost:8080"
  - tableName - The parent table name.
  - drop_tables - Drops previous tables.
  - debug - For verbose output.




## Example

The following example shows how to use it in an application:

```python
llm = ChatOpenAI(
        model=os.getenv("EXAMPLE_MODEL", "model name"),
        base_url=os.getenv("EXAMPLE_BASE_URL", "model base URL"),
        #api_key=os.getenv("EXAMPLE_API_KEY", "llm provider key"),
    )
agent = create_agent(
        model=llm,
        system_prompt=(
            "You are a helpful assistant."
        ),
        checkpointer=OracleNoSqlDbCheckpointer.create_from_db_endpoint(
            ORACLE_NOSQL_DB_ENDPOINT,
            table_name="checkpoints",
            drop_tables=False,
        ),
    )
```

For a full example see [example.py](example.py).

## NoSQL Database connection string

`NoSqlDbConnectionString` parses the Oracle NoSQL Database Connection String format:

Format:
```text
nosqldb[+cloud|+on_prem][+http|+https]://[auth@]host[:port][,host[:port]...][/compartment_or_namespace[/reserved_path]][?options]
```

The default connection type is `cloud`, and the default protocol is `https`
(port 443; explicit `http` uses port 80). Known cloud region names resolve to
service endpoints; other hosts are direct endpoints. Host lists are preserved;
`get_nosql_handle_config(host_index=0)` selects one host for a Borneo handle. Use
`get_endpoints()` to inspect all endpoints.

Authentication supports username/password for on-prem, OCI config files and profiles,
inline OCI credentials, session tokens, instance principals `+instance_principal`, 
resource principals `+resource_principal`, OKE workload identity `+oke_workload`, 
and `+cloudsim`. `OracleNoSqlDbConnectionString` parsing does not access credential 
files or connect. `get_nosql_handle_config()` creates the authorization provider, 
which may read credentials or contact an identity service. An existing Borneo 
authorization provider can be passed as `get_nosql_handle_config(provider=provider)`.

Separate options with commas or `&`; percent-encode separators inside values.
Names are case insensitive and the first duplicate wins. `compartment` and
`namespace` override the first path segment; the service-specific name wins if
both are present. `configFile` (including a bare `?configFile`) overrides inline
cloud credentials and defaults to `~/.oci/config`, with profile `DEFAULT`.

`get_nosql_handle_config()` applies compartment/namespace, consistency (default `EVENTUAL`),
`timeout`, `ddlTimeout`, `rateLimiter` (boolean), `rateLimiterPercent`,
`retryMaxRetries`, and on-prem PEM certificates. A PEM file without a password
sets the CA bundle; with `pemCertificatePassword` it loads an encrypted client
certificate/key into an SSL context. Remaining options, including polling,
durability, query memory, HTTP options, and SDK-specific retry settings, remain
available through `get_options()` for application or request-level use. Unknown
and options are ignored.

Run the connection-string tests without a database:

```shell
PYTHONPATH=. venv/bin/python -m pytest test/test_connection_string.py -q
```


## LangChain Checkpoint Conformance test

To run the [LangChain Checkpoint Conformance](https://github.com/langchain-ai/langgraph/tree/main/libs/checkpoint-conformance) test install the test packages and setup PYTHONPATH:

```shell
pip install langgraph-checkpoint-conformance pytest pytest-asyncio
export PYTHONPATH=$PYTHONPATH:
```

Run the test:
```shell
pythest
```
