# HMDA Platform API

## Public HTTP Endpoints

* `/`
    * `GET` - Root endpoint, with information about the HMDA Platform service. Used for health checks

    Example response, with HTTP code 200:

    ```json
    {
      "status": "OK",
      "service": "hmda-filing-api",
      "time": "2016-06-17T13:54:10.725Z",
      "host": "localhost"
    }
    ```

All endpoints in the `/institutions` namespace require two headers (see "Authorization" section below for more detail):
* `CFPB-HMDA-Username`, containing a string
* `CFPB-HMDA-Institutions`, containing a list of integers


* `/institutions`
    * `GET` - List of Financial Institutions

    Example response, with HTTP code 200:

    ```json
    {
      "institutions": [
        {
          "id": "12345",
          "name": "First Bank",
          "status": {
            "code": 1,
            "message": "active"
          }
        },
        {
          "id": "123456",
          "name": "Second Bank",
          "status": {
            "code": 0,
            "message": "inactive"
          }
        }
      ]
    }
    ```

* `/institutions/<institution>`
    * `GET` - Details for Financial Institution

    Example response, with HTTP code 200:

    ```json
    {
      "institution": {
      "id": "12345",
      "name": "First Bank",
      "status": {
        "code": 0,
        "message": "inactive"
      }
    },
      "filings": [
        {
          "period": "2017",
          "institutionId": "12345",
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


* `/institutions/<institution>/filings/<period>`
  * `GET` - Details for a filing

  Example response, with HTTP code 200:

```json
{
"filing": {
  "period": "2017",
  "institutionId": "12345",
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

* `/institutions/<institution>/filings/<period>/submissions`

    * `POST` - Create a new submission

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

* `/institutions/<institution>/filings/<period>/submissions/latest`

    * `GET` - The latest submission for some institution and period

     Example response, with HTTP code 200:

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


* `/institutions/<institution>/filings/<period>/submissions/<submissionId>`
    * `POST` - Upload HMDA data to submission
    
    Example response, with HTTP code 200:
    
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

* `/institutions/<institution>/filings/<period>/submissions/<submissionId>/edits`
    * `GET`  - List of all edits for a given submission, grouped by edit type

    Example response, with HTTP code 200:

    ```json
    {
      "syntactical": {
        "edits": [
          {
            "edit": "S025",
            "description": "Description of S025",
            "ts": true,
            "lars": [
              {
                "lar": {"loanId": "s1"}
              },
              {
                "lar": {"loanId": "s2"}
              },
              {
                "lar": {"loanId": "s3"}
              }
            ]
          },
          {
            "edit": "S010",
            "description": "Description of S010",
            "ts": false,
            "lars": [
              {
                "lar": {"loanId": "s4"}
              },
              {
                "lar": {"loanId": "s5"}
              }
            ]
          }
        ]
      },
      "validity": {},
      "quality": {},
      "macro": [
        {
            "edits": [
                {
                   "edit": "Q007",
                   "description: "Description of Q007",
                   "justifications": [
                     {
                       "value": "don't worry",
                       "selected": false
                     },
                     {
                       "value": "be happy",
                       "selected": false
                     }
                   ]
                }
            ]
        }
      ]
    }
    ```

* `/institutions/<institution>/filings/<period>/submissions/<submissionId>/edits/<syntactical|validity|quality|macro>`
    * `GET`  - List of edits of a specific type, for a given submission

    Example response, with HTTP code 200:

```json
{
  "edits": [
    {
      "edit": "V555",
      "description: "Description of V555",
      "ts": false,
      "lars": [
        {
          "lar": {
            "loanId": "4977566612"
          }
        }
      ]
    },
    {
      "edit": "V550",
      "description: "Description of V550",
      "ts": false,
      "lars": [
        {
          "lar": {
            "loanId": "4977566612"
          }
        }
      ]
    }
  ]
}
```

* `/institutions/<institution>/filings/<period>/submissions/<submissionId>/irs`
*NOTE:*  This is a mocked, static endpoint.

    * `GET`  - Institution Register Summary

    Example response, with HTTP code 200:

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
  "timestamp": 0,
  "receipt": "",
  "status": {
       "code": 10,
       "message": "IRS report generated"
     }
}
```

   * `POST`  - Verify the IRS
       Example body:
```
{
 "verified": true
}
```
   Example response, with HTTP code 200:
```
{
  "timestamp": 1476809530772,
  "receipt": asd0f987134asdlfasdflk,
  "status": {
      "code": 11,
      "message": "IRS report verified"
    }
}
```

* `/institutions/<institution>/filings/<period>/submissions/<submissionId>/sign`
*NOTE:*  This is a mocked, static endpoint.
    * `GET`  - Returns a receipt
    Example response, with HTTP code 200:
```
{
  "timestamp": 1476809530772,
  "receipt": asd0f987134asdlfasdflk,
  "status": {
      "code": 11,
      "message": "IRS report verified"
    }
}
```

   * `POST`  - Sign the submission
    Example body:
```
{
  "signed": true
}
```
    Example response, with HTTP code 200:
```
{
  "timestamp": 1476809530772,
  "receipt": asd0f987134asdlfasdflk,
  "status": {
      "code": 12,
      "message": "signed"
    }
}
```

* `/institutions/<institution>/filings/<period>/submissions/<submissionId>/summary`
*NOTE:*  This is a mocked, static endpoint.
    * `GET`  - Returns a submission summary
    Example response, with HTTP code 200:
```
{
  "respondent": {
    "name": "Bank",
    "id": "1234567890",
    "taxId": "0987654321",
    "agency": "CFPB",
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

* `/institutions/<institution>/filings/<period>/submissions/<submissionId>/parseErrors`

    * `GET` - Returns all parsing errors for a submission

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
      ]
    }
    ```

## Authorization
Each endpoint that starts with `/institutions` is protected by three authorization requirements.

* Requests must include the `CFPB-HMDA-Username` header.
  * Its value should be the username of the user making the request.
* Requests must include the `CFPB-HMDA-Institutions` header.
  * This header will contain the comma-separated list of institution IDs
    that the user is authorized to view.
* For requests to institution-specific paths, such as `/institutions/<institution>`
  and `/institutions/<institution>/summary` (any endpoint except `/institutions`),
  the institution ID requested must match one of the IDs in the `CFPB-HMDA-Institutions`
  header.
