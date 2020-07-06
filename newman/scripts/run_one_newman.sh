#!/bin/bash
#
# Run ./run_one_newman.sh 

CONFIG_PATH="tests/hmda-filing/configs/$NEWMAN_YEAR/test_no_m_no_q/hmda-filing-api-config.json"

curl --location --request PUT $HOST_ADMIN'/institutions' --header 'Content-Type: application/json' --header "Authorization: Bearer $authToken" --data-raw '{ "activityYear": 2019, "lei": "NEWMANLEITEST1234678", "agency": 9, "institutionType": -1, "institutionId2017": "", "taxId": "84-7652134", "rssd": -1, "emailDomains": [ "newmantest.com" ], "respondent": { "name": "Newman Bank", "state": "", "city": "" }, "parent": { "idRssd": -1, "name": "" }, "assets": 5, "otherLenderCode": -1, "topHolder": { "idRssd": -1, "name": "" }, "hmdaFiler": true, "quarterlyFiler": true, "quarterlyFilerHasFiledQ1": false, "quarterlyFilerHasFiledQ2": false, "quarterlyFilerHasFiledQ3": false }'

newman run tests/hmda-filing/tests/test_no_m_no_q/hmda-filing-api-test.json -d \
$CONFIG_PATH \
--env-var host_filing=$HOST_FILING \
--env-var host_admin_api=$HOST_ADMIN \
--env-var host_public=$HOST_PUBLIC \
--env-var authToken="Bearer $authToken" 

echo "done"
