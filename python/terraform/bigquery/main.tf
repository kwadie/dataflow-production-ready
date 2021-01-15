# Set up BQ Dataset
resource "google_bigquery_dataset" "bq_demo_dataset" {
  dataset_id = var.dataset_name
  location   = var.dataset_location
}

# Set up results table
resource "google_bigquery_table" "bq_demo_dataset_results" {
  dataset_id = google_bigquery_dataset.bq_demo_dataset.dataset_id
  table_id   = "ml_preproc_results"
  schema     = file("schema/ml_preproc_results.json")
}

# Set up errors table
resource "google_bigquery_table" "bq_demo_dataset_errors" {
  dataset_id = google_bigquery_dataset.bq_demo_dataset.dataset_id
  table_id   = "ml_preproc_errors"
  schema     = file("schema/ml_preproc_errors.json")
}