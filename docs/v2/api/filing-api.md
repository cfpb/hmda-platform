# HMDA Platform Filing API

This documentation describes the HMDA Platform Filing HTTP API


## HTTP Endpoints

### Root

 `/`

`GET` - Root endpoint with information about the HMDA Platform service. Used for health checks.

Example response:

```json
{
  "status": "OK",
  "service": "hmda-filing-api",
  "time": "2016-06-17T13:54:10.725Z",
  "host": "localhost"
}
```


Quick links:



### Filings

`/institutions/<institutionId>/filings/<period>`

`GET` - Returns the details for a filing for an institution and filing period

Example response:

```json
{
  "filing": {
    "period": "2018",
    "lei": "12345",
    "filingRequired": true,
    "status": {
      "code": 1,
      "message": "not-started"
    },
    "start": 0,
    "end": 0
  },
  "submissions": [
    {
      "id": {
        "lei": "12345",
        "period": "2018",
        "sequenceNumber": 1
      },
      "status": {
        "code": 1,
        "message": "created",
        "description": "The filing period is now open and available to accept HMDA data."
      },
      "fileName": "bank1_hmda_2018.txt",
      "receipt": "",
      "start": 1483287071000,
      "end": 0
    },
    {
      "id": {
        "lei": "12345",
        "period": "2018",
        "sequenceNumber": 2
      },
      "status": {
        "code": 1,
        "message": "created",
        "description": "The filing period is now open and available to accept HMDA data."
      },
      "fileName": "bank1_lars_2018.txt",
      "receipt": "",
      "start": 1483287071000,
      "end": 0
    },
    {
      "id": {
        "lei": "12345",
        "period": "2018",
        "sequenceNumber": 3
      },
      "status": {
        "code": 1,
        "message": "created",
        "description": "The filing period is now open and available to accept HMDA data."
      },
      "fileName": "bank1_lars_2018.txt",
      "receipt": "",
      "start": 1483287071000,
      "end": 0
    }
  ]
}
```

`POST` - Creates a new filing for the institution and period

Returns the filing details of the filing created (see above for `JSON` format)
