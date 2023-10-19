provider oci {
	region = var.region
}

provider "oci" {
  alias                = "homeregion"
  region               = data.oci_identity_region_subscriptions.home_region_subscriptions.region_subscriptions[0].region_name
}
