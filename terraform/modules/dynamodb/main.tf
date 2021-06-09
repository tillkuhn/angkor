resource "aws_dynamodb_table" "table" {
  name           = var.name
  read_capacity  = var.rcu
  write_capacity = var.wcu
  # (Required, Forces new resource) The attribute to use as the hash (partition) key. Must also be defined as an attribute
  hash_key = var.primary_key
  #   range_key      = "activityDate" ## v2.0 :-)
  attribute {
    name = var.primary_key
    type = var.primary_key_type
  }
  tags = merge({ "Name" : var.name }, var.tags)
}
