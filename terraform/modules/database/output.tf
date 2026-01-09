#output "something" {
#  value = resource.value
#}


output "branches" {
  value = data.neon_branches.main
}

output "main_branch" {
  value = local.main_branch
}

output "main_branch_roles" {
  value = data.neon_branch_roles.main
}

output "db_url_prod" {
  #sensitive = true
  # pattern sql 'postgresql://<user_owner>:<password>@<database_host>/<database>?sslmode=require&channel_binding=require'
  # channel_binding=require is a crucial security parameter or SCRAM-SHA-256-PLUS authentication, ensuring mutual trust by cryptographically linking your TLS connection

  # todo: migrate db name from jellyfish to ${neon_database.prod.name} once we migrated the content
  value = "jdbc:postgresql://${data.neon_project.main.database_host}/${neon_database.prod.name}?sslmode=require&channel_binding=require"
  ##value = "jdbc:postgresql://${data.neon_project.main.database_host}/jellyfish?sslmode=require&channel_binding=require"
}

output "db_user_prod" {
  value = neon_role.owner.name
}


output "db_password_prod" {
  value     = neon_role.owner.password
  sensitive = true
}

