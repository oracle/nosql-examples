#------------------------------------------------------------------------------
# Copyright (c) 2024, Oracle and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#------------------------------------------------------------------------------

import os
import logging
import uvicorn

from borneo import (NoSQLHandle, NoSQLHandleConfig , 
                    TableLimits, State,
                    PutRequest,QueryRequest, DeleteRequest, TableRequest, GetRequest, PutOption, QueryIterableResult )

from borneo.kv import StoreAccessTokenProvider
#from borneo.iam import SignatureProvider


from typing import List, Set, Union, Any
from pydantic import BaseModel, HttpUrl
from fastapi import FastAPI, HTTPException

# Pydantic model for item data
class Image(BaseModel):
    #url: str
    url: HttpUrl
    name: str


class Item(BaseModel):
    __tablename__ = "fapi_items"
    item_id: int
    name: str
    tags: Set[str] = set()
    #tags: List[str] = []
    #tags: list = []
    images: Union[List[Image], None] = None


def get_connection():
    # Learn more https://nosql-python-sdk.readthedocs.io/en/stable/installation.html#configuring-the-sdk
    print("Connecting to the Oracle NoSQL database")
    provider = StoreAccessTokenProvider()
    config = NoSQLHandleConfig( 'http://localhost:8080', provider).set_logger(None)
    return(NoSQLHandle(config))


def create_table ():
    # Learn more https://nosql-python-sdk.readthedocs.io/en/stable/tables.html
    # Learn more https://docs.oracle.com/en/database/other-databases/nosql-database/23.3/nsdev/schema-flexibility-oracle-nosql-database.html
    statement = 'create table if not exists fapi_items(item_id LONG, primary key(item_id)) AS JSON COLLECTION'
    request = TableRequest().set_statement(statement).set_table_limits( TableLimits(20, 10, 5))
    result = handle.do_table_request(request, 40000, 3000)
    result = handle.table_request(request)
    result.wait_for_completion(handle, 40000, 3000)
    print (statement)

# Create the NoSQL handle and NoSQL the JSON COLLECTION table 
tableName="fapi_items"
key="item_id"
handle = get_connection()
create_table()
# Define the FastAPI app
app = FastAPI()


# Endpoint to create an item
@app.post("/items/", 
    response_model=Item,
    summary="Create an item",
    response_description="A document containing all information about a Item"
)
def create_item(item: Item):
    # Learn more https://nosql-python-sdk.readthedocs.io/en/stable/tables.html
    # See the option IF_ABSENT
    request = PutRequest().set_table_name(tableName).set_option(PutOption.IF_ABSENT)
    #request.set_value(item.model_dump()); 
    request.set_value_from_json(item.model_dump_json()); 
    result = handle.put(request)
    if result.get_version() is not None:
       return item
    else:
       raise HTTPException(status_code=404, detail="Error Insterting - Item found")

# Endpoint to retrieve items
@app.get("/items/",
    # Validate a list[Items] needs python 3.9 or 3.10
    # response_model=list[Item],
    summary="Gets all item",
    response_description="A list containing all information about the Items"
)
def get_items(page: int = 0, limit: int = 10, orderby: str = "item_id", where: str = "" ):
    try:
       statement = ("SELECT * FROM {table} {where} ORDER BY {orderby} LIMIT {limit} OFFSET {offset}").format(table=tableName,where=where,orderby=orderby,limit=limit,offset=limit*page)
       print(statement)
       request = QueryRequest().set_statement(statement)
       qiresult = handle.query_iterable(request)
       items = list()
       for row in qiresult:
         try:
           items.append(Item.parse_obj(row))
           return items
         except Exception as e:
           print("---- Ignoring bad records")
           print(f"Error: {e}")
           continue
    except Exception as e:
       raise HTTPException(status_code=500, detail=f"Error: {e}")

# Endpoint to retrieve an item by ID
@app.get("/items/{item_id}",
    response_model=Item,
    summary="Gets an item",
    response_description="A document containing all information about a Item"
)
def get_item(item_id: int):
    # Learn more https://nosql-python-sdk.readthedocs.io/en/stable/tables.html
    try:
       request = GetRequest().set_key({key: item_id}).set_table_name(tableName)
       result = handle.get(request)
    except Exception as e:
       raise HTTPException(status_code=500, detail=f"Error: {e}")
    if result.get_value() is None:
       raise HTTPException(status_code=404, detail="Item not found")
    else:
       try:
          item  = Item.parse_obj(result.get_value())
          # return (result.get_value())
          return (Item.parse_obj(result.get_value()))
       except Exception as e:
          raise HTTPException(status_code=500, detail=f"Error: {e}")

# Endpoint to update an item by ID
@app.put("/items/{item_id}", 
    response_model=Item,
    summary="Update an item",
    response_description="A document containing all information about a Item"
)
def update_item(item_id: int, item: Item):
    # Learn more https://nosql-python-sdk.readthedocs.io/en/stable/tables.html
    # See the option IF_PRESENT
    request = PutRequest().set_table_name(tableName).set_option(PutOption.IF_PRESENT)
    #request.set_value(item.model_dump()); 
    request.set_value_from_json(item.model_dump_json()); 
    result = handle.put(request)
    if result.get_version() is not None:
       return item
    else:
       raise HTTPException(status_code=404, detail="Error Updating - Item not found")

# Endpoint to delete an item by ID
@app.delete("/items/{item_id}",
    summary="Delete an item",
    response_description="A message indicating the Itemi was deleted"
)
def delete_item(item_id: int):
    # Learn more https://nosql-python-sdk.readthedocs.io/en/stable/tables.html
    request = DeleteRequest().set_key({key: item_id}).set_table_name(tableName)
    result = handle.delete(request)
    if result.get_success():
       return {"message": "Item deleted successfully"}
    else:
       raise HTTPException(status_code=404, detail="Item not found")

# Close the connection pool on app shutdown
@app.on_event("shutdown")
def shutdown_event():
   if handle is not None:
      print("Disconnecting from the Oracle NoSQL database")
      handle.close()
