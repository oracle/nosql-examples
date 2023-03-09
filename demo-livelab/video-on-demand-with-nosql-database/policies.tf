## Copyright (c) 2023, Oracle and/or its affiliates.
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl



resource "oci_identity_dynamic_group" "NoSQLServiceDynamicGroup" {
  provider = oci.homeregion
  name = "nosql_demos"
  description = "nosql_demos"
  compartment_id = var.tenancy_ocid
  matching_rule = "Any {\nALL {resource.type = 'ApiGateway', resource.compartment.id = '${var.compartment_ocid}'},\nALL {resource.type = 'computecontainerinstance', resource.compartment.id = '${var.compartment_ocid}'}\n}"
  provisioner "local-exec" {
       command = "sleep 5"
  }
}

resource "oci_identity_policy" "FunctionsServiceDynamicGroupPolicy" {
  depends_on = [oci_identity_dynamic_group.NoSQLServiceDynamicGroup]
  provider = oci.homeregion
  name = "nosql_demos_ci"
  description = "nosql_demos_ci"
  compartment_id = var.compartment_ocid
  statements = [
    "allow dynamic-group ${oci_identity_dynamic_group.NoSQLServiceDynamicGroup.name} to manage all-resources in compartment id ${var.compartment_ocid} "
  ]

  provisioner "local-exec" {
       command = "sleep 5"
  }
}

