## Copyright (c) 2020, Oracle and/or its affiliates.
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl

output "oci_apigateway_deployment_URL" {
  value = [join("", [data.oci_apigateway_deployment.export_BaggageDemo.endpoint, "/getBagInfoByTicketNumber"])]
}

output "nosql_table_ddl_statement" {
  value = data.oci_nosql_table.nosql_demo.ddl_statement
}

output "nosql_kv_table_ddl_statement" {
  value = data.oci_nosql_table.nosql_demoKeyVal.ddl_statement
}

