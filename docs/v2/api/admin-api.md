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
      "agency" : "1",
      "institutionType" : "17",
      "institutionId2017" : "12345",
      "taxId" : "99-00000000",
      "rssd" : "Pb",
      "emailDomains" : ["email@bank0.com"],
      "respondent" : {
        "name" : "xvavjuitZa",
        "state" : "NC",
        "city" : "Raleigh"
      },
      "parent" : {
        "idRssd" : "1520162208",
        "name" : "Parent Name"
      },
      "assets" : "450",
      "otherLenderCode" : "1406639146",
      "topHolder" : {
        "idRssd" : "442825905",
        "name" : "TopHolder Name"
      },
      "hmdaFiler" : true
    }
    ```

   The response is the institution created, in `JSON` format, with an HTTP code of 201 (Created)


    * `PUT` Updates an institution, if it already exists. 


     Example body, in `JSON` format:

        ```json
        {
          "activityYear" : 2019,
          "LEI" : "54930084UKLVMY22DS16",
          "agency" : "1",
          "institutionType" : "17",
          "institutionId2017" : "12345",
          "taxId" : "99-00000000",
          "rssd" : "Pb",
          "emailDomains" : ["email@bank0.com"],
          "respondent" : {
            "name" : "xvavjuitZa",
            "state" : "NC",
            "city" : "Raleigh"
          },
          "parent" : {
            "idRssd" : "1520162208",
            "name" : "Parent Name"
          },
          "assets" : "450",
          "otherLenderCode" : "1406639146",
          "topHolder" : {
            "idRssd" : "442825905",
            "name" : "TopHolder Name"
          },
          "hmdaFiler" : true
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

    * `GET` Retrieves an institution by its identifier, the LEI (Legal Entity Identifier)

    The response is the institution in `JSON` format (see above for examples).