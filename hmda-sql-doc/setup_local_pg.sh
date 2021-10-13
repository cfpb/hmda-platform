#!/bin/bash

# This script is for setting up a new local development postgres instance to support the hmda-platform.
# It is assumed the container is created with docker-compose provided in the project root directory.
# This script also requires the psql CLI to be installed

declare -a files=("institutions2019.sql" "institutions2020.sql" "institutions2021.sql" "institutions_history_notes.sql"
"lar_quarterly_2021_q1.sql" "lar_quarterly_2021_q2.sql" "lar_quarterly_2021_q3.sql" "modified-lar2019.sql" "modified-lar2020.sql"
"submissionHistory2019.sql" "submissionHistory.sql" "submissionHistoryMView.sql" "transmittalsheet2019.sql" "transmittalsheet2020.sql"
"ts_quarterly_2020_q1.sql" "ts_quarterly_2020_q2.sql" "ts_quarterly_2020_q3.sql" "ts_quarterly_2021_q1.sql" "ts_quarterly_2021_q2.sql"
"ts_quarterly_2021_q3.sql")

psql -h localhost -U postgres < setup_schema_users.sql

for file in "${files[@]}"; do
  psql -h localhost -U postgres hmda < $file
done