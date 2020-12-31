#  Copyright 2020 Google LLC.
#  This software is provided as-is, without warranty or representation for any use or purpose.
#  Your use of it is subject to your agreement with Google.
from .data_classes import _input_fields, HEADER


def _results_schema():
  fields = _input_fields(HEADER)

  # We just STRING as we parse the CSV as strings
  # TODO: If we change any of the types in Accomodation to bool, string, etc, we need to adapt the schema here too
  schema = ['%s:STRING' % f for f in fields]

  return ','.join(schema)


ERRORS_SCHEMA = 'error:STRING,line:STRING'

# TODO: add more columns for other features calculations
RESULTS_SCHEMA = _results_schema() + ',' + ','.join(['address_similarity:FLOAT64', 'city_similarity:FLOAT64'])