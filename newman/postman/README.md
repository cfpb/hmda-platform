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
- `YEAR` The year you are filing for (default to be used for localhost: `2018`)
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
## CURL commands for Dev

```shell
CREATE INSTITUTION
curl --location --request POST 'http://localhost:8081/institutions' \
--header 'Content-Type: application/json' \
--data "@/dev/stdin"<<EOF
{
    "activityYear": 2022,
    "lei": "B90YWS6AFX2LGWOXJ1LD",
    "agency": 9,
    "institutionType": -1,
    "institutionId2017": "",
    "taxId": "01-0123456",
    "rssd": -1,
    "emailDomains": [
        "bank1.com"
    ],
    "respondent": {
        "name": "Bank 0",
        "state": "",
        "city": ""
    },
    "parent": {
        "idRssd": -1,
        "name": ""
    },
    "assets": -1,
    "otherLenderCode": -1,
    "topHolder": {
        "idRssd": -1,
        "name": ""
    },
    "hmdaFiler": false,
    "quarterlyFiler": true,
    "quarterlyFilerHasFiledQ1": false,
    "quarterlyFilerHasFiledQ2": false,
    "quarterlyFilerHasFiledQ3": false
}
EOF


START FILING YEAR
curl --location --request POST 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022' \
--header 'Content-Type: application/json' \
--data ''
{"filing":{"period":"2022","lei":"B90YWS6AFX2LGWOXJ1LD","status":{"code":2,"message":"in-progress"},"filingRequired":true,"start":1685544862557,"end":0},"submissions":[]}

CREATE SUBMISSION
curl --location --request POST 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions' \
--data ''
{"id":{"lei":"B90YWS6AFX2LGWOXJ1LD","period":{"year":2022,"quarter":null},"sequenceNumber":1},"status":{"code":1,"message":"No data has been uploaded yet.","description":"The filing period is open and available to accept HMDA data. Make sure your data is in a pipe-delimited text file."},"start":1685544982089,"end":0,"fileName":"","receipt":"","signerUsername":null}

NOTE: SEQUENCE Number is "1"

DOWNLOAD TEST FILE
curl -O https://raw.githubusercontent.com/cfpb/hmda-platform/master/data/2022/yearly/clear_test_files/bank0/Bank0_clean_5_rows.txt

UPLOAD
curl --location --request POST 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1' \
--header 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
--form 'file=@"/root/Bank0_clean_5_rows.txt"'
{"id":{"lei":"B90YWS6AFX2LGWOXJ1LD","period":{"year":2022,"quarter":null},"sequenceNumber":1},"status":{"code":3,"message":"Your file has been uploaded.","description":"Your data is ready to be analyzed."},"start":1685544982089,"end":0,"fileName":"","receipt":"","signerUsername":null}

VERIFY QUALITY 
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1/edits/quality' \
--header 'Content-Type: application/json' \
--data '{"verified": true}'
{"verified":true,"status":{"code":13,"message":"Your data has macro edits that need to be reviewed.","description":"Your file has been uploaded, but the filing process may not proceed until edits are verified or the file is corrected and re-uploaded."}}

VERIFY MACRO
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1/edits/macro' \
--header 'Content-Type: application/json' \
--data '{"verified": true}'
{"verified":true,"status":{"code":14,"message":"Your data is ready for submission.","description":"Your financial institution has certified that the data is correct, but it has not been submitted yet."}}


SIGN
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1/sign' \
--header 'Content-Type: application/json' \
--data '{"signed": true}'
{"email":"dev@dev.com","timestamp":1685546189816,"receipt":"B90YWS6AFX2LGWOXJ1LD-2022-1-1685546189816","status":{"code":15,"message":"Your submission has been accepted.","description":"This completes your HMDA filing process for this year. If you need to upload a new HMDA file, the previously completed filing will not be overridden until all edits have been cleared and verified, and the new file has been submitted."},"signerUsername":"dev"}

SHOW LATEST SUBMISSION
curl --location --request POST 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions' \
--data ''
{"id":{"lei":"B90YWS6AFX2LGWOXJ1LD","period":{"year":2022,"quarter":null},"sequenceNumber":2},"status":{"code":1,"message":"No data has been uploaded yet.","description":"The filing period is open and available to accept HMDA data. Make sure your data is in a pipe-delimited text file."},"start":1685547432464,"end":0,"fileName":"","receipt":"","signerUsername":null}
```

## HMDA API Documenation 

More information about HMDA's API can be found in the [HMDA API Docs](https://cfpb.github.io/hmda-platform/#hmda-api-documentation)
