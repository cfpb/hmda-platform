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

- [`/institutions`](#institutions)
- [`/institutions/<institutionId>`](#institutions-by-id)
- [`/institutions/<institutionId>/filings/<period>`](#filings)
- [`/institutions/<institutionId>/filings/<period>/submissions`](#submissions)
- [`/institutions/<institutionId>/filings/<period>/submissions/latest`](#latest-submission)
- [`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>`](#submission-by-id)
- [`/institutions/<institution>/filings/<period>/submissions/<submissionId>/parseErrors`](#parse-errors)
- [`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits`](#edits)
- [`/institutions/<institution>/filings/<period>/submissions/<submissionId>/edits/<syntactical|validity|quality|macro>`](#edits-by-type)
- [`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/csv`](#edits-csv)
- [`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/<edit>`](#edit-details)
- [`/institutions/<institution>/filings/<period>/submissions/<submissionId>/irs`](#irs)
- [`/institutions/<institution>/filings/<period>/submissions/<submissionId>/irs/csv`](#irs-csv)
- [`/institutions/<institution>/filings/<period>/submissions/<submissionId>/sign`](#signature)
- [`/institutions/<institution>/filings/<period>/submissions/<submissionId>/summary`](#summary)

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

Returns the filing details of the filing created (see above for `JSON` format) with `HTTP` code `201 (Created)`
