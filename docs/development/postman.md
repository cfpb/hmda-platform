# HMDA Postman Collection

The HMDA [Postman](https://www.postman.com/) collection has everything you need to file using HMDA Public API. There are 2 files you will need to import into Postman.

1. `HMDA_API_Filing.postman_collection.json` has all the requests. You shouldn't need to modify it.
2. `HMDA_ENV.postman_environment.json` has all the enviorment variables need to run the requests. You will need to edit this file either before import or in Postman.

## Variables

The variables you will need to set in `HMDA_ENV.postman_environment` are below. If you are running the platform locally, we've provided default values that you can input. The default values can be easily changed in the postman collection once imported

- `URL_ADMIN` (default to be used for localhost: `http://localhost:8081`)
- `URL_FILING` (default to be used for localhost: `http://localhost:8080`)
- `HMDA_TOKEN_URL` (not needed for localhost)
- `HMDA_USERNAME` Your username (not needed for localhost)
- `HMDA_PASSWORD` Your password (not needed for localhost)
- `LEI` The LEI you are filing for (default to be used for localhost: `B90YWS6AFX2LGWOXJ1LD`)
- `YEAR` The year you are filing for (default to be used for localhost: `2022`)
- `FILE` Full path to file for upload 

### Notes about the fields
 
- Information about [HMDA authorization](https://cfpb.github.io/hmda-platform/#hmda-filing-api-authorization)
- The variables `TOKEN` and `SEQUENCE_NUMBER` are set to be updated automatically by Postman.
    - `TOKEN` is set by the *Get Token* request
    - `SEQUENCE_NUMBER` is set by the *Create a submission* request
- `EDIT_NUMBER` can be used for sending a request for a single edit details

## Upload Delay

When submitting an upload request, please allow a time delay before trying to hit any of the edits requests. If you try any of the edit requests, you will get an error indicating that the

## Newman

If you have [Newman](https://github.com/postmanlabs/newman) installed, you can edit the environment file and run the collection via the command line using:

```shell
newman run HMDA_API_Filing.postman_collection.json -e HMDA_ENV.postman_environment.json --delay-request 5000
```
