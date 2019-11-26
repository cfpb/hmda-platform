#!/bin/sh
 BEARER=$(curl -X POST $3 -d 'client_id=hmda2-api&grant_type=password&username='"$1"'&password='"$2"''| awk -F'"' '$2=="access_token"{print $4}')
echo $BEARER


#$1 user name
#$2 password
#$3 auth url