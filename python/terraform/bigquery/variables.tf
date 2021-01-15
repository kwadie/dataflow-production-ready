# BigQuery variables

variable "dataset_name" {
  default = "dataflow_production_ready"
}

variable "dataset_location" {
  description = "Dataset Location"
  default     = "EU"
}