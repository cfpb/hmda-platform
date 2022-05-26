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
- [`/institutions/<lei>`](#institutions-by-id)
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


### Institutions by id
`/institutions/<lei>`

`GET` - Returns the institution by its `lei` and its filings

Example response:

```json
{
    "institution": {
        "activityYear": 2014,
        "lei": "10Bx939c5543TqA1144M",
        "agency": 1,
        "institutionType": 17,
        "institutionId2017": "12345",
        "taxId": "99-00000000",
        "rssd": 12345,
        "emailDomains": [
            "bank1.com"
        ],
        "respondent": {
            "name": "xvavjuitZa",
            "state": "NC",
            "city": "Raleigh"
        },
        "parent": {
            "idRssd": 1520162208,
            "name": "Parent Name"
        },
        "assets": 450,
        "otherLenderCode": 1406639146,
        "topHolder": {
            "idRssd": 442825905,
            "name": "TopHolder Name"
        },
        "hmdaFiler": true,
        "quarterlyFiler": false
    },
    "filings": [
        {
            "period": "2018",
            "lei": "10Bx939c5543TqA1144M",
            "status": {
                "code": 2,
                "message": "in-progress"
            },
            "filingRequired": true,
            "start": 1541558493499,
            "end": 0
        },
        {
            "period": "2017",
            "lei": "10Bx939c5543TqA1144M",
            "status": {
                "code": 2,
                "message": "in-progress"
            },
            "filingRequired": true,
            "start": 1541558491086,
            "end": 0
        },
        {
            "period": "2019",
            "lei": "10Bx939c5543TqA1144M",
            "status": {
                "code": 2,
                "message": "in-progress"
            },
            "filingRequired": true,
            "start": 1541558485816,
            "end": 0
        }
    ]
}
```


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
      "end": 0,
      "qualityVerified": false,
      "macroVerified": false
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
      "end": 0,
      "qualityVerified": false,
      "macroVerified": false
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
      "end": 0,
      "qualityVerified": false,
      "macroVerified": false
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
  "end": 1514736671000,
  "qualityVerified": false,
  "macroVerified": false
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

### Parse errors

`/institutions/<lei>/filings/<period>/submissions/<submissionId>/parseErrors`

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

`/institutions/<lei>/filings/<period>/submissions/<submissionId>/edits`

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
      "message": "Your data has been analyzed for Syntactical and Validity Errors.",
      "description": "Your file has been analyzed and does not contain any Syntactical or Validity errors.",
      "qualityVerified": false,
      "macroVerified": false
  }
}
```


### Edits By Type

`POST` - Provides verification for quality or macro edits

_Specific to the `/institutions/<lei>/filings/<period>/submissions/<submissionId>/edits/<quality|macro>` endpoint._

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
    "code": 14,
    "message": "Your data is ready for submission.",
    "description": "Your financial institution has certified that the data is correct, but it has not been submitted yet."
  }
}
```

### Edit Details

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/<edit>`

`GET` - Returns details for an edit

```json
{
    "edit": "Q613",
    "rows": [
        {
            "id": "B90YWS6AFX2LGWOXJ1LDFXNYXTW0X1EMV6356JR1UAJ66",
            "fields": [
                {
                    "name": "name",
                    "value": 1
                }
            ]
        }
    ],
    "count": 1,
    "total": 1,
    "_links": {
        "href": "/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2018/submissions/5/edits/Q613{rel}",
        "self": "?page=1",
        "first": "?page=1",
        "prev": "?page=1",
        "next": "?page=1",
        "last": "?page=1"
    }
}
```

### Signature

`/institutions/<lei>/filings/<period>/submissions/<submissionId>/sign`

`GET`  - Returns a receipt

Example response:
```json
{
  "timestamp": 1476809530772,
  "receipt": "asd0f987134asdlfasdflk",
  "status": {
    "code": 15,
    "message": "Your submission has been accepted.",
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
    "code": 15,
    "message": "Your submission has been accepted.",
    "description": "Your financial institution has certified that the data is correct. This completes the HMDA filing process for this year."
  }
}
```

### Summary

`/institutions/<lei>/filings/<year>/submissions/<submissionId>/summary`

`GET` - Returns Summary

Example response:
```json
{
    "submission": {
        "id": {
            "lei": "B90YWS6AFX2LGWOXJ1LD",
            "period": "2018",
            "sequenceNumber": 3
        },
        "status": {
            "code": 15,
            "message": "Your submission has been accepted.",
            "description": "This completes your HMDA filing process for this year. If you need to upload a new HMDA file, the previously completed filing will not be overridden until all edits have been cleared and verified, and the new file has been submitted."
        },
        "start": 1544875860983,
        "end": 1544875869861,
        "fileName": "clean_file_100_rows_Bank0_syntax_validity.txt",
        "receipt": "B90YWS6AFX2LGWOXJ1LD-2018-3-1544875869861"
    },
    "ts": {
        "id": 1,
        "institutionName": "Bank0",
        "year": 2018,
        "quarter": 4,
        "contact": {
            "name": "Mr. Smug Pockets",
            "phone": "555-555-5555",
            "email": "pockets@ficus.com",
            "address": {
                "street": "1234 Hocus Potato Way",
                "city": "Tatertown",
                "state": "UT",
                "zipCode": "84096"
            }
        },
        "agency": 9,
        "totalLines": 100,
        "taxId": "01-0123456",
        "LEI": "B90YWS6AFX2LGWOXJ1LD"
    }
}
```

