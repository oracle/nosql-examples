## Copyright (c) 2020, Oracle and/or its affiliates.
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl

# Functions Policies

resource "oci_identity_policy" "NOSQLDEMOFunctionsServiceReposAccessPolicy" {
  provider = oci.homeregion
  name = "${var.ocir_repo_name}_FunctionsServiceReposAccessPolicy"
  description = "${var.ocir_repo_name}_FunctionsServiceReposAccessPolicy"
  compartment_id = var.tenancy_ocid
  statements = ["Allow service FaaS to read repos in tenancy", "Allow service FaaS to use virtual-network-family in tenancy"]
  provisioner "local-exec" {
       command = "sleep 5"
  }
}

resource "oci_identity_dynamic_group" "FunctionsServiceDynamicGroup" {
  provider = oci.homeregion
  name = "nosql_demos"
  description = "nosql_demos"
  compartment_id = var.tenancy_ocid
  matching_rule = "Any {\nALL {resource.type = 'ApiGateway', resource.compartment.id = '${var.compartment_ocid}'},\nALL {resource.type = 'fnfunc', resource.compartment.id = '${var.compartment_ocid}'}\n}"
  provisioner "local-exec" {
       command = "sleep 5"
  }
}

resource "oci_identity_policy" "FunctionsServiceDynamicGroupPolicy" {
  depends_on = [oci_identity_dynamic_group.FunctionsServiceDynamicGroup]
  provider = oci.homeregion
  name = "nosql_demos_faas"
  description = "nosql_demos_faas"
  compartment_id = var.compartment_ocid
  statements = [
   "allow dynamic-group ${oci_identity_dynamic_group.FunctionsServiceDynamicGroup.name} to use functions-family  in compartment id ${var.compartment_ocid} "
  ,"allow dynamic-group ${oci_identity_dynamic_group.FunctionsServiceDynamicGroup.name} to manage all-resources in compartment id ${var.compartment_ocid} "
  ,"allow any-user  to use functions-family   in compartment id ${var.compartment_ocid} where ALL {request.principal.type = 'ApiGateway', request.resource.compartment.id = '${var.compartment_ocid}'}"
  ,"allow any-user to use fn-function in compartment id ${var.compartment_ocid} where all {request.principal.type='serviceconnector',  request.principal.compartment.id='${var.compartment_ocid}'}"
  ,"allow any-user to use fn-invocation in compartment id ${var.compartment_ocid} where all {request.principal.type='serviceconnector',  request.principal.compartment.id='${var.compartment_ocid}'}"
#  ,"allow any-user to {STREAM_READ, STREAM_CONSUME} in compartment id ${var.compartment_ocid} where all {request.principal.type='serviceconnector',  target.stream.id='oci_streaming_stream.export_nosql_demos_3.id',  request.principal.compartment.id='${var.compartment_ocid}'}"
  ]

  provisioner "local-exec" {
       command = "sleep 5"
  }
}

