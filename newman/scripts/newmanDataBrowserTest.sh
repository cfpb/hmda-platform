#!/bin/bash

 test=$(./node_modules/.bin/newman run dev/data-browser/data-browser-api-test.json -d\
 dev/data-browser/data-browser-api-config.json \
 --env-var db-api=$1 \
 --bail |\
  tee scripts/results.txt)

#extract the failure details of the test
testResults=$(sed -n '/failure/,$p' scripts/results.txt)

#escape newman output for curl command
data="$( jq -nc --arg str "# $2 Newman Error (Data Browser Test): $testResults" '{"text": $str}' )"

#post message is error is found
 if [[ $data == *"failure"* ]]; then
     curl -i -X POST -H 'Content-Type: application/json' -d "$data" $3
fi


# Keycloak env vars
#$1 Data Browser API URL
#$2 Kube Environment
#$3  MM Hook



