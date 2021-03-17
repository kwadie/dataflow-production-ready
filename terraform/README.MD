
## Usage
```
export PROJECT_ID=<project_id>`
terraform init
terraform plan
terraform apply -var=project=${PROJECT_ID}
```
To automate the execution of the terraform scripts cloudbuild_tf.yaml file could be used from the root folder. It will trigger Cloud Build pipeline which will execute terraform init, terraform plan and terraform apply steps.

Terraform scripts are used to automate bq dataset and tables creation. It will create a BQ dataset with default name "dataflow_production_ready" (could be configured via tf variables) and populate it with tables generated based on the files in the `schemas` folder

Terraform script accepts 2 variables:
project (required) - id of the gcp project used to deploy components
bq_dataset_name (optional, default = "dataflow_production_ready") - bigquery dataset name

Terraform automation is configured to use a gcs bucket as a backend. So it assumes the bucket defined in backend.tf is created and the account running terraform scripts has enough permissions to access it.

For the sake of simplicity we put terraform configuration to the same folder as the dataflow pipeline. In the production environment though we recommend moving the terraform configuration to the separate repository to be able to modify the infrastructure and the pipeline independently.

