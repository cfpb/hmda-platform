#!/bin/sh
 BEARER=$(curl -X POST $3 -d 'client_id='"$4"'&grant_type=password&username='"$1"'&password='"$2"''| awk -F'"' '$2=="access_token"{print $4}')
echo $BEARER

#$1 username
#$2 password
#$3 auth url
#$4 client_id