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

`/institutions/<lei>/filings/<period>`

`GET` - Returns the details for a filing for an institution and filing period

Example response:

```json
{
  "filing": {
    "period": "2018",
    "lei": "10Bx939c5543TqA1144M",
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
        "lei": "10Bx939c5543TqA1144M",
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
        "lei": "10Bx939c5543TqA1144M",
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
        "lei": "10Bx939c5543TqA1144M",
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


### Submissions

`/institutions/<lei>/filings/<period>/submissions`

`POST` - Create a new submission for an institution and filing period

Example response, with HTTP code 201:

```json
{
  "id": {
    "lei": "10Bx939c5543TqA1144M",
    "period": "2018",
    "sequenceNumber": 1
  },
  "status": {
    "code": 1,
    "message": "created",
    "description": "The filing period is now open and available to accept HMDA data."
  },
  "fileName": "bank1_lars_2017.txt",
  "receipt": "",
  "start": 1483287071000,
  "end": 0
}
```

### Latest submission

`/institutions/<lei>/filings/<period>/submissions/latest`

`GET` - Returns the latest submission for an institution and filing period

Example response:

```json
{
  "id": {
    "lei": "10Bx939c5543TqA1144M",
    "period": "2017",
    "sequenceNumber": 3
  },
  "status": {
    "code": 1,
    "message": "created",
    "description": "The filing period is now open and available to accept HMDA data."
  },
  "fileName": "bank1_lars_2017.txt",
  "receipt": "",
  "start": 1483287071000,
  "end": 1514736671000
}
```

### Submission by ID

`/institutions/<lei>/filings/<period>/submissions/<submissionId>`

`POST` - Upload HMDA data to submission

Example response:

```json
{
  "id": {
    "lei": "10Bx939c5543TqA1144M",
    "period": "2017",
    "sequenceNumber": 3
  },
  "status": {
    "code": 3,
    "message": "uploaded",
    "description": "The data have finished uploading and are ready to be analyzed."
  },
  "fileName": "bank1_lars_2017.txt",
  "receipt": "",
  "start": 1505142349962,
  "end": 0
}
```

Example response, with HTTP code 400:

```json
{
  "id": {
    "lei": "10Bx939c5543TqA1144M",
    "period": "2017",
    "sequenceNumber": 4848484
  },
  "status": {
    "code": -1,
    "message": "Submission 4848484 not available for upload",
    "description": "An error occurred during the process of submitting the data. Please re-upload your file."
  },
  "fileName": "",
  "receipt": "",
  "start": 0,
  "end": 0
}
```