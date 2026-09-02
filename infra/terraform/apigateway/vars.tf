variable "project_name" {
  default = "fiap-mecanica"
}

variable "region_default" {
  default = "us-east-1"
}

variable "tags" {
  default = {
    Name = "fiap-mecanica-terraform"
  }
}

variable "app_elb_host" {
  description = <<-EOT
    Hostname do LoadBalancer do Service Kubernetes da aplicacao (sem esquema e sem barra final),
    ex: "a1b2c3d4e5f6.us-east-1.elb.amazonaws.com". A AWS gera esse nome e ele muda toda vez que
    o Service e recriado, por isso o valor nao fica fixado aqui — o job `apigw-apply` do cd.yml
    passa o hostname real via TF_VAR_app_elb_host depois do deploy no cluster.

    Default vazio de proposito: permite `terraform plan` no CI (em Pull Request nao existe ELB) e
    permite criar o gateway numa infra do zero, antes do primeiro deploy no Kubernetes. Enquanto
    estiver vazio, a rota da aplicacao nao e criada e o gateway responde 404 em tudo.
  EOT
  type        = string
  default     = ""
}

variable "auth_lambda_name" {
  description = <<-EOT
    Nome da Function Lambda de autenticacao de cliente por CPF (repo `fiap-mecanica-lambda`).
    Default vazio de proposito: enquanto nao for preenchido, nenhum `data`/rota da Lambda e
    avaliado, e este modulo nao depende da existencia da Lambda na conta AWS. Sem isso, um
    `data "aws_lambda_function"` incondicional faria o plan/apply quebrar em toda PR e
    impossibilitaria um deploy do zero.
  EOT
  type        = string
  default     = ""
}
