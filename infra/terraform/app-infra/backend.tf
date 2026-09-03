terraform {
  backend "s3" {
    bucket = "fiap-mecanica"
    key    = "tfstate/app-infra.tfstate"
    region = "us-east-1"
  }
}
