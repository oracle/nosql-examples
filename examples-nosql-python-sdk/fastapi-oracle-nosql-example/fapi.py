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

from borneo import (Regions, NoSQLHandle, NoSQLHandleConfig, 
                    PutRequest,QueryRequest, DeleteRequest, TableRequest, GetRequest, PutOption,QueryIterableResult,
                    TableLimits, State,TimeToLive)
from borneo.iam import SignatureProvider
from borneo.kv import StoreAccessTokenProvider


from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn

# Pydantic model for order data
class Order(BaseModel):
    order_id: int
    product_name: str
    quantity: int


def get_connection():
  print("Connecting to the Oracle NoSQL database")
  provider = StoreAccessTokenProvider()
  config = NoSQLHandleConfig( 'http://localhost:8080', provider).set_logger(None)
  return(NoSQLHandle(config))

def create_table ():
  statement = 'create table if not exists fapi_orders(order_id LONG,  primary key(order_id)) AS JSON COLLECTION'
  request = TableRequest().set_statement(statement)
  result = handle.do_table_request(request, 40000, 3000)
  result = handle.table_request(request)
  result.wait_for_completion(handle, 40000, 3000)
  print (statement)

# Create a connection pool
handle = get_connection()
# Set up the schema
create_table()
# Define the FastAPI app
app = FastAPI()

# Endpoint to create an order
@app.post("/orders/", response_model=Order)
def create_order(order: Order):
  request = PutRequest().set_table_name("fapi_orders").set_option(PutOption.IF_ABSENT)
  request.set_value(order.model_dump()); 
  result = handle.put(request)
  if result.get_version() is not None:
    return order
  else:
    raise HTTPException(status_code=404, detail="Error Insterting - Order found")

# Endpoint to retrieve orders
#@app.get("/orders/",response_model=list[Order])
@app.get("/orders/")
def get_orders() -> Any:
   statement = 'select * from fapi_orders order by order_id'
   request = QueryRequest().set_statement(statement)
   qiresult = handle.query_iterable(request)
   orders = []
   for row in qiresult:
      orders.append(row)
   return orders

# Endpoint to retrieve an order by ID
@app.get("/orders/{order_id}", response_model=Order)
def get_order(order_id: int):
  request = GetRequest().set_key({'order_id': order_id}).set_table_name("fapi_orders")
  result = handle.get(request)
  if result.get_value() is None:
    raise HTTPException(status_code=404, detail="Order not found")
  else:
    return (result.get_value())

# Endpoint to update an order by ID
@app.put("/orders/{order_id}", response_model=Order)
def update_order(order_id: int, order: Order):
  request = PutRequest().set_table_name("fapi_orders").set_option(PutOption.IF_PRESENT)
  request.set_value(order.model_dump()); 
  result = handle.put(request)
  if result.get_version() is not None:
    return order
  else:
    raise HTTPException(status_code=404, detail="Error Updating - Order not found")

# Endpoint to delete an order by ID
@app.delete("/orders/{order_id}")
def delete_order(order_id: int):
    request = DeleteRequest().set_key({'order_id': order_id}).set_table_name("fapi_orders")
    result = handle.delete(request)
    if result.get_success():
      return {"message": "Order deleted successfully"}
    else:
      raise HTTPException(status_code=404, detail="Order not found")

# Close the connection pool on app shutdown
@app.on_event("shutdown")
def shutdown_event():
  if handle is not None:
     handle.close()
