# HMDA Platform Admin API

This API is for internal use only. The following endpoints are available

* `/`
    * `GET` - Root endpoint, with information about the HMDA Platform admin service. Used for health checks

    Example response, with HTTP code 200:

    ```json
    {
      "status": "OK",
      "service": "hmda-admin-api",
      "time": "2016-06-17T13:54:10.725Z",
      "host": "localhost"
    }
    ```

* `/institutions`
    * `POST` - Creates new financial institution

    Accepts data in `json` format. For example:

    ```json
    {
      "id": "123",
      "agency": "CFPB",
      "activityYear": "2017",
      "institutionType": "bank",
      "cra": false,
      "externalIds": [{
        "id": "bank-id",
        "idType": "fdic-certificate-number"
      }],
      "emailDomains": [
        "email1",
        "email2"
      ],
      "respondent": {
        "externalId": {
            "id": "bank-id",
            "idType": "fdic-certificate-number"
        },
        "name": "bank 0",
        "state": "VA",
        "city": "City Name",
        "fipsStateNumber": "2"
      },
      "hmdaFilerFlag": true,
      "parent": {
        "respondentId": "12-3",
        "idRssd": 3,
        "name": "parent name",
        "city": "parent city",
        "state": "VA"
      },
      "assets": 123,
      "otherLenderCode": 0,
      "topHolder": {
        "idRssd": 4,
        "name": "top holder name",
        "city": "top holder city",
        "state": "VA",
        "country": "USA"
      }
    }
    ```

    Response is the same object inserted, with code 201


    * `PUT` - Modifies existing financial institution. Can modify any field except `id` (used for finding the institution to be updated)

    Accepts data in `json` format. For example:

    ```json
    {
      "id": "123",
      "agency": "CFPB",
      "activityYear": "2017",
      "institutionType": "bank",
      "cra": false,
      "externalIds": [{
        "id": "bank-id",
        "idType": "fdic-certificate-number"
      }],
      "emailDomains": [
        "email1",
        "email2"
      ],
      "respondent": {
        "externalId": {
            "id": "bank-id",
            "idType": "fdic-certificate-number"
        },
        "name": "bank 0",
        "state": "VA",
        "city": "City Name",
        "fipsStateNumber": "2"
      },
      "hmdaFilerFlag": true,
      "parent": {
        "respondentId": "12-3",
        "idRssd": 3,
        "name": "parent name",
        "city": "parent city",
        "state": "VA"
      },
      "assets": 123,
      "otherLenderCode": 0,
      "topHolder": {
        "idRssd": 4,
        "name": "top holder name",
        "city": "top holder city",
        "state": "VA",
        "country": "USA"
      }
    }
    ```

    Response is the same object that has been modified, with code 202

* `/institutions/<institutionID>`

    * `GET`

    Retrieves the details of an institution.

    Example Response, in `JSON` format:

    ```json
        {
          "id": "123",
          "agency": "CFPB",
          "activityYear": "2017",
          "institutionType": "bank",
          "cra": false,
          "externalIds": [{
            "id": "bank-id",
            "idType": "fdic-certificate-number"
          }],
          "emailDomains": [
            "email1",
            "email2"
          ],
          "respondent": {
            "externalId": {
                "id": "bank-id",
                "idType": "fdic-certificate-number"
            },
            "name": "bank 0",
            "state": "VA",
            "city": "City Name",
            "fipsStateNumber": "2"
          },
          "hmdaFilerFlag": true,
          "parent": {
            "respondentId": "12-3",
            "idRssd": 3,
            "name": "parent name",
            "city": "parent city",
            "state": "VA"
          },
          "assets": 123,
          "otherLenderCode": 0,
          "topHolder": {
            "idRssd": 4,
            "name": "top holder name",
            "city": "top holder city",
            "state": "VA",
            "country": "USA"
      }
    }
    ```

* `/apor`

    * `POST`

    Creates a new APOR value

    Example request, in `JSON` format

    ```json
        {
        	"rateType": "FixedRate",
        	"newApor": {
        		"rateDate": "2018-01-01",
        		"values": [
        			1.00,
        			3.26,
        			3.39,
        			3.39,
        			3.59,
        			3.59,
        			3.68,
        			3.68,
        			3.74,
        			3.74,
        			3.74,
        			3.74,
        			3.51,
        			3.51,
        			3.51,
        			3.51,
        			3.51,
        			3.51,
        			3.51,
        			3.51,
        			3.51,
        			3.51,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03,
        			4.03
        		]
        	}
        }
        ```

    If a value for that date already exists, this new entry will not be created (see below for endpoint to update values).

    The response is the new APOR in `JSON` format (see endpoint below for exact data structure).

    * `PUT`

    Updates APOR value

    Example request, in `JSON` format:

    ```json
    {
    	"rateType": "FixedRate",
    	"newApor": {
    		"rateDate": "2018-01-01",
    		"values": [
    			1.00,
    			3.26,
    			3.39,
    			3.39,
    			3.59,
    			3.59,
    			3.68,
    			3.68,
    			3.74,
    			3.74,
    			3.74,
    			3.74,
    			3.51,
    			3.51,
    			3.51,
    			3.51,
    			3.51,
    			3.51,
    			3.51,
    			3.51,
    			3.51,
    			3.51,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03,
    			4.03
    		]
    	}
    }
    ```

    The response is the updated APOR, in `JSON` format (see endpoint below for exact data structure).


* `/apor/[fixed|variable]/<year>/<month>/<day>`

    * `GET`
    Retrieves the APOR values for a specific date, filtered by rate type (fixed or variable)

    Example Response, in `JSON` format:

    ```json
    {
      "rateDate": "2018-01-01",
      "values": [
          3.25,
          3.26,
          3.39,
          3.39,
          3.59,
          3.59,
          3.68,
          3.68,
          3.74,
          3.74,
          3.74,
          3.74,
          3.51,
          3.51,
          3.51,
          3.51,
          3.51,
          3.51,
          3.51,
          3.51,
          3.51,
          3.51,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03,
          4.03
      ]
    }
    ```