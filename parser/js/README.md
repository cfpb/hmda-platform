# HMDA File Parser

Standalone parser for the Home Mortgage Disclosure Act (HMDA) submission file, as per the 2017 [File Specification Guide](http://www.consumerfinance.gov/data-research/hmda/static/for-filers/2017/2017-HMDA-FIG.pdf)

## Building

The standalone HMDA file parser is built with [Scala.js](https://www.scala-js.org/).
To build this project, clone the repository from GitHub:

```shell
git clone https://github.com/cfpb/hmda-platform.git
```

[`SBT`](https://github.com/sbt/sbt) is required to build the library.

```shell
cd hmda-platform
$ sbt
> project parserJS
> fullOptJS
```

This will build the production ready `JavaScript` library containing the parser, and place it in parser/js/target/scala-2.12/parserjs-opt.js

NOTE: Building the project is only necessary if you intend to work with it locally from source code, or work on the implementation.
If you just want to use the library, see the next section

## Using

This library is published to [NPM](https://www.npmjs.com/), to run in a Node.js environment, do the following:

```javascript
npm install hmda-file-parser
```

This will install the latest version of the library. The parser accepts `Transmittal Sheet` or `Loan Application Register` strings as input data. 

To instantiate the parser:

```javascript
var js = require('hmda-file-parser');
var parser = new js.hmda.parser.fi.CsvParser();
```
Here are a couple of examples parsing HMDA data

* Transmittal Sheet

```javascript
var ts = parser.parseTs('1|21|1|201503111034|2017|99-9999999|10|Bank 0|1275 1st ST NW|Washington|DC|20054|BANK 0|1275 1st ST NE|Washington|DC|20054|Joan Smith|555-555-5555|555-555-5555|js@bank0.com');
```

Which parses into the following structure:

```json
{
  "id": 1,
  "agencyCode": 1,
  "timestamp": 201503111034,
  "activityYear": 2017,
  "taxId": "99-9999999",
  "totalLines": 10,
  "respondent": {
    "id": 21,
    "name": "Bank 0",
    "address": "1275 1st ST NW",
    "city": "Washington",
    "state": "DC",
    "zipCode": "20054"
  },
  "parent": {
    "name": "BANK 0",
    "address": "1275 1st ST NW",
    "city": "Washington",
    "state": "DC",
    "zipCode": "20054"
  },
  "Contact": {
    "name": "Joan Smith",
    "phone": "555-555-5555",
    "fax": "555-555-5555",
    "email": "js@bank0.com"
  }
}
```

```javascript
var ts = parser.parseTs('NA|21|NA|201503111034|2017|99-9999999|10|Bank 0|1275 1st ST NW|Washington|DC|20054|BANK 0|1275 1st ST NE|Washington|DC|20054|Joan Smith|555-555-5555|555-555-5555|js@bank0.com');
```

This `Transmittal Sheet` does not conform to the specification, returning the following errors:

```json
{ 
  "errors":
   [ "Record Identifier is not an Integer",
     "Agency Code is not an Integer",
     "Applicant Ethnicity is not an Integer" ]
}
```


* Loan Application Register

```javascript
var lar = parser.parseLar('2|21|1|10531                    |20170304|1|1|2|1|43|3|4|20170723|45460|18|165|0205.00|2|5|5| | | | |8| | | | |1|5|23|0| | | |NA   |2|1');
```

which parses into the following structure:

```json
{
  "id": 2,
  "respondentId": "21",
  "agencyCode": 1,
  "loan": {
    "id": "10531",
    "applicationDate": "20170304",
    "loanType": 1,
    "propertyType": 1,
    "purpose": 2,
    "occupancy": 1,
    "amount": 43
  },
  "preapprovals": 3,
  "actionTakenType": 4,
  "actionTakenDate": "20170723",
  "gepography": {
    "msa": "45460",
    "state": "18",
    "county": "165",
    "tract": "0205.00"
  },
  "applicant": {
    "ethnicity": 2,
    "coEthnicity": 5,
    "race1": 5,
    "race2": "",
    "race3": "",
    "race4": "",
    "race5": "",
    "coRace1": 8,
    "coRace2": "",
    "coRace3": "",
    "coRace4": "",
    "coRace5": "",
    "sex": 1,
    "coSex": 5,
    "income": "23"
  },
  "purchaserType": 1,
  "denial": {
    "reason1": "0",
    "reason2": "",
    "reason3": ""
  },
  "rateSpread": "NA",
  "hoepaStatus": 2,
  "lienStatus": 1
}
```

```javascript
var lar = parser.parseLar('NA|21|NA|10531                    |20170304|1|1|2|1|43|3|4|20170723|45460|18|165|0205.00|2|5|5| | | | |8| | | | |1|5|23|0| | | |NA   |2|1');
```
This `Loan Application Register` does not conform to the specification, this parsing operation returns the following errors

```json
{ 
  "errors":
   [ "Record Identifier is not an Integer",
     "Agency Code is not an Integer" ]
}
```

