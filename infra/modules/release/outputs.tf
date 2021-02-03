output "name" {
  value = random_pet.release.id
}

output "version" {
  value = random_pet.release.keepers.version
}
