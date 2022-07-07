## Copyright (c) 2020, Oracle and/or its affiliates.
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl

data "oci_identity_regions" "oci_regions" {
  
  filter {
    name = "name" 
    values = [var.region]
  }

}

data "oci_core_services" "oci_services" {
  filter {
    name   = "name"
    values = ["All .* Services In Oracle Services Network"]
    regex  = true
  }
}

data "oci_identity_region_subscriptions" "home_region_subscriptions" {
    tenancy_id = var.tenancy_ocid

    filter {
      name   = "is_home_region"
      values = [true]
    }
}

data "oci_identity_tenancy" "oci_tenancy" {
    tenancy_id = var.tenancy_ocid
}

data "oci_objectstorage_namespace" "test_namespace" {
    compartment_id = var.tenancy_ocid
}

data "oci_nosql_table" "nosql_demo" {
  table_name_or_id = oci_nosql_table.nosql_demo.id
	compartment_id = oci_nosql_table.nosql_demo.compartment_id
}

data "oci_nosql_table" "nosql_demoKeyVal" {
  table_name_or_id = oci_nosql_table.nosql_demoKeyVal.id
	compartment_id = oci_nosql_table.nosql_demoKeyVal.compartment_id
}

