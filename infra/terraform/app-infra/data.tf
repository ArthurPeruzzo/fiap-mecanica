# Leituras somente-leitura — este módulo nunca escreve nos states de outros repositórios.

data "terraform_remote_state" "db" {
  backend = "s3"
  config = {
    bucket = "fiap-mecanica"
    key    = "tfstate/db.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "k8s" {
  backend = "s3"
  config = {
    bucket = "fiap-mecanica"
    key    = "tfstate/terraform.tfstate"
    region = "us-east-1"
  }
}
