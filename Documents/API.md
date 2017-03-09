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
- [`/institutions/<institution>/filings/<period>/submissions/<submissionId>/irs`](#irs)
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
        "message": "created"
      },
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
        "message": "created"
      },
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
        "message": "created"
      },
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
    "message": "created"
  },
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
    "message": "created"
  },
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
    "message": "uploaded"
  }
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
    "message": "Submission 4848484 not available for upload"
  }
}
```

### Parse errors

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/parseErrors`

`GET` - Returns all parsing errors for a submission

This endpoint is paginated.

| Query parameter | Description |
| --------------- | ----------- |
| page | Integer. If blank, will default to page 1. Page size is 20 lines of errors. |


The response contains 3 fields of pagination metadata:

 - `total`: total number of parser errors for this file
 - `count`: number of errors returned on this page. Full page contains errors from 20 lines of the HMDA file.
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

`GET`  - Returns a list of all edits for a given submission

By default, results are grouped by edit type.

| Query parameter | Description |
| --------------- | ----------- |
| sortBy | `row` to group edits by row, rather than the default edit type. |
| format | `csv` to return edits in CSV format, rather than the default by edit type, for use in spreadsheet software. |

Example responses:

Default Sorting:

```json
{
  "syntactical": {
    "edits": [
      {
        "edit": "S020",
        "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
        "rows": [
          {
            "row": {
              "rowId": "Transmittal Sheet"
            },
            "fields": {
              "Agency Code": 10
            }
          },
          {
            "row": {
              "rowId": "8299422144"
            },
            "fields": {
              "Agency Code": 10
            }
          },
          {
            "row": {
              "rowId": "2185751599"
            },
            "fields": {
              "Agency Code": 10
            }
          }
        ]
      },
      {
        "edit": "S010",
        "description": "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR).",
        "rows": [
          {
            "row": {
              "rowId": "2185751599"
            },
            "fields": {
              "Record Identifier": 1
            }
          }
        ]
      }
    ]
  },
  "validity": {
    "edits": [
      {
        "edit": "V555",
        "description": "If loan purpose = 1 or 3, then lien status must = 1, 2, or 4.",
        "rows": [
          {
            "row": {
              "rowId": "4977566612"
            },
            "fields": {
              "Loan Purpose": 3,
              "Lien Status": 8
            }
          }
        ]
      },
      {
        "edit": "V560",
        "description": "If action taken type = 1-5, 7 or 8, then lien status must = 1, 2, or 3.",
        "rows": [
          {
            "row": {
              "rowId": "4977566612"
            },
            "fields": {
              "Type of Action Taken": 2,
              "Lien Status": 8
            }
          }
        ]
      }
    ]
  },
  "quality": {
    "verified": false,
    "edits": []
  },
  "macro": {
    "edits": [
      {
        "edit": "Q008",
        "justifications": [
          {
            "id": 1,
            "value": "Applicants decided not to proceed with the loan.",
            "verified": false
          },
          {
            "id": 2,
            "value": "There were a large number of applications, but few loans were closed",
            "verified": false
          },
          {
            "id": 3,
            "value": "Loan activity for this filing year consisted mainly of purchased loans.",
            "verified": false
          }
        ]
      }
    ]
  }
}
```

Formatted as CSV, `?format=csv`:

```csv
editType, editId, loanId
syntactical, S025, Transmittal Sheet
syntactical, S025, s1
syntactical, S025, s2
syntactical, S025, s3
syntactical, S010, s4
syntactical, S010, s5
macro, Q007
```

Sorted by row, `?sortBy=row`:

_Macro edits remain their own object because they aren't row based._

```json
{
  "rows": [
    {
      "rowId": "Transmittal Sheet",
      "edits": [
        {
          "editId": "S020",
          "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
          "fields": {
            "Agency Code": 10
          }
        }
      ]
    },
    {
      "rowId": "8299422144",
      "edits": [
        {
          "editId": "S020",
          "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
          "fields": {
            "Agency Code": 10
          }
        }
      ]
    },
    {
      "rowId": "4977566612",
      "edits": [
        {
          "editId": "V555",
          "description": "If loan purpose = 1 or 3, then lien status must = 1, 2, or 4.",
          "fields": {
            "Loan Purpose": 1,
            "Lien Status": 0
          }
        },
        {
          "editId": "V560",
          "description": "If action taken type = 1-5, 7 or 8, then lien status must = 1, 2, or 3.",
          "fields": {
            "Action Taken Type": 3,
            "Lien Status": 0
          }
        }
      ]
    }
  ],
  "macro": {
    "edits": [
      {
        "edit": "Q008",
        "justifications": [
          {
            "id": 1,
            "value": "Applicants decided not to proceed with the loan.",
            "verified": false
          },
          {
            "id": 2,
            "value": "There were a large number of applications, but few loans were closed",
            "verified": false
          },
          {
            "id": 3,
            "value": "Loan activity for this filing year consisted mainly of purchased loans.",
            "verified": false
          }
        ]
      }
    ]
  }
}
```

### Edits by type

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/<syntactical|validity|quality|macro>`

`GET` - Returns a list of edits of a specific type, for a given submission

By default, results are grouped by named edit.

| Query parameter | Description |
| --------------- | ----------- |
| sortBy | `row` to group edits by row, rather than the default edit type. |
| format | `csv` to return edits in CSV format, rather than the default by edit type, for use in spreadsheet software. |

Example response:

```json
{
  "edits": [
    {
      "edit": "S020",
      "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
      "rows": [
        {
          "row": {
            "rowId": "Transmittal Sheet"
          },
          "fields": {
            "Agency Code": 1
          }
        },
        {
          "row": {
            "rowId": "8299422144"
          },
          "fields": {
            "Agency Code": 1
          }
        }
      ]
    },
    {
      "edit": "S010",
      "description": "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR).",
      "rows": [
        {
          "row": {
            "rowId": "2185751599"
          },
          "fields": {
            "Record Identifier": 1
          }
        }
      ]
    }
  ]
}
```

Formatted as CSV:
```
editType, editId, loanId
validity, V555, 4977566612
validity, V550, 4977566612
```

Sorted by row, `?sortBy=row`:

_Macro edits remain their own object because they aren't row based._

```json
{
  "rows": [
    {
      "rowId": "Transmittal Sheet",
      "edits": [
        {
          "editId": "S020",
          "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
          "fields": {
            "Agency Code": 4
          }
        }
      ]
    },
    {
      "rowId": "8299422144",
      "edits": [
        {
          "editId": "S020",
          "description": "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.",
          "fields": {
            "Agency Code": 11
          }
        }
      ]
    },
  ],
  "macro": {
    "edits": []
  }
}
```

`POST` - Provides verification for quality edits

_Specific to the `/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/quality` endpoint._

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
    "message": "validated with errors"
  }
}
```

`POST` - Provides justification for macro edits

_Specific to the `/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits/macro` endpoint._

Example payload, in `JSON` format:

```json
{
  "edit": "Q023",
  "justification": {
  "id": 1,
  "value": "Most of the loan activity are in areas outside of an MSA/MD",
  "verified": true
  }
}
```

Example response:

```json
{
  "edit": "Q023",
  "justifications": [
    {
      "id": 1,
      "value": "Most of the loan activity are in areas outside of an MSA/MD",
      "verified": true
    },
    {
      "id": 2,
      "value": "Most branches or the main branch is located outside of an MSA/MD, therefore many loans are located outside of an MSA/MD.",
      "verified": false
    },
    {
      "id": 3,
      "value": "Acquired or merged with an entity whose loan activity are outside of an MSA/MD.",
      "verified": false
    },
    {
      "id": 4,
      "value": "Purchased loans are located in areas outside of an MSA/MD.",
      "verified": false
    }
  ]
}
```

### IRS

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs`

_NOTE: This is currently a mocked, static endpoint._

`GET` - Returns the Institution Register Summary

Example response:

```json
{
  "msas": [
    {
      "id": "123",
      "name": "MSA 123",
      "totalLARS": 4,
      "totalAmount": 123,
      "conv": 4,
      "FHA": 0,
      "VA": 0,
      "FSA": 0,
      "1to4Family": 4,
      "MFD": 0,
      "multiFamily": 0,
      "homePurchase": 0,
      "homeImprovement": 0,
      "refinance": 4
    },
    {
      "id": "456",
      "name": "MSA 456",
      "totalLARS": 5,
      "totalAmount": 456,
      "conv": 5,
      "FHA": 0,
      "VA": 0,
      "FSA": 0,
      "1to4Family": 5,
      "MFD": 0,
      "multiFamily": 0,
      "homePurchase": 0,
      "homeImprovement": 0,
      "refinance": 5
    }
  ],
  "status": {
       "code": 10,
       "message": "IRS report generated"
     }
}
```

### Signature

`/institutions/<institutionId>/filings/<period>/submissions/<submissionId>/sign`

_NOTE: This is currently a mocked, static endpoint._

`GET`  - Returns a receipt

Example response:
```json
{
  "timestamp": 1476809530772,
  "receipt": "asd0f987134asdlfasdflk",
  "status": {
      "code": 11,
      "message": "signed"
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
    "code": 12,
    "message": "signed"
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
