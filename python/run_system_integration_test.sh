
#!/bin/bash

# Integration testing:
# 1. Run the job from the deployed flex template
# 2. Check periodically for job status from the Dataflow API
# 3. Fail/Pass the integration test based on the job final status

#TODO:
# * handle gcloud command failures in each step and stop the script
# * Upload input file(s) to GCS and read from there
# * Count number of records/lines in input file
# * Apply Test
# * On job success: get table row count from the output tables, sum them and assert they are equal initial row count
# * Clean up: delete input data and output tables


# Run flex template and parse the JobID
JOB_ID_LINE=$(gcloud dataflow flex-template run "ml-preproc-flex-`date +%Y%m%d-%H%M%S`" \
  --project="${GCP_PROJECT}" \
  --region="${REGION}" \
  --template-file-gcs-location "${TEMPLATE_GCS_LOCATION}" \
  --parameters input-csv="${INPUT_CSV}"  \
  --parameters results-bq-table="${BQ_RESULTS}" \
  --parameters errors-bq-table="${BQ_ERRORS}" \
  --parameters setup_file=$SETUP_FILE | grep 'id:')

JOB_ID=$(echo $JOB_ID_LINE | sed -e "s/^id://")

# Dataflow service job status.
SUCCESS_JOB_STATUS=JOB_STATE_DONE
FAILED_JOB_STATUS=JOB_STATE_FAILED

# Check periodically for job status (Success or Failure)
while :; do
  CURRENT_JOB_STATUS_LINE=$(gcloud dataflow jobs describe $JOB_ID --region=$REGION | grep 'currentState:')
  CURRENT_JOB_STATUS=$(echo $CURRENT_JOB_STATUS_LINE | sed -e "s/^currentState://")
  echo "JobID ${JOB_ID} current status = ${CURRENT_JOB_STATUS} .."
  if [ $CURRENT_JOB_STATUS = $SUCCESS_JOB_STATUS ]; then
    echo "JobID ${JOB_ID} finished successfully."
    break
  elif [ $CURRENT_JOB_STATUS = $FAILED_JOB_STATUS ]; then
    echo "ERROR: JobID ${JOB_ID} failed."
    exit 1
    break
  fi
  sleep 60s
done

