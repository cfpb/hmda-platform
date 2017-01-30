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
`ts.respondentId` should return `201503111034`


* Loan Application Register

var lar = parser.parseLar('2|21|1|10531                    |20170304|1|1|2|1|43|3|4|20170723|45460|18|165|0205.00|2|5|5| | | | |8| | | | |1|5|23|0| | | |NA   |2|1');

`lar.agencyCode` should return 1


