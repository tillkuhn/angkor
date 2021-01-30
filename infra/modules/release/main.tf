
variable "id" {
  type = string
  description = "Version to which the random name is tied as Keeper"
}

# Generate a new pet name each time we switch to a new AMI id
resource "random_pet" "release" {
  keepers = {
    version = var.id
  }
}

output "name" {
  value = random_pet.release.id
}

output "version" {
  value = random_pet.release.keepers.version
}
