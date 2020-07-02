#!/bin/bash

authToken=$(./scripts/authTokenGen.sh $KC_UN $KC_PW $KC_URL $KC_CLIENT_ID) && \
newman run tests/hmda-filing/tests/test_no_m_no_q/hmda-filing-api-test.json -d \
tests/hmda-filing/configs/$YEAR/test_no_m_no_q/hmda-filing-api-config.json \
--env-var host_filing=$HOST_FILING \
--env-var host_admin_api=$HOST_ADMIN \
--env-var host_public=$HOST_PUBLIC \
--env-var authToken="Bearer $authToken" \
--bail