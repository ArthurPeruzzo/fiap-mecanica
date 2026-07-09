data "aws_caller_identity" "current" {}

locals {
  # Turns the assumed-role session ARN (arn:...:assumed-role/voclabs/<session>)
  # into the underlying IAM role ARN, which is what EKS access entries need.
  voclabs_role_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/voclabs"
}

resource "aws_eks_access_entry" "voclabs" {
  cluster_name  = aws_eks_cluster.cluster.name
  principal_arn = local.voclabs_role_arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "voclabs_admin" {
  cluster_name  = aws_eks_cluster.cluster.name
  principal_arn = aws_eks_access_entry.voclabs.principal_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }
}

# LabRole é usada tanto como service role do cluster (eks-cluster.tf) quanto como
# node role do node group (eks-node.tf) — a AWS Academy Lab não permite criar IAM
# roles próprias. Numa recriação do zero, a EKS pode auto-criar uma access entry
# pra essa identidade do tipo STANDARD sem grupos, o que faz os nodes se
# autenticarem no cluster mas nunca conseguirem se registrar como objetos Node
# (RBAC nega tudo). Grupos "system:*" (system:nodes, system:bootstrappers) são
# reservados e a API do EKS rejeita atribuí-los manualmente a uma entry STANDARD
# — o tipo correto é EC2_LINUX, que já vem com esses grupos vinculados
# automaticamente. Declarando esse recurso aqui, com depends_on no node group
# (eks-node.tf), garantimos que a entry correta exista antes dos nodes tentarem
# se registrar, evitando esse bug em qualquer novo `terraform apply`.
resource "aws_eks_access_entry" "node_role" {
  cluster_name  = aws_eks_cluster.cluster.name
  principal_arn = data.aws_iam_role.lab_role.arn
  type          = "EC2_LINUX"
}
