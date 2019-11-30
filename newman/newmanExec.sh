 #!/bin/sh
authToken=$(./scripts/authTokenGen.sh $KEY_UN $KEY_PW  $KEY_URL)
test=$(newman run dev/hmda-filing/hmda-filing-api-test.json -d\
 dev/hmda-filing/hmda-filing-api-config.json \
 --env-var host_filing=$HOST_FILING \
 --env-var host_admin_api=$HOST_ADMIN \
 --env-var host_public=$HOST_PUBLIC \
 --env-var authToken="Bearer $authToken" \
 --bail |\
  tee newman2.txt)

#extract the failure details of the test
test=$(sed -n '/failure/,$p' newman2.txt)

#escape newman output for curl command
data="$( jq -nc --arg str "# Newman Error (Filing Test): $test" '{"text": $str}' )"

if [ '{"text":""}' = "$data" ]; then
    echo "All Clear."
else
  curl -i -X POST -H 'Content-Type: application/json' -d "$data" $MM_WEB_HOOK
fi
