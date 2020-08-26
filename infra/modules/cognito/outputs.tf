output "pool_id" {
  value = aws_cognito_user_pool.main.id
}

output "pool_endpoint" {
  value = aws_cognito_user_pool.main.endpoint
}

output "pool_client_id" {
  value = aws_cognito_user_pool_client.main.id
}

output "pool_client_secret" {
  value = aws_cognito_user_pool_client.main.client_secret
}
