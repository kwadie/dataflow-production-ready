terraform {
  backend "gcs" {
    bucket = "dataflow-tf-backend"
    prefix = "state"
  }
}
