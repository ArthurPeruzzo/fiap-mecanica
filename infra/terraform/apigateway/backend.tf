terraform {
  backend "s3" {
    bucket = "fiap-mecanica"
    key    = "tfstate/apigateway.tfstate"
    region = "us-east-1"
  }
}
