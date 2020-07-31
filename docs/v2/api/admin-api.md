# HMDA Platform Admin API

This documentation describes the admin HMDA Platform HTTP API

## Institutions

This endpoint deals with institution events. The identifier for each entity is the LEI (Legal Entity Identifier).

* `/institutions`

    * `POST` Creates a new institution if one doesn't exist already.

    Example body, in `JSON` format:

    ```json
    {
      "activityYear" : 2019,
      "LEI" : "54930084UKLVMY22DS16",
      "agency" : 1,
      "institutionType" : 17,
      "institutionId2017" : "12345",
      "taxId" : "99-00000000",
      "rssd" : 12345,
      "emailDomains" : ["bank0.com"],
      "respondent" : {
        "name" : "xvavjuitZa",
        "state" : "NC",
        "city" : "Raleigh"
      },
      "parent" : {
        "idRssd" : 1520162208,
        "name" : "Parent Name"
      },
      "assets" : 450,
      "otherLenderCode" : 1406639146,
      "topHolder" : {
        "idRssd" : 442825905,
        "name" : "TopHolder Name"
      },
      "hmdaFiler" : true,
      "quarterlyFiler": false
    }
    ```

   The response is the institution created, in `JSON` format, with an HTTP code of 201 (Created)
   
   The response code is `400` if the `LEI` already exists


    * `PUT` Updates an institution, if it already exists. 


     Example body, in `JSON` format:

        ```json
        {
          "activityYear" : 2019,
          "LEI" : "54930084UKLVMY22DS16",
          "agency" : 1,
          "institutionType" : 17,
          "institutionId2017" : "12345",
          "taxId" : "99-00000000",
          "rssd" : 12345,
          "emailDomains" : ["bank0.com"],
          "respondent" : {
            "name" : "xvavjuitZa",
            "state" : "NC",
            "city" : "Raleigh"
          },
          "parent" : {
            "idRssd" : 1520162208,
            "name" : "Parent Name"
          },
          "assets" : 450,
          "otherLenderCode" : 1406639146,
          "topHolder" : {
            "idRssd" : 442825905,
            "name" : "TopHolder Name"
          },
          "hmdaFiler" : true,
          "quarterlyFiler" : false
        }
        ```

       The response is the institution updated, in `JSON` format, with an HTTP code of 202 (Accepted)


    * `DELETE` Deletes an institution, if it already exists

    The body of the request is the institution to be deleted, in `JSON` format (see above for examples).

    Example response, in `JSON` format:

    ```json
    {
        "LEI":"54930084UKLVMY22DS16"
    }
    ```

* `/institutions/<id>`

    * `GET` Retrieves a list of institutions by its identifier, the LEI (Legal Entity Identifier)

    Example body, in `JSON` format:

        ```json
        [
          {
            "activityYear" : 2018,
            "LEI" : "54930084UKLVMY22DS16",
            "agency" : 1,
            "institutionType" : 17,
            "institutionId2017" : "12345",
            "taxId" : "99-00000000",
            "rssd" : 12345,
            "emailDomains" : ["bank0.com"],
            "respondent" : {
              "name" : "xvavjuitZa",
              "state" : "NC",
              "city" : "Raleigh"
            },
            "parent" : {
              "idRssd" : 1520162208,
              "name" : "Parent Name"
            },
            "assets" : 450,
            "otherLenderCode" : 1406639146,
            "topHolder" : {
              "idRssd" : 442825905,
              "name" : "TopHolder Name"
            },
            "hmdaFiler" : true,
            "quarterlyFiler" : false
          },
          {
            "activityYear" : 2019,
            "LEI" : "54930084UKLVMY22DS16",
            "agency" : 1,
            "institutionType" : 17,
            "institutionId2017" : "12345",
            "taxId" : "99-00000000",
            "rssd" : 12345,
            "emailDomains" : ["bank0.com"],
            "respondent" : {
              "name" : "xvavjuitZa",
              "state" : "NC",
              "city" : "Raleigh"
            },
            "parent" : {
              "idRssd" : 1520162208,
              "name" : "Parent Name"
            },
            "assets" : 450,
            "otherLenderCode" : 1406639146,
            "topHolder" : {
              "idRssd" : 442825905,
              "name" : "TopHolder Name"
            },
            "hmdaFiler" : true,
            "quarterlyFiler" : false
          }
        ]
        ```

* `/institutions/<id>/period/<period>`

    * `GET` Retrieves an institution by its identifier, the LEI (Legal Entity Identifier) and period (Year)

    The response is the institution in `JSON` format (see above for examples).

## Publications

This endpoint is to regenerate publications

* `/publish/<kafka-topic>/institutions/<lei>/filings/<year>/submissions/<submission-id>`

    * `POST` Creates a kafka topic to regenerate publications