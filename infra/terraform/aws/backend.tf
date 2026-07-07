terraform {
  backend "s3" {
    bucket = "fiap-mecanica"
    key    = "tfstate/terraform.tfstate"
    region = "us-east-1"
  }
}