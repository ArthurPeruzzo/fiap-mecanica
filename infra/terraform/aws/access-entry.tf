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
