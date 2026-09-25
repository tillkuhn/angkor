# Terraform Agent Guidelines

## Build/Lint/Test Commands

**Primary tool**: OpenTofu (`tofu`) not Terraform
- `make init` - Initialize Terraform working directory
- `make plan` - Format, validate, and show execution plan
- `make apply` - Apply changes without prompting  
- `make fmt` - Format all Terraform files recursively
- `make check` - Check formatting without modifying files
- `make update` - Update provider versions

**Prerequisites**: AWS_PROFILE must be set, terraform.tfvars and terraform-backend.tf must exist (copy from .tmpl files)

## Code Style Guidelines

### File Structure
- 2-space indentation everywhere
- Use `./modules/{name}` for all reusable modules
- Each module: main.tf, variables.tf, outputs.tf, versions.tf (some older modules still use `output.tf`/`variabels.tf` or omit `versions.tf` — prefer the standard names for new/updated modules)
- Root level: main.tf, variables.tf, outputs.tf, providers.tf, versions.tf

### Naming Conventions  
- Modules: lowercase single words (s3, ec2, iam, cognito); a few pre-existing modules use snake_case (secrets_read, secrets_write)
- Resources: `{appid}-{resource-type}-{descriptor}` pattern
- Variables: snake_case with descriptions
- Locals: `local.tags` and `local.common_tags` for tagging

### Tagging Strategy
- Always merge tags: `merge(local.tags, var.tags, tomap({"Name" = resource-name}))`
- Module tags: `terraformModule` tag with module name
- Common tags: appid, managedBy ("terraform"), releaseName, releaseVersion

### Provider Management
- AWS provider in eu-central-1 region
- Use Phase.dev for secrets, Confluent for Kafka, Grafana for monitoring
- Provider versions use pessimistic constraints (`~> X.Y`)

### Security & Patterns
- Sensitive values via Phase provider, not plain variables
- EC2 instance has a toggleable `ignore_changes` lifecycle block for the AMI (commented in/out depending on whether AMI changes should force recreation)
- Dev resources with `-dev` suffix, prod without suffix