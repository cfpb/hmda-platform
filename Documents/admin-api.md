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

* `/institutions/create|delete`
    * `GET` - Creates or deletes the instutition schema

    Example response, with HTTP code 202

    ```
    InstitutionSchemaDeleted()
    ```
