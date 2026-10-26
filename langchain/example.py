# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

import os

from langchain_openai import ChatOpenAI
from langchain.agents import create_agent

from OracleNoSqlDbTools import OracleNoSqlDbCheckpointer


ORACLE_NOSQL_DB_ENDPOINT = os.getenv("ORACLE_NOSQL_DB_ENDPOINT", "http://localhost:8080")
LMSTUDIO_MODEL = os.getenv("LMSTUDIO_MODEL", "lfm2.5-1.2b-thinking-mlx")
LMSTUDIO_BASE_URL = os.getenv("LMSTUDIO_BASE_URL", "http://localhost:1234/v1")


def build_llm():
    # """Create the chatmodel using environment-based configuration."""
    return ChatOpenAI(
        model=os.getenv("EXAMPLE_MODEL", LMSTUDIO_MODEL),
        base_url=os.getenv("EXAMPLE_BASE_URL", LMSTUDIO_BASE_URL),
        api_key=os.getenv("EXAMPLE_API_KEY", "LLM_PROVIDER_KEY"),
    )


def main() -> None:
    print("=== List checkpoints ===")

    checkpointer = OracleNoSqlDbCheckpointer.create_from_db_endpoint(ORACLE_NOSQL_DB_ENDPOINT, debug=True, drop_tables=False)

    cpIter = checkpointer.list(
        config={"configurable": {"thread_id": "thread_1"}},
        limit=10,
    )

    for cp in cpIter:
        q = cp.checkpoint.get('channel_values', {}).get("messages", [])
        if q and isinstance(q, list) and len(q) > 0:
            q = q[-1].content
            q = q.strip()[0:60] + "..." if len(q) > 60 else q.strip() 
        else:
            q = "<no messages>"

        print(f"   Cpt: {cp.config.get('configurable', {}).get('checkpoint_id')} ", q)
        # thread_id: {cp.config.get('configurable', {}).get('thread_id')}, \
        # cp_ns: {cp.config.get('configurable', {}).get('checkpoint_ns')}, \
        # ts: {cp.config.get('configurable', {}).get('ts')}, \

    print("\n=== Delete checkpoints ===")
    checkpointer.delete_thread("thread_1")


    llm = build_llm()
    agent = create_agent(
        model=llm,
        system_prompt=(
            "You are a helpful assistant."
        ),
        checkpointer = checkpointer,
    )

    print("\n=== LLM Invocation 1 ===")
    response = agent.invoke(
        {
            "messages": [
                {
                    "role": "user",
                    "content": f"What is the answer to the ultimate question?",
                }
            ]
        },
        config={
            "configurable": {
                "thread_id": "thread_1",
            }
        }
    )

    print("\n  - Response from LLM:")
    print(response["messages"][-1].content)

    # Second invocation to demonstrate retrieval from the database in a different context
    print("\n=== LLM Invocation 2 ===")
    response = agent.invoke(
        {
            "messages": [
                {
                    "role": "user",
                    "content": f"What is the ultimate question?",
                }
            ]
        },
        config={
            "configurable": {
                "thread_id": "thread_1",
            }
        }
    )

    print("\n  - LLM 2nd Response:")
    print(response["messages"][-1].content)


if __name__ == "__main__":
    main()