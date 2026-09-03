output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

# Passthrough dos states de outros repositórios — consumidos pelo cd.yml (job docker-build-push,
# k8s-deploy e apigw-apply), que continuam lendo db_endpoint/ecr_repo com o mesmo nome de antes,
# só que agora vindos daqui em vez do módulo combinado infra/terraform/aws.
output "db_endpoint" {
  value = data.terraform_remote_state.db.outputs.db_endpoint
}

output "eks_cluster_name" {
  value = data.terraform_remote_state.k8s.outputs.eks_cluster_name
}
