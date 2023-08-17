# HMDA Auth API

## Health Endpoint

```shell
curl -XGET {{host}}:9095
```

The response should be similar to the following:

```json
  {
    "status":"OK",
    "service":
    "hmda-auth-api",
    "time":"2018-08-08T19:08:20.655Z",
    "host":"{{host}}"
  }
```

## Update User Account

```shell
curl --location --request PUT 'https://host:9095/users/' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{token}}' \
--data '{"firstName": "{{user first name}}", "lastName": "{{user last name}}", "leis": ["{{lei1}}", "{{lei2}}"]}'
```

Response:

```json
  {
    "firstName": "{{user first name}}",
    "lastName": "{{user last name}}",
    "leis": [
        "{{lei1}}", "{{lei2}}"
    ]
}
```