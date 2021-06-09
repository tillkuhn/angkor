variable "name" {
  description = "the name of the table"
}

variable "wcu" {
  description = "Default write capacity units defaults to 1"
  default     = "1"
}

variable "rcu" {
  description = "Default read capacity units defaults to 1"
  default     = "1"
}

variable "tags" {
  type        = map(any)
  description = "Tags to attached to the table, Name tag will be added by the module"
  default     = {}
}

variable "primary_key" {
  default     = "id"
  description = "Primary key used as value for hashkey, defaults to id"
}

variable "primary_key_type" {
  default     = "S"
  description = "Default datatype for hashkey, defaults to String"
}
