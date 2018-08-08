# Institutions Read Only API

## Testing

### Running project and database


First, run a `postgres` database in `Docker`:

```shell
docker run -p 54321:5432 postgres
```

Confirm that database server is up and running by connecting to it (adjust IP for `Docker` host as appropriate):

```shell
$ psql hmda -h 192.168.99.100 -U postgres

```

To load the data, issue the commands in the following [script](src/test/resources/institutions.sql) from the `psql` prompt

To run the project:

```shell
$sbt
sbt:root> project institutions-api
sbt:institutions-api> reStart
```

In order to confirm that the API is up and running:

```shell
curl -XGET http://localhost:9092
```

The response should be similar to the following:

```json
  {
    "status":"OK",
    "service":
    "hmda-institution-api",
    "time":"2018-08-08T19:08:20.655Z",
    "host":"localhost"
  }
```

Test that the data has been loaded and that the API responds to queries:

```shell
$ curl -XGET http://localhost:9092/institutions/54930084UKLVMY22DS16
```

Response:

```json
   {
     "activityYear":2019,
     "LEI":"54930084UKLVMY22DS16",
     "agency":"1",
     "institutionType":"17",
     "institutionId2017":"12345",
     "taxId":"99-00000000",
     "rssd":"Pb",
     "emailDomains":
        ["aaa.com","bbb.com"],
     "respondent": {
        "name":"xvavjuitZa",
        "state":"NC",
        "city":"Raleigh"
     },
     "parent":{
        "idRssd":"1520162208",
        "name":"Parent Name"
     },
     "assets":"450",
     "otherLenderCode":"1406639146",
     "topHolder":{
        "idRssd":"442825905",
        "name":"TopHolder Name"
     },
     "hmdaFiler":true
   }
```

```shell
$ curl -XGET http://localhost:9092/institutions?domain=aaa.com
```

Response:

```json
  {"institutions":
     [
        {
            "activityYear":2019,
            "LEI":"54930084UKLVMY22DS16",
            "agency":"1",
            "institutionType":"17",
            "institutionId2017":"12345",
            "taxId":"99-00000000",
            "rssd":"Pb",
            "emailDomains":
                ["aaa.com"],
            "respondent":{
                "name":"xvavjuitZa",
                "state":"NC",
                "city":"Raleigh"
            },
            "parent":{
                "idRssd":"1520162208",
                "name":"Parent Name"
            },
            "assets":"450",
            "otherLenderCode":"1406639146",
            "topHolder":{
                "idRssd":"442825905",
                "name":"TopHolder Name"
            },
            "hmdaFiler":true
        }
     ]
  }
```







