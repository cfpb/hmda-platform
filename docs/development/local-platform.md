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
