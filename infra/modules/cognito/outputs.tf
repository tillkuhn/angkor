output "pool_id" {
  value = aws_cognito_user_pool.main.id
}

// e.g. "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_NvRKQYlaB"
output "pool_issuer_uri" {
  value = "https://${aws_cognito_user_pool.main.endpoint}"
}

output "app_client_name" {
  value = aws_cognito_user_pool_client.main.name
}

output "app_client_id" {
  value = aws_cognito_user_pool_client.main.id
}

output "app_client_secret" {
  value = aws_cognito_user_pool_client.main.client_secret
}
