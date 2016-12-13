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
      "name": "Bank 2",
      "cra": false,
      "agency": "fdic",
      "externalIds": [{
        "id": "bank-id",
        "idType": "fdic-certificate-number"
      }],
      "id": "2",
      "hasParent": false,
      "institutionType": "bank"
    }
    ```
    
    Response is the same object inserted, with code 201
    
    
    * `PUT` - Modifies existing financial institution. Can modify any field except `id` (used for finding the institution to be updated)
    
    Accepts data in `json` format. For example: 
    
    ```json
    {
      "name": "Bank 2",
      "cra": false,
      "agency": "fdic",
      "externalIds": [{
        "id": "bank-id",
        "idType": "fdic-certificate-number"
      }],
      "id": "2",
      "hasParent": false,
      "institutionType": "bank"
    }
    ```
    
    Response is the same object that has been modified, with code 202