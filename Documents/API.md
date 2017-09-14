# HMDA Platform API

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

### Institutions endpoints

All endpoints in the `/institutions` namespace require two headers (see ["Authorization" section](#authorization) below for more detail):

- `CFPB-HMDA-Username`, containing a string
- `CFPB-HMDA-Institutions`, containing a list of integers

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

### Institutions

`/institutions`

`GET` - Returns the list of institutions

  Example response:

```json
{
  "institutions": [
    {
      "id": "12345",
      "name": "First Bank",
    },
    {
      "id": "123456",
      "name": "Second Bank",
    }
  ]
}
```

### Institutions by ID

`/institutions/<institutionId>`

`GET` - Returns the details for an institution based on ID

Example response:

```json
{
  "institution": {
    "id": "12345",
    "name": "First Bank",
  },
  "filings": [
    {
      "period": "2017",
      "institutionId": "12345",
      "filingRequired": true,
      "status": {
        "code": 1,
        "message": "not-started"
      },
      "start": 0,
      "end": 0
    },
    {
      "period": "2016",
      "institutionId": "12345",
      "filingRequired": false,
      "status": {
        "code": 3,
        "message": "completed"
      },
      "start": 1483287071000,
      "end": 1514736671000
    }
  ]
}
```

### Filings

`/institutions/<institutionId>/filings/<period>`

`GET` - Returns the details for a filing for an institution and filing period

Example response:

```json
{
  "filing": {
    "period": "2017",
    "institutionId": "12345",
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
        "institutionId": "12345",
        "period": "2017",
        "sequenceNumber": 1
      },
      "status": {
        "code": 1,
        "message": "created",
        "description": "The filing period is now open and available to accept HMDA data."
      },
      "fileName": "bank1_hmda_2017.txt",
      "receipt": "",
      "start": 1483287071000,
      "end": 0
    },
    {
      "id": {
        "institutionId": "12345",
        "period": "2017",
        "sequenceNumber": 2
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
    },
    {
      "id": {
        "institutionId": "12345",
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
      "end": 0
    }
  ]
}
```

### Submissions

`/institutions/<institutionId>/filings/<period>/submissions`

`POST` - Create a new submission for an institution and filing period

Example response, with HTTP code 201:

```json
{
  "id": {
    "institutionId": "0",
    "period": "2016",
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

`/institutions/<institutionId>/filings/<period>/submissions/latest`

`GET` - Returns the latest submission for an institution and filing period

Example response:

```json
{
  "id": {
    "institutionId": "0",
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

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>`

`POST` - Upload HMDA data to submission

Example response:

```json
{
  "id": {
    "institutionId": "0",
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
    "institutionId": "0",
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

### Parse errors

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/parseErrors`

`GET` - Returns all parsing errors for a submission

| Query parameter | Description |
| --------------- | ----------- |
| page | Integer. If blank, will default to page 1. Page size is 20 lines of errors. |


The `larErrors` array in this endpoint is paginated. The response contains 3 fields of pagination metadata:

 - `total`: total number of LAR parser errors for this file
 - `count`: number of errors returned on this page. Full page contains errors from 20 LARs of the HMDA file.
 - `links`: the `href` field is the path to this resource, with a `{rel}` to be replaced with the query strings in the `first`, `prev`, `self`, `next`, `last` fields.


Example response, with HTTP code 201:

```json
{
  "transmittalSheetErrors": [
    "Record Identifier is not an Integer",
    "Agency Code is not an Integer"
  ],
  "larErrors": [
    {
      "lineNumber": 2,
      "errorMessages": [
        "Incorrect number of fields. found: 32, expected: 39"
      ]
    },
    {
      "lineNumber": 4,
      "errorMessages": [
        "Record Identifier is not an Integer"
      ]
    },
    {
      "lineNumber": 11,
      "errorMessages": [
        "Loan Type is not an Integer",
        "Property Type is not an Integer",
        "Loan Purpose is not an Integer",
        "Owner Occupancy is not an Integer"
      ]
    }
  ],
  "count": 20,
  "total": 130,
  "status": {
      "code": 5,
      "message": "parsed with errors",
      "description": "The data are not formatted according to certain formatting requirements specified in the Filing Instructions Guide. The filing process may not proceed until the data have been corrected and the file has been reuploaded."
  },
  "_links": {
    "first": "?page=1",
    "prev": "?page=1",
    "self": "?page=1",
    "next": "?page=2",
    "last": "?page=7",
    "href": "/institutions/1/filings/2017/submissions/1/parseErrors{rel}"
  }
}
```

### Edits

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits`

`GET`  - Returns a list of all edits for a given submission, including the edit name and description

Results are grouped by edit type.

Example response:

```json
{
  "syntactical": {
    "edits": [
      {
        "edit": "S020",
        "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
      }
      {
        "edit": "S010",
        "description": "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR).",
      }
    ]
  },
  "validity": {
    "edits": [
      {
        "edit": "V555",
        "description": "If loan purpose = 1 or 3, then lien status must = 1, 2, or 4.",
      },
      {
        "edit": "V560",
        "description": "If action taken type = 1-5, 7 or 8, then lien status must = 1, 2, or 3.",
      }
    ]
  },
  "quality": {
    "verified": false,
    "edits": []
  },
  "macro": {
    "verified": false,
    "edits": [
      {
        "edit": "Q023",
        "description": "The number of loan applications that report MSA/MD = NA should be ≤ 30% of the total number of loan applications."
      }
    ]
  },
  "status": {
      "code": 8,
      "message": "validated with errors",
      "description": "The data validation process is complete, but there are edits that may need to be addressed."
  }
}
```

### Edit Details

`/institutions/<institution>/filings/<period>/submissions/<submissionId>/edits/<edit>`

`GET` - For an edit, return a collection of all rows that failed it, including the relevant fields and their values.


| Query parameter | Description |
| --------------- | ----------- |
| page | Integer. If blank, will default to page 1. Page size is 20 lines of errors. |

This endpoint is paginated. The response contains 3 fields of pagination metadata:

 - `total`: total number of parser errors for this file
 - `count`: number of errors returned on this page. Full page contains errors from 20 lines of the HMDA file.
 - `links`: the `href` field is the path to this resource, with a `{rel}` to be replaced with the query strings in the `first`, `prev`, `self`, `next`, `last` fields.


Example response:

```json
{
  "edit": "Q036",
  "rows": [
    {
      "row": {
        "rowId": "4514746044"
      },
      "fields": {
        "Property Type": 2,
        "Loan Amount": 213
      }
    },
    {
      "row": {
        "rowId": "6072140231"
      },
      "fields": {
        "Property Type": 2,
        "Loan Amount": 185
      }
    },
    {
      "row": {
        "rowId": "7254350246"
      },
      "fields": {
        "Property Type": 2,
        "Loan Amount": 252
      }
    }
  ],
  "count": 3,
  "total": 3,
  "_links": {
    "self": "?page=1",
    "prev": "?page=1",
    "last": "?page=1",
    "next": "?page=1",
    "first": "?page=1",
    "href": "/institutions/2/filings/2017/submissions/2/edits/Q036{rel}"
  }
}
```


### Edits by type

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/<syntactical|validity|quality|macro>`

`GET` - Returns a list of edits of a specific type, for a given submission

Example response:

```json
{
  "edits": [
    {
      "edit": "S020",
      "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
    },
    {
      "edit": "S010",
      "description": "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR).",
    }
  ],
  "status": {
    "code": 8,
    "message": "validated with errors",
    "description": "The data validation process is complete, but there are edits that may need to be addressed."
  }
}
```


`POST` - Provides verification for quality or macro edits

_Specific to the `/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/<quality|macro>` endpoint._

Example payload, in `JSON` format:

```json
{
  "verified": true
}
```

Example response:

```json
{
  "verified": true,
  "status": {
    "code": 8,
    "message": "validated with errors",
    "description": "The data validation process is complete, but there are edits that may need to be addressed."
  }
}
```


### Edits CSV

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/csv`

`GET` - Returns a list of all validation errors for the submission, formatted as CSV for use in spreadsheet software

Example response:

```csv
editType, editId, loanId
Syntactical, S020, Transmittal Sheet
Validity, V125, Transmittal Sheet
Quality, Q027, 9553605194
Quality, Q027, 9401122359
Quality, Q027, 2156575876
Quality, Q027, 12073393.95
Quality, Q027, 4101572269
Quality, Q027, 4748775957
Quality, Q027, 6024192341
Quality, Q027, 621742533.4
Macro, Q023,
```


### IRS

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs`

`GET` - Returns the Institution Register Summary

| Query parameter | Description |
| --------------- | ----------- |
| page | Integer. If blank, will default to page 1. Page size is 20 MSAs. |


This endpoint is paginated. The response contains 3 fields of pagination metadata:

 - `total`: total number of MSAs for this file.
 - `count`: number of MSAs returned on this page.
 - `_links`: the `href` field is the path to this resource, with a `{rel}` to be replaced with the query strings in the `first`, `prev`, `self`, `next`, `last` fields.


Example response:

```json
{
  "msas": [
    {
      "id": "123",
      "name": "Some, Place",
      "totalLARS": 4,
      "totalAmount": 123,
      "conv": 4,
      "FHA": 0,
      "VA": 0,
      "FSA": 0,
      "oneToFourFamily": 4,
      "MFD": 0,
      "multiFamily": 0,
      "homePurchase": 0,
      "homeImprovement": 0,
      "refinance": 4
    },
    {
      "id": "456",
      "name": "Other, Place",
      "totalLARS": 5,
      "totalAmount": 456,
      "conv": 5,
      "FHA": 0,
      "VA": 0,
      "FSA": 0,
      "oneToFourFamily": 5,
      "MFD": 0,
      "multiFamily": 0,
      "homePurchase": 0,
      "homeImprovement": 0,
      "refinance": 5
    }
  ],
  "summary": {
    "homeImprovement": 0,
    "multiFamily": 0,
    "lars": 9,
    "FSA": 0,
    "FHA": 0,
    "amount": 579,
    "oneToFourFamily": 9,
    "refinance": 9,
    "MFD": 0,
    "conv": 9,
    "homePurchase": 0,
    "VA": 0
  },
  "count": 20,
  "total": 130,
  "_links": {
    "first": "?page=1",
    "prev": "?page=1",
    "self": "?page=1",
    "next": "?page=2",
    "last": "?page=7",
    "href": "/institutions/1/filings/2017/submissions/1/irs{rel}"
  }
}
```

### IRS CSV

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs/csv`

`GET` - Returns the Institution Register Summary in CSV format

Example response:
```csv
MSA/MD, MSA/MD Name, Total LARs, Total Amt. (in thousands), CONV, FHA, VA, FSA/RHS, 1-4 Family, MFD, Multi-Family, Home Purchase, Home Improvement, Refinance
45460,"Terre Haute, IN", 7, 297, 7, 0, 0, 0, 7, 0, 0, 0, 4, 3
NA,"NA", 3, 79, 3, 0, 0, 0, 3, 0, 0, 0, 1, 2
Totals,, 10, 376, 10, 0, 0, 0, 10, 0, 0, 0, 5, 5
```


### Signature

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/sign`

`GET`  - Returns a receipt

Example response:
```json
{
  "timestamp": 1476809530772,
  "receipt": "asd0f987134asdlfasdflk",
  "status": {
      "code": 10,
      "message": "signed",
      "description": "Your financial institution has certified that the data is correct. This completes the HMDA filing process for this year."
    }
}
```

`POST`  - Sign the submission

Example body:
```json
{
  "signed": true
}
```

Example response:
```json
{
  "timestamp": 1476809530772,
  "receipt": "asd0f987134asdlfasdflk",
  "status": {
    "code": 10,
    "message": "signed",
    "description": "Your financial institution has certified that the data is correct. This completes the HMDA filing process for this year."
  }
}
```

### Summary

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/summary`

`GET`  - Returns a submission summary

Example response:

```json
{
  "respondent": {
    "name": "Bank",
    "id": "1234567890",
    "taxId": "0987654321",
    "agency": "cfpb",
    "contact": {
      "name": "Your Name",
      "phone": "123-456-7890",
      "email": "your.name@bank.com"
    }
  },
  "file": {
    "name": "lar.dat",
    "year": "2016",
    "totalLARS": 25
  }
}
```

## Authorization

Each endpoint that starts with `/institutions` is protected by three authorization requirements.

- Requests must include the `CFPB-HMDA-Username` header.
  - Its value should be the username of the user making the request.
- Requests must include the `CFPB-HMDA-Institutions` header.
  - This header will contain the comma-separated list of institution IDs
    that the user is authorized to view.
- For requests to institution-specific paths, such as `/institutions/<institutionId>`
  and `/institutions/<institutionId>/summary` (any endpoint except `/institutions`),
  the institution ID requested must match one of the IDs in the `CFPB-HMDA-Institutions`
  header.
