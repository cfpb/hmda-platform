#!/bin/sh

  BEARER=$(curl --location --request POST $3 \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode "client_id=${4}" \
--data-urlencode 'grant_type=password' \
--data-urlencode "username=${1}" \
--data-urlencode "password=${2}" | awk -F'"' '$2=="access_token"{print $4}')
echo $BEARER


#$1 username
#$2 password
#$3 auth url
#$4 client_id