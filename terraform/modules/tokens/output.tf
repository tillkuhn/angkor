output "api_token_metrics" {
  value = base64encode(jsonencode({
    # jwt style fields https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-token-claims#registered-claims
    "iss"   = var.app_id   # Identifies principal that issued the JWT.
    "aud"   = "prometheus" # (audience): Recipient for which the JWT is intended
    "token" = "${random_pet.api_token_metrics.id}-${random_string.api_token_metrics.id}"
  }))
}

