terraform {
  # use_lockfile (lock de state nativo do S3, sem DynamoDB) exige Terraform >= 1.10.
  required_version = ">= 1.10.0"
  backend "s3" {
    bucket = "fiap-mecanica"
    # State PRÓPRIO deste módulo (só New Relic). Até 2026-09-08 este diretório dividia a chave
    # "tfstate/terraform.tfstate" com o repositório fiap-mecanica-infra-k8s — o que fazia o
    # `terraform apply` sem `-target` daquele repo tentar destruir os recursos newrelic_* (config
    # dele não os declara) e falhar por não ter o provider newrelic. Agora são states separados.
    key    = "tfstate/newrelic.tfstate"
    region = "us-east-1"
    # Lock de state nativo do S3: dois `apply` na mesma chave nao se sobrescrevem
    # (ex.: apply local sobrepondo o CD). Objeto <chave>.tflock no mesmo bucket.
    use_lockfile = true
  }
}
