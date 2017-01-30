# HMDA File Parser

Standalone parser for the Home Mortgage Disclosure Act (HMDA) submission file, as per the 2017 [File Specification Guide](http://www.consumerfinance.gov/data-research/hmda/static/for-filers/2017/2017-HMDA-FIG.pdf)

To run in a Node.js environment, do the following:

```javascript
npm install hmda-file-parser
```

This will install the latest version of the library. The parser accepts `Transmittal Sheet` or `Loan Application Register` strings as input data. 

To instantiate the parser:

```javascript
var js = require('hmda-file-parser');
var parser = new hmda.parser.fi.CsvReader();
```
Here are a couple of examples parsing HMDA data

* Transmittal Sheet

```javascript
var ts = parser.parseTs('1|21|1|201503111034|2017|35-0704860|10|CENTRAL FIRST BANK|221 Hesburgh Library|Notre Dame|IN|46556|FIRST BANK|2566 S. Kinnickinnic Ave.|Milwaukee|WI|53207|Anne Shirley|574-555-2000|574-555-2001|as@centerfirst.COM');
```

Which parses into the following structure:

```json
{
  "id": 1,
  "agencyCode": 1,
  "timestamp": 201503111034
  "activityYear": 2017,
  "taxId": "35-0704860",
  "totalLines": 10,
  "respondent": {
    "id": 21,
    "name": "CENTRAL FIRST BANK,
    "address": "221 Hesburgh Library",
    "city": "Notre Dame",
    "state": "IN",
    "zipCode": "46556"
  },
  "parent": {
    "name": "FIRST BANK",
    "address": "2566 S. Kinnickinnic Ave.", 
    "city": "Milwaukee",
    "state": "WI",
    "zipCode": "53207"
  },
  "Contact": {
    "name": "Anne Shirley",
    "phone": "574-555-2000",
    "fax": "574-555-2001",
    "email": "as@centerfirst.COM"
  }
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
    "county": "165"
    "tract": "0205.00"
  },
  "applicant": {
    "ethnicity": 2
    "coEthnicity": 5
    "race1": 5
    "race2": ""
    "race3": ""
    "race4": ""
    "race5": ""
    "coRace1": 8
    "coRace2": ""
    "coRace3": ""
    "coRace4": ""
    "coRace5": ""
    "sex": 1
    "coSex": 5
    "income": "23"
  },
  "purchaserType": 1,
  "denial": {
    "reason1": "0"
    "reason2": ""
    "reason3": ""
  },
  "rateSpread": "NA",
  "hoepaStatus: 2,
  "lienStatus": 1
}
```

