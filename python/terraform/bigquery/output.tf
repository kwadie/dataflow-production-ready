output "project_name" {
  value = google_bigquery_dataset.bq_demo_dataset.project
}

output "bq_demo_dataset" {
  value = google_bigquery_dataset.bq_demo_dataset.dataset_id
}

output "bq_demo_dataset_results" {
  value = google_bigquery_table.bq_demo_dataset_results.table_id
}

output "bq_demo_dataset_errors" {
  value = google_bigquery_table.bq_demo_dataset_errors.table_id
}
