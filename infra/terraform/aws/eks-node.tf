resource "aws_eks_node_group" "node-group" {
  cluster_name    = aws_eks_cluster.cluster.name
  node_group_name = "nodeg-${var.project_name}"
  node_role_arn   = data.aws_iam_role.lab_role.arn
  subnet_ids      = aws_subnet.subnet_public[*].id
  disk_size       = 50
  instance_types  = [var.instance_type]

  scaling_config {
    desired_size = 2
    max_size     = 3
    min_size     = 1
  }

  update_config {
    max_unavailable = 1
  }

  # Garante que a access entry da LabRole (com system:nodes/system:bootstrappers)
  # já exista antes dos nodes tentarem se registrar no cluster.
  depends_on = [aws_eks_access_entry.node_role]
}