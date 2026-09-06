provider "aws" {
  region = var.region_default
}

# O provider "newrelic" e os recursos newrelic_* (alertas/dashboards de negócio) ainda moram no
# módulo legado infra/terraform/aws — migração para cá adiada de propósito (ver README deste
# módulo). Quando migrar, adicionar o required_providers + provider "newrelic" aqui, igual ao
# módulo legado.
