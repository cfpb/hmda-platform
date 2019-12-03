#!/bin/bash

authToken=$(./scripts/authTokenGen.sh $1 $2 $3 $4)
test=$(./node_modules/.bin/newman run hmda-filing/hmda-filing-api-test.json -d\
 hmda-filing/hmda-filing-api-config.json \
 --env-var host_filing=$5 \
 --env-var host_admin_api=$6 \
 --env-var host_public=$7 \
 --env-var authToken="Bearer $authToken" \
 --bail |\
  tee scripts/results.txt)

#extract the failure details of the test
testResults=$(sed -n '/failure/,$p' scripts/results.txt)

#escape newman output for curl command
data="$( jq -nc --arg str "# $8 Newman Error (Filing Test): $testResults" '{"text": $str}' )"

#post message is error is found
 if [[ $data == *"failure"* ]]; then
     curl -i -X POST -H 'Content-Type: application/json' -d "$data" $9
fi


# Keycloak env vars
#$1 $KC_UN
#$2 $KC_PW
#$3 $KC_URL
#$4 $KC_CLIENT_ID

#API en vars
#$5 $HOST_FILING
#$6 $HOST_ADMIN
#$7 $HOST_PUBLIC

#MM Web hook Env Vars
#$8 $HMDA_ENV
#$9 $MM_HOOK



