variable "project_name" {
  default = "fiap-mecanica"
}

variable "region_default" {
  default = "us-east-1"
}

variable "cidr_vpc" {
  default = "10.0.0.0/16"
}

variable "tags" {
  default = {
    Name = "fiap-mecanica-terraform"
  }
}

variable "instance_type" {
  default = "t3.medium"
}

variable "db_name" {
  description = "Initial database name created on the RDS instance"
  default     = "mecanica"
}

variable "db_instance_class" {
  description = "RDS instance class"
  default     = "db.t3.micro"
}

variable "db_username" {
  description = "Master username for the RDS MySQL instance"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Master password for the RDS MySQL instance"
  type        = string
  sensitive   = true
}