# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

import os
from dataclasses import dataclass
from httpx2 import query
from langchain_openai import ChatOpenAI
from langchain.agents import create_agent
from langchain.tools import tool, ToolRuntime
from OracleNoSqlDbStore import OracleNoSqlDbStore
from uuid_utils import uuid7


ORACLE_NOSQL_DB_CONNECTION_STRING = os.getenv("ORACLE_NOSQL_DB_CONNECTION_STRING", "nosqldb+on_prem+http://localhost:8080")
# LMSTUDIO_MODEL = os.getenv("LMSTUDIO_MODEL", "lfm2.5-1.2b-thinking-mlx")
LMSTUDIO_MODEL = os.getenv("LMSTUDIO_MODEL", "google/gemma-4-e4b")
LMSTUDIO_BASE_URL = os.getenv("LMSTUDIO_BASE_URL", "http://localhost:1234/v1")


def build_llm():
    # """Create the chatmodel using environment-based configuration."""
    return ChatOpenAI(
        model=os.getenv("EXAMPLE_MODEL", LMSTUDIO_MODEL),
        base_url=os.getenv("EXAMPLE_BASE_URL", LMSTUDIO_BASE_URL),
        api_key=os.getenv("EXAMPLE_API_KEY", "LLM_PROVIDER_KEY"),
    )


@dataclass
class UserContext:
    user_id: str


@tool
def save_my_memory(
    memory_key: str,
    memory_value: str,
    runtime: ToolRuntime[UserContext],
) -> None:
    """Save information to the current user's long term memory. Example usage: save_my_memory("favorite_food", "pizza")"""

    user_id = runtime.context.user_id if runtime and runtime.context else "Unknown"

    print(f"TOOL: Saving memory for user_id: '{user_id}' with memory: {memory_key} -> {memory_value}")
    runtime.store.put(
        (user_id, "memories"),
        str(uuid7()),
        {memory_key: memory_value}
    )


@tool
def search_my_memories(
    query: str,
    runtime: ToolRuntime[UserContext],
) -> str:
    """Search the current user's saved memories for information relevant to a question."""

    user_id = runtime.context.user_id if runtime and runtime.context else "Unknown"

    print(f"TOOL: Searching for memories for user_id: '{user_id}' with query: {query}")
    matches = runtime.store.search(
        (user_id, "memories"),
        query=query,
        limit=5,
    )
    return "\n".join(
        str(item.value.get("memory", item.value)) for item in matches
    ) or "No relevant memories found."

@tool
def delete_my_memory(
    memory_key: str,
    runtime: ToolRuntime[UserContext],
) -> None:
    """Delete information from the current user's long term memory. Example usage: delete_my_memory("favorite_food")"""

    user_id = runtime.context.user_id if runtime and runtime.context else "Unknown"

    print(f"TOOL: Deleting memory for user_id: '{user_id}' with memory: {memory_key}")
    runtime.store.delete(
        (user_id, "memories"),
        memory_key
    )




def main() -> None:

    # Setup Store with Oracle NoSQL Database
    store = OracleNoSqlDbStore.create_from_db_connection_string(ORACLE_NOSQL_DB_CONNECTION_STRING, debug=True, drop_table=False)

    mems = store.search(("user-42", "memories"))
    for m in mems:
        print(f"Deleting memory: {m.key} -> {m.value}")
        store.delete(("user-42", "memories"), m.key)


    # Setup LLM and Agent with Checkpointer and Store
    llm = build_llm()
    agent = create_agent(
        model=llm,
        system_prompt=(
            "You are a helpful assistant. Use the memory tools to save, delete, search user memories."
        ),
        store = store,
        tools=[save_my_memory, search_my_memories, delete_my_memory],
    )

    print("\n=== LLM Invocation 1 ===")
    response = agent.invoke(
        {
            "messages": [
                {
                    "role": "user",
                    "content": f"Search through my memories. What is my name, occupation and favorite food?",
                }
            ]
        },
        config={
            "configurable": {
                "thread_id": "thread_1",
            }
        },
        context=UserContext(user_id="user-42")
    )

    print("\n  - Response from LLM:")
    print(response["messages"][-1].content)

    # Store some information in the database
    print("\n=== Storing information in the database ===")

    user_id = "user-42"
    namespace_for_memory = (user_id, "memories")

    memory_id = str(uuid7())
    memory = {"food_preference" : "I like pizza"}
    store.put(namespace_for_memory, memory_id, memory)

    memories = store.search(namespace_for_memory)
    print(f"\n  - Stored memories for {user_id}:")
    for m in memories:
        print(f"    - {m.key}: {m.value}")


    # Second invocation to demonstrate storing and retrieving information in a different thread
    print("\n\n=== LLM Invocation 2 ===")
    response = agent.invoke(
        {
            "messages": [
                {
                    "role": "user",
                    "content": f"Store in my long-term memory that my name is Albert Eistain and I am a physicist.",
                }
            ]
        },
        config={
            "configurable": {
                "thread_id": "thread_2",
            }
        },
        context=UserContext(user_id="user-42")
    )

    print("\n  - LLM 2nd Response:")
    print(response["messages"][-1].content)


    print("\n\n=== LLM Invocation 3 ===")
    response = agent.invoke(
        {
            "messages": [
                {
                    "role": "user",
                    "content": f"What's my name, occupation and favorite food? Use my long-term memory to answer.",
                }
            ]
        },
        config={
            "configurable": {
                "thread_id": "thread_3",
            }
        },
        context=UserContext(user_id="user-42")
    )

    print("\n  - LLM 3rd Response:")
    print(response["messages"][-1].content)


if __name__ == "__main__":
    main()
