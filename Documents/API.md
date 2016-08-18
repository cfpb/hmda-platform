# HMDA Platform API

## Public HTTP Endpoints

* `/`
    * `GET` - Root endpoint, with information about the HMDA Platform service. Used for health checks
    
    Example response, with HTTP code 200:
    
    ```json
    {
      "status": "OK",
      "service": "hmda-api",
      "time": "2016-06-17T13:54:10.725Z",
      "host": "localhost"
    }
    ```
    
    

* `/institutions`
    * `GET` - List of Financial Institutions
    
    Example response, with HTTP code 200: 
    
    ```json
    {
      "institutions": [
        {
          "id": "12345",
          "name": "First Bank",
          "status": "active"
        },
        {
          "id": "123456",
          "name": "Second Bank",
          "status": "inactive"
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
      "status": "active"
    },
      "filings": [
        {
          "id": "2017",
          "fid": "12345",
          "status": "not-started"
        },
        {
          "id": "2016",
          "fid": "12345",
          "status": "completed"
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
     "id": "2017",
     "fid": "12345",
     "status": "not-started"
   },
   "submissions": [
     {
       "id": 3,
       "submissionStatus": "created"
     },
     {
       "id": 2,
       "submissionStatus": "created"
     },
     {
       "id": 1,
       "submissionStatus": "created"
     }
   ]
   } 
    ```
    
* `/institutions/<institution>/filings/<period>/submissions`
    
    * `POST` - Create a new submission
    
    Example response, with HTTP code 201:
    
    ```json
    {
        "id": 4,
        "submissionStatus": "created"
    }
    ```
    
* `/institutions/<institution>/filings/<period>/submissions/<submissionId>`
    * `POST` - Upload HMDA data to submission
    
    
* `/institutions/<institution>/summary`
    * `GET` - Summary for Financial Institution, including filing information
    
    Example response, with HTTP code 200:
    
    ```json
    {
      "id": "12345",
      "name": "First Bank",
      "filings": [
        {
          "period": "2017",
          "fid": "12345",
          "status": "not-started"
        },
        {
          "period": "2016",
          "fid": "12345",
          "status": "completed"
        }
      ]
    }
    ```

## Authorization
Each endpoint (excluding `/`) is protected by three authorization requirements.

* Requests must include the `CFPB-HMDA-Username` header.
  * Its value should be the username of the user making the request.
* Requests must include the `CFPB-HMDA-Institutions` header.
  * This header will contain the comma-separated list of institution IDs
    that the user is authorized to view.
* For requests to institution-specific paths, such as `/institutions/<institution>`
  and `/institutions/<institution>/summary` (any endpoint in the `/institutions` namespace
  except `/institutions`), the institution ID requested must match one of the IDs in the
  `CFPB-HMDA-Institutions` header.
