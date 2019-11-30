#!/bin/sh
 BEARER=$(curl -X POST $3 -d "$AUTH_DATA"| awk -F'"' '$2=="access_token"{print $4}')
echo $BEARER
