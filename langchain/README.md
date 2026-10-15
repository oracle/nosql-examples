# LangChain and NoSql Integration

This example illustrates the use of LangChain to save and retrieve checkpoints into Oracle NoSQL Database. 

Integrating LangChain with Oracle NoSQL Database is accomplished by developing a custom checkpointer using Oracle NoSQL's Python APIs.
 
Source code implements LangChain's Checkpointer interface.

### Requirements ###

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
pip install borneo langchain langgraph langgraph-checkpoint oci
```

Make sure the NoSQL DB and Model are configured corectly.
For example a local on-preemises DB and local LMStudio:

```shell
export ORACLE_NOSQL_DB_ENDPOINT="http://localhost:8080"
export LMSTUDIO_MODEL="lfm2.5-1.2b-thinking-mlx"
export LMSTUDIO_BASE_URL="http://localhost:1234/v1"
```

Run example
```shell
python example.py
```

## Documentation

The LangChan agent has to be created with an ```OracleNoSqlDbCheckpointer``` as in the example below.

The ```OracleNoSqlDbCheckpointer``` constructor has 4 parameters:
  - dbConfig - the config for the NoSQL Database as a [NoSQLHandleConfig](https://nosql-python-sdk.readthedocs.io/en/stable/api/borneo.NoSQLHandleConfig.html#borneo.NoSQLHandleConfig)
    This allows connecting to all supported types of NoSQL DB instances, including the Oracle NoSQL DB cloud service.
  - tableName - The parent table name to use for checkpoints. It uses 2 tables: a checkpoint parent table by default "checkpoints" and a child table "checkpoints.writes". 
  - drop_tables - Drops the current tables if True. By default is False.
  - debug - If verbose output is required.

Use ```OracleNoSqlDbCheckpointer.create_from_db_endpoint()``` method to connect to an on-premices, usually local, non-secured instance.
  - dbEndpoint - the endpoint of the NoSQL Database, by default uses local on-premises: "http://localhost:8080"
  - tableName - The parent table name.
  - drop_tables - Drops previous tables.
  - debug - For verbose output.


## Examples

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
        checkpointer = OracleNoSqlDbCheckpointer.create_from_db_endpoint(ORACLE_NOSQL_DB_ENDPOINT,
            tableName: str = "checkpoints",
            drop_tables=True),
    )
```

For a full example see [example.py](example.py).