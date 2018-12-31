#!/bin/zsh
#jq - https://stedolan.github.io/jq/


## usage ./hmda4-api-endpoint-script.sh <number of requests> <username> <password>
## Provide username and password for HMDA4

## $1 -- Number of submissions
## $2 -- username
## $3 -- password

## TODO - Expand the script for more endpoints and lei/submission year to be taken as input parameters. 
## 1 - Create institution 
## 2 - Start Filing
## 3 - Create Submissoion
## 4 - Upload file


for i in $(seq $1 ); do 
	token="$(http --check-status --ignore-stdin --form POST  https://hmda4.demo.cfpb.gov/auth/realms/hmda2/protocol/openid-connect/token client_id=hmda2-api grant_type=password username=$2 password=$3 2>&1 | jq '.access_token')"
	token=${token:1}
	token=${token: : -1}
	echo "HMDA4-Token: Fetched"
	sequenceNumber="$(http --check-status --ignore-stdin POST https://hmda4.demo.cfpb.gov/v2/filing/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions "authorization:Bearer $token" 2>&1 | jq '.id.sequenceNumber')" 
	echo "Create-Submission: $sequenceNumber"
	UPLOAD_FILE="Upload-file: $(http --check-status --ignore-stdin --form POST  https://hmda4.demo.cfpb.gov/v2/filing/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/$sequenceNumber file@clean_test_files/bank_0/clean_file_10000_rows_Bank0_syntax_validity.txt "authorization:Bearer $token" )"
	echo "Upload-File: $UPLOAD_FILE"
	
	sleep 20
	# LATEST="LATEST: $(http   GET  https://hmda4.demo.cfpb.gov/v2/filing/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/latest "authorization:Bearer $token" )"
	# echo "Latest: $LATEST"			
	# VERIFY_STATUS_CODE="Verify-edits: $(http  POST  https://hmda4.demo.cfpb.gov/v2/filing/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/$sequenceNumber/edits/quality verified:=true "authorization:Bearer $token" )"
	# echo "Verify-Submission(quality): $VERIFY_STATUS_CODE"
	# VERIFY_MACRO_CODE="Verify-edits: $(http  POST  http://hmda4.demo.cfpb.gov/v2/filing/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/$sequenceNumber/edits/macro verified:=true "authorization:Bearer $token" )"
	# echo "Verify-Submission(macro): $VERIFY_MACRO_CODE"	
	# sleep 5	
	# SIGN_STATUS_CODE="Sign-submission: $(http  POST  http://hmda4.demo.cfpb.gov/v2/filing/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/$sequenceNumber/sign signed:=true "authorization:Bearer $token" )"
	# echo "Sign-Submission: $SIGN_STATUS_CODE"
	# sleep 3
done