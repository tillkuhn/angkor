# secrets_write

Writes secrets that are generated (managed) by OpenTofu to [Phase](https://phase.dev), by default to path `/managed` of the given environment. Keys are uppercased (`upper_key = true`).

All information on secret management with Phase (environments and paths, which secrets are provided vs. managed, GitHub Actions sync, EC2 access, how to renew a provided secret, gotchas) is documented in the sibling module: [secrets_read/README.md](../secrets_read/README.md).

## Usage

```hcl
module "secrets_write_ci" {
  source = "./modules/secrets_write"
  app_id = var.phase_app_id
  env    = "staging" # development, staging or production
  # path = "/managed" (default)
  secrets = [
    {
      name  = "release_version" # stored as RELEASE_VERSION
      value = module.release.version
    },
  ]
}
```
