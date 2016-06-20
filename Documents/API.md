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
      "id": "12345",
      "name": "First Bank",
      "status": "active"
    }
    ```
    
* `/institutions/<institution>/filings`
    * `GET` - List of filings for Financial Institution
    
    Example response, with HTTP code 200:
    
    ```json
    {
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
    
    
* `/institutions/<institution>/filings/<period>`
    * `GET` - Details for a filing
    
    Example response, with HTTP code 200:
    
    ```json
    {
      "period": "2017",
      "fid": "12345",
      "status": "not-started"
    }
    ```
    
* `/institutions/<institution>/filings/<period>/submissions`

    * `GET` - List of submissions for a financial institution, per filing period
    
    Example response, with HTTP code 200:
    
    ```json
    {
      submissions: [
        {  
          id: 3,
          submissionStatus: "created"
        },
        {
          id: 2,
          submissionStatus: "created"
        },
        {
          id: 1,
          submissionStatus: "created"
        }
      ]
    }
    ```
    
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