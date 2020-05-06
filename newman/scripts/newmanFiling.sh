#!/bin/bash

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
#$10 $NEWMAN_NOTIFY=hourly

# ./scripts/newmanFiling.sh $KC_UN $KC_PW $KC_URL $KC_CLIENT_ID $HOST_FILING $HOST_ADMIN $HOST_PUBLIC $HMDA_ENV $MM_HOOK $NEWMAN_NOTIFY


declare -a TEST_TYPES=( "test_yes_m_no_q" "test_no_m_yes_q" "test_no_m_no_q" "test_yes_m_yes_q")
declare -a FILING_YEARS=("2018" "2019")

for filingYear in "${FILING_YEARS[@]}"
do
  for testType in "${TEST_TYPES[@]}"
 do
    authToken=$(./scripts/authTokenGen.sh $1 $2 $3 $4)
    test=$(newman run tests/hmda-filing/tests/"$testType"/hmda-filing-api-test.json -d \
     tests/hmda-filing/configs/"${filingYear}"/"$testType"/hmda-filing-api-config.json \
 --env-var host_filing=$5 \
 --env-var host_admin_api=$6 \
 --env-var host_public=$7 \
 --env-var authToken="Bearer $authToken" \
 --bail |
  tee scripts/results-allclear-"${filingYear}"-"${testType}".txt)

echo "${test}"
#extract the failure details of the test
testFiling=$(sed -n '/failure/,$p' scripts/results-allclear-"${filingYear}"-"${testType}".txt)

if [[ $testFiling != *"failure"* ]]; then
testResults+="# $8-${filingYear}  Filing API OK! (Filing $testType) :thumbs-up-bb8: "$'\n'"$testFiling"$'\n\n'
else
testResults+="# $8-${filingYear}  Something Disastrous Has Happend (Filing $testType) :starwars-darth:  "$'\n'"$testFiling"$'\n\n'
fi

rm -f scripts/results-allclear-"${filingYear}"-"${testType}".txt

done
done

mattermostPost() {
  if [[ -n $1 ]];then
    curl -i -X POST -H 'Content-Type: application/json' -d "$2" "${1}"
    fi
}

#escape newman output for curl command
data="$( jq -nc --arg str "$testResults" '{"text": $str}' )"

if [[ ${10} == *"hourly"*  && $data == *"failure"* ]]; then
 mattermostPost "${9}" "$data"

elif [[ ${10} == *"daily"* ]]; then
 mattermostPost "${9}" "$data"

fi
