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

    * `DELETE` - Deletes existing financial institution.

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


    Response is the same object that has been deleted, with code 202

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

    The response is the new APOR in `JSON` format (see endpoint below for exact data structure).

    * `PUT`

    Updates APOR values, for a given rate type and rate date.

    If an APOR record already exists in the system with the given rate type and date, the new record will effectively overwrite the existing one.
    Both will exist in the system, but the most recently added one will be used in rate spread calculations

    The `values` array should contain exactly 50 entries.

    Example request, in `JSON` format:

    ```json
    {
    	"rateType": "FixedRate",
    	"newApor": {
    		"rateDate": "2018-01-01",
    		"values": [
    			1.00, 3.26, 3.39, 3.39, 3.59,
    			3.59, 3.68, 3.68, 3.74, 3.74,
    			3.74, 3.74, 3.51, 3.51, 3.51,
    			3.51, 3.51, 3.51, 3.51, 3.51,
    			3.51, 3.51, 4.03, 4.03, 4.03,
    			4.03, 4.03, 4.03, 4.03, 4.03,
    			4.03, 4.03, 4.03, 4.03, 4.03,
    			4.03, 4.03, 4.03, 4.03, 4.03,
    			4.03, 4.03, 4.03, 4.03, 4.03,
    			4.03, 4.03, 4.03, 4.03, 4.03
    		]
    	}
    }
    ```

    The response is the updated APOR, in `JSON` format (see endpoint below for exact data structure).


* `/apor/[fixed|variable]/<year>/<month>/<day>`

    * `GET`
    Retrieves the APOR values for a specific date, filtered by rate type (fixed or variable).
    Returns 404 if the record does not exist.

    Example Response, in `JSON` format:

    ```json
    {
      "rateDate": "2018-01-01",
      "values": [
          3.25, 3.26, 3.39, 3.39, 3.59,
          3.59, 3.68, 3.68, 3.74, 3.74,
          3.74, 3.74, 3.51, 3.51, 3.51,
          3.51, 3.51, 3.51, 3.51, 3.51,
          3.51, 3.51, 4.03, 4.03, 4.03,
          4.03, 4.03, 4.03, 4.03, 4.03,
          4.03, 4.03, 4.03, 4.03, 4.03,
          4.03, 4.03, 4.03, 4.03, 4.03,
          4.03, 4.03, 4.03, 4.03, 4.03,
          4.03, 4.03, 4.03, 4.03, 4.03
      ]
    }
    ```

* `/disclosure/<institution RSSD>/<year>/<submission ID>`

    * `POST`

    Forces the backend to generate and publish the disclosure reports for a given submission.
    Saves all reports to the s3 bucket that is configured in the AWS environment variables.

    If the submission has a `Signed` status, returns a `200 OK` response.
    If the submission is not Signed, returns `400 Bad Request` response and does not generate reports.

* `/aggregate/2017`

    * `POST`

    Generates all Aggregate and National Aggregate reports for all 2017 data that has been reported.
    Saves all reports to the s3 bucket that is configured in the AWS environment variables.

    Returns a `200 OK` response.


* `/filers`

    * `POST`

    Creates a new HMDA filer
    Returns HTTP code 201 when successful

    Example payload, in `JSON` format:

    ```json
    {
        "institutionId": "0",
        "name": "bank-0 National Association",
        "period": "2017",
        "respondentId": "Bank0_RID"
    }
    ```

    The response is the same filer that has been inserted.

    * `GET`

    Retrieves list of HMDA filers.
    Example response with HTTP code 200, in `JSON` format:

    ```json
    {
        "institutions": [
            {
                "institutionId": "0",
                "name": "bank-0 National Association",
                "period": "2017",
                "respondentId": "Bank0_RID"
            },
            {
                "institutionId": "1",
                "name": "Bak 1",
                "period": "2016",
                "respondentId": "Bank1_RID"
             }
        ]
    }
    ```

    * `DELETE`

    Deletes HMDA filer with matching institutionId. name, period, and respondentId fields in payload are ignored.
    Returns HTTP code 202 when successful

    Example payload, in `JSON` format:

    ```json
    {
        "institutionId": "0",
        "name": "bank-0 National Association",
        "period": "2017",
        "respondentId": "Bank0_RID"
    }
    ```

    The response is the same filer that has been deleted.

