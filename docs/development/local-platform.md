- Install cqlsh
```
brew install cassandra
```
- Create keyspaces and tables
```
cqlsh -f hmda-sql-doc/dev-cassandra.cql

cqlsh                                  
WARNING: cqlsh was built against 5.0.2, but this server is 3.11.3.  All features may not work!
Connected to Test Cluster at 127.0.0.1:9042
[cqlsh 6.2.0 | Cassandra 3.11.3 | CQL spec 3.4.4 | Native protocol v4]
Use HELP for help.
cqlsh> SELECT * FROM system_schema.keyspaces;                                                                                                                                             
 keyspace_name      | durable_writes | replication
--------------------+----------------+-------------------------------------------------------------------------------------
     hmda2_snapshot |           True | {'class': 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '1'}
      hmda2_journal |           True | {'class': 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '1'}
        system_auth |           True | {'class': 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '1'}
      system_schema |           True |                             {'class': 'org.apache.cassandra.locator.LocalStrategy'}
 system_distributed |           True | {'class': 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '3'}
             system |           True |                             {'class': 'org.apache.cassandra.locator.LocalStrategy'}
      system_traces |           True | {'class': 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '2'}

(7 rows)
``` 
- Filing health-check
```
curl --location 'http://localhost:8080/' \
--header 'Content-Type: application/json'
```
- Admin health-check
```
curl --location 'http://localhost:8081/' \
--header 'Content-Type: application/json'
```
- Create test institution - bank0
```
curl --location --request PUT 'http://localhost:8081/institutions' \
--header 'Content-Type: application/json' \
--data '{
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
}'
```
- Get test institution - bank0
```
curl --location 'http://localhost:8081/institutions/B90YWS6AFX2LGWOXJ1LD/year/2022' \
--header 'Content-Type: application/json'
```
- Delete test institution - bank0
```
curl --location --request DELETE 'http://localhost:8081/institutions' \
--header 'Content-Type: application/json' \
--data '{
    "activityYear": 2022,
    "lei": "B90YWS6AFX2LGWOXJ1LD",
    "agency": 9,
    "institutionType": -1,
    "institutionId2017": "",
    "taxId": "01-0123456",
    "rssd": -1,
    "emailDomains": [
        "bank1.com", "cfpb.gov", "cfpb.gov", "cfpb.gov", "bank1.com", "test.com"
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
    "quarterlyFilerHasFiledQ3": false,
    "notes" : "add domains"
}'
```
- Show latest submission
```
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/latest' \
--header 'Content-Type: application/json'
```
- Start filing year
```
curl --location --request POST 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022' \
--header 'Content-Type: application/json' \
--data ''
```
- Create a submission
Note: Sequence number is unique, it cannot be reused
```
curl --location --request POST 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions' \
--data ''

{
    "id": {
        "lei": "B90YWS6AFX2LGWOXJ1LD",
        "period": {
            "year": 2022,
            "quarter": null
        },
        "sequenceNumber": 1
    },
    "status": {
        "code": 1,
        "message": "No data has been uploaded yet.",
        "description": "The filing period is open and available to accept HMDA data. Make sure your data is in a pipe-delimited text file."
    },
    "start": 1738613166367,
    "end": 0,
    "fileName": "",
    "receipt": "",
    "signerUsername": null
}
```
- Upload file
```
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1' \
--header 'Content-Type: application/json' \
--form 'file=@"/Users/testuser/hmda-platform/data/2022/yearly/clear_test_files/bank0/Bank0_clean_5_rows.txt"'

{
    "id": {
        "lei": "B90YWS6AFX2LGWOXJ1LD",
        "period": {
            "year": 2022,
            "quarter": null
        },
        "sequenceNumber": 1
    },
    "status": {
        "code": 3,
        "message": "Your file has been uploaded.",
        "description": "Your data is ready to be analyzed."
    },
    "start": 1738613166367,
    "end": 0,
    "fileName": "",
    "receipt": "",
    "signerUsername": null
}
```
- Verify Quality Edits
```
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1/edits/quality' \
--header 'Content-Type: application/json' \
--data '{"verified": true}'

{
    "verified": true,
    "status": {
        "code": 13,
        "message": "Your data has macro edits that need to be reviewed.",
        "description": "Your file has been uploaded, but the filing process may not proceed until edits are verified or the file is corrected and re-uploaded."
    }
}
```
- Verify Macro Edits
```
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1/edits/macro' \
--header 'Content-Type: application/json' \
--data '{"verified": true}'

{
    "verified": true,
    "status": {
        "code": 14,
        "message": "Your data is ready for submission.",
        "description": "Your financial institution has certified that the data is correct, but it has not been submitted yet."
    }
}
```
- Sign 
```
curl --location 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1/sign' \
--header 'Content-Type: application/json' \
--data '{"signed": true}'

{
    "email": "dev@dev.com",
    "timestamp": 1738613553416,
    "receipt": "B90YWS6AFX2LGWOXJ1LD-2022-1-1738613553416",
    "status": {
        "code": 15,
        "message": "Your submission has been accepted.",
        "description": "This completes your HMDA filing process for this year. If you need to upload a new HMDA file, the previously completed filing will not be overridden until all edits have been cleared and verified, and the new file has been submitted."
    },
    "signerUsername": "dev"
}
```
- Get Sign
```
curl --location --request GET 'http://localhost:8080/institutions/B90YWS6AFX2LGWOXJ1LD/filings/2022/submissions/1/sign' \
--header 'Content-Type: application/json' \
--data '{"signed": true}'

{
    "email": "dev@dev.com",
    "timestamp": 1738613553416,
    "receipt": "B90YWS6AFX2LGWOXJ1LD-2022-1-1738613553416",
    "status": {
        "code": 15,
        "message": "Your submission has been accepted.",
        "description": "This completes your HMDA filing process for this year. If you need to upload a new HMDA file, the previously completed filing will not be overridden until all edits have been cleared and verified, and the new file has been submitted."
    },
    "signerUsername": "dev"
}
```
