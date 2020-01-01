#!/bin/bash


declare -a TEST_TYPES=( "test_yes_m_no_q" "test_no_m_yes_q" "test_no_m_no_q" "test_yes_m_yes_q")
declare -a FILING_YEARS=("2018" "2019")

for filingYear in "${FILING_YEARS[@]}"
do
  scripts/results-"${filingYear}".txt
  for testType in "${TEST_TYPES[@]}"
  do

    authToken=$(./scripts/authTokenGen.sh "$1" "$2" "$3" "$4")
    test=$(./node_modules/.bin/newman run dev/hmda-filing/"${filingYear}"/"$testType"/hmda-filing-api-test.json -d \
     dev/hmda-filing/"${filingYear}"/"$testType"/hmda-filing-api-config.json \
     --env-var host_filing="$5" \
     --env-var host_admin_api="$6" \
     --env-var host_public="$7" \
     --env-var authToken="Bearer $authToken" \
     --bail |
     tee scripts/results-"${filingYear}"-"${testType}".txt)
     
     #extract the failure details of the test
     testResults=$(sed -n '/failure/,$p' scripts/results-"${filingYear}"-"${testType}".txt)
     
     #escape newman output for curl command
     data="$( jq -nc --arg str "# $8-${filingYear}  Newman Error (Filing $testType): $testResults" '{"text": $str}' )"
     
     #post message is error is found
      if [[ $data == *"failure"* ]]; then
        if [[ -n $9 ]];then
          curl -i -X POST -H 'Content-Type: application/json' -d "$data" "${9}"
          fi
          
        if [[ -n ${10} ]];then
          curl -i -X POST -H 'Content-Type: application/json' -d "$data" "${10}"
          fi
      fi
rm -f scripts/results-"${filingYear}"-"${testType}".txt
done
done
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
#$10 $ALT_HOOK



