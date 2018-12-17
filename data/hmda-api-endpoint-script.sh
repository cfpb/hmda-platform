#!/bin/bash
#jq - https://stedolan.github.io/jq/

## TODO - Expand the script for more endpoints and lei/submission year to be taken as input parameters. 
## 1 - Create institution 
## 2 - Start Filing
## 3 - Create Submissoion
## 4 - Upload file


for i in $(seq 1 $1); do 
	sequenceNumber="$(http --check-status --ignore-stdin POST  http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions 2>&1 | jq '.id.sequenceNumber')"
	echo "Create-Submission: $sequenceNumber"
	UPLOAD_FILE="Upload-file: $(http --check-status --ignore-stdin --form POST  http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/$sequenceNumber file@clean_test_files/bank_0/clean_file_10_rows_Bank0_syntax_validity.txt 2>&1 | jq '.id.sequenceNumber')"
	echo "Upload-File: $UPLOAD_FILE"
	sleep 1
	VERIFY_STATUS_CODE="Verify-edits: $(http --check-status --ignore-stdin POST  http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/$sequenceNumber/edits/quality verified:=true 2>&1 | jq '.status.code')"
	echo "Verify-Submission: $VERIFY_STATUS_CODE"
	SIGN_STATUS_CODE="Sign-submission: $(http --check-status --ignore-stdin POST  http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/$sequenceNumber/sign signed:=true 2>&1 | jq '.status.code')"
	echo "Sign-Submission: $SIGN_STATUS_CODE"
done