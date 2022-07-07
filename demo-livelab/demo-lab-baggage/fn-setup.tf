## Copyright (c) 2020, Oracle and/or its affiliates.
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl

resource "null_resource" "Login2OCIR" {
  provisioner "local-exec" {
    command = "echo '${var.ocir_user_password}' |  docker login ${local.ocir_docker_repository} --username ${local.ocir_namespace}/${var.ocir_user_name} --password-stdin"
  }
}


resource "null_resource" "demo-apiPush2OCIR" {
  depends_on = [null_resource.Login2OCIR
			   ]

  provisioner "local-exec" {
    command = "image=$(docker images | grep demo-api | awk -F ' ' '{print $3}') ; docker rmi -f $image &> /dev/null ; echo $image"
    working_dir = "functions-fn/api/demo-api/"
  }

  provisioner "local-exec" {
    command = "fn build "
    working_dir = "functions-fn/api/demo-api/"
  }

  provisioner "local-exec" {
    command = "image=$(docker images | grep demo-api | awk -F ' ' '{print $3}') ; docker tag $image ${local.ocir_docker_repository}/${local.ocir_namespace}/${var.ocir_repo_name}/demo-api:0.0.1"
    working_dir = "functions-fn/api/demo-api/"
  }

  provisioner "local-exec" {
    command = "docker push ${local.ocir_docker_repository}/${local.ocir_namespace}/${var.ocir_repo_name}/demo-api:0.0.1"
    working_dir = "functions-fn/api/demo-api/"
  }
}

resource "null_resource" "demo-keyval-loadPush2OCIR" {
  depends_on = [null_resource.Login2OCIR
			   ]

  provisioner "local-exec" {
    command = "image=$(docker images | grep demo-keyval-load | awk -F ' ' '{print $3}') ; docker rmi -f $image &> /dev/null ; echo $image"
    working_dir = "functions-fn/load/demo-keyval-load/"
  }

  provisioner "local-exec" {
    command = "fn build "
    working_dir = "functions-fn/load/demo-keyval-load/"
  }

  provisioner "local-exec" {
    command = "image=$(docker images | grep demo-keyval-load | awk -F ' ' '{print $3}') ; docker tag $image ${local.ocir_docker_repository}/${local.ocir_namespace}/${var.ocir_repo_name}/demo-keyval-load:0.0.1"
    working_dir = "functions-fn/load/demo-keyval-load/"
  }

  provisioner "local-exec" {
    command = "docker push ${local.ocir_docker_repository}/${local.ocir_namespace}/${var.ocir_repo_name}/demo-keyval-load:0.0.1"
    working_dir = "functions-fn/load/demo-keyval-load/"
  }
}

resource "null_resource" "demo-loadPush2OCIR" {
  depends_on = [null_resource.Login2OCIR
			   ]

  provisioner "local-exec" {
    command = "image=$(docker images | grep demo-load | awk -F ' ' '{print $3}') ; docker rmi -f $image &> /dev/null ; echo $image"
    working_dir = "functions-fn/load/demo-load/"
  }

  provisioner "local-exec" {
    command = "fn build "
    working_dir = "functions-fn/load/demo-load/"
  }

  provisioner "local-exec" {
    command = "image=$(docker images | grep demo-load | awk -F ' ' '{print $3}') ; docker tag $image ${local.ocir_docker_repository}/${local.ocir_namespace}/${var.ocir_repo_name}/demo-load:0.0.1"
    working_dir = "functions-fn/load/demo-load/"
  }

  provisioner "local-exec" {
    command = "docker push ${local.ocir_docker_repository}/${local.ocir_namespace}/${var.ocir_repo_name}/demo-load:0.0.1"
    working_dir = "functions-fn/load/demo-load/"
  }
}


