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
- Each module: main.tf, variables.tf, outputs.tf, versions.tf
- Root level: main.tf, variables.tf, outputs.tf, providers.tf, versions.tf

### Naming Conventions  
- Modules: lowercase single words (s3, ec2, iam, cognito)
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
- EC2 instances use `ignore_changes = [ami]` for stability
- Dev resources with `-dev` suffix, prod without suffix