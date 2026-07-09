resource "aws_ecr_repository" "app" {
  name                 = var.project_name
  image_tag_mutability = "MUTABLE" # permite sobrescrever a tag :latest a cada push durante testes

  image_scanning_configuration {
    scan_on_push = true
  }

  force_delete = true # permite terraform destroy mesmo com imagens dentro (ambiente de estudo)

  tags = var.tags
}
