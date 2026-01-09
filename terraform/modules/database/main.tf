locals {

}

# manage projects .. requires more permissions?
# https://registry.terraform.io/providers/kislerdm/neon/latest/docs/resources/project
#resource "neon_project" "main_db" {
#  name = "${var.app_id}-db"
#}

data "neon_project" "main" {
  id = var.project_id
}
data "neon_branches" "main" {
  project_id = data.neon_project.main.id
}

locals {
  main_branch = [for b in data.neon_branches.main.branches : b if b.name == "main"][0]
}


data "neon_branch_roles" "main" {
  project_id = data.neon_project.main.id
  branch_id  = local.main_branch.id
}

#data "neon_branch_endpoints" "example" {
#  project_id = data.neon_project.main.id
#  branch_id  = local.main_branch.id
#}

resource "neon_role" "owner" {
  project_id = data.neon_project.main.id
  branch_id  = local.main_branch.id
  name       = "${var.app_id}_owner"
}

resource "neon_database" "prod" {
  project_id = data.neon_project.main.id
  branch_id  = local.main_branch.id
  name       = "${var.app_id}-prod"
  owner_name = neon_role.owner.name
}

# to locate the main branch entry:
#   main_branch = [for b in local.branches : b if b.name == "main"][0]
#This creates a new local variable main_branch containing the object where name is "main".
#


# example branches:
#   + neon_branches                   = {
#       + branches   = [
#           + {
#               + id           = "br-xxxx-xxxx-00000001"
#               + logical_size = 54386688
#               + name         = "main"
#               + parent_id    = ""
#               + primary      = true
#             },
#           + {
#               + id           = "br-xxxx-xxxx-00000002"
#               + logical_size = 53665792
#               + name         = "feature/anonymous"
#               + parent_id    = "br-xxxx-xxxx-00000001"
#               + primary      = false
#             },
#         ]
#       + id         = "project-00000001/branches"
#       + project_id = "sparkling-credit-45897645"


# example roles
#   + neon_main_roles                 = {
#       + branch_id  = "br-xxxx-xxxx-00000001"
#       + id         = "project-00000001/br-xxxx-xxxx-00000001/roles"
#       + project_id = "project-00000001"
#       + roles      = [
#           + {
#               + name      = "role_owner"
#               + protected = false
#             },
#           + {
#               + name      = "role_1"
#               + protected = false
#             },
#           + {
#               + name      = "role_2"
#               + protected = false
#             },
#         ]
#     }

# resource "neon_database" "service_db" {
#   project_id = data.neon_project.main.id
#   branch_id  = "br-calm-mud-12334"
#   name       = "${var.app_id}-db"
#   owner_name = neon_role.app_user.name
# }

