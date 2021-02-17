module "dataflow_bigquery" {
  source         = "./modules/bigquery"
  project = var.project
}