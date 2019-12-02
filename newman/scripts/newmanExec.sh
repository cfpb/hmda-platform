#!/bin/sh

authToken=$(./scripts/authTokenGen.sh $KC_UN $KC_PW  $KC_URL $KC_CLIENT_ID)
test=$(newman run dev/hmda-filing/hmda-filing-api-test.json -d\
 dev/hmda-filing/hmda-filing-api-config.json \
 --env-var host_filing=$HOST_FILING \
 --env-var host_admin_api=$HOST_ADMIN \
 --env-var host_public=$HOST_PUBLIC \
 --env-var authToken="Bearer $authToken" \
 --bail |\
  tee scripts/results.txt)

#extract the failure details of the test
test=$(sed -n '/failure/,$p' scripts/results.txt)

#escape newman output for curl command
data="$( jq -nc --arg str "# $HMDA_ENV Newman Error (Filing Test): $test" '{"text": $str}' )"

#post message is error is found
 if [[ $data == *"failure"* ]]; then
     curl -i -X POST -H 'Content-Type: application/json' -d "$data" $MM_HOOK
fi

