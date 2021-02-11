terraform {
  backend "gcs" {
    bucket = "tf-backend"
    prefix = "state"
  }
}
