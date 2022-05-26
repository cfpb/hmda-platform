# HMDA Platform Public API

This documentation describes de public HMDA Platform HTTP API

## Institutions

### Search

* `/institutions/period/<period>?domain=<domain>`

   * `GET` - Returns a list of institutions filtered by their email domain and period (year). If none are found, an HTTP 404 error code (not found) is returned

   Example response, with HTTP code 200:
   
   ```json
   {
     "institutions": [
       {
         "activityYear" : 2019,
         "LEI" : "54930084UKLVMY22DS16",
         "agency" : 1,
         "institutionType" : 17,
         "institutionId2017" : "12345",
         "taxId" : "99-00000000",
         "rssd" : 12345,
         "emailDomains" : ["bank0.com"],
         "respondent" : {
           "name" : "xvavjuitZa",
           "state" : "NC",
           "city" : "Raleigh"
         },
         "parent" : {
           "idRssd" : 1520162208,
           "name" : "Parent Name"
         },
         "assets" : 450,
         "otherLenderCode" : 1406639146,
         "topHolder" : {
           "idRssd" : 442825905,
           "name" : "TopHolder Name"
         },
         "hmdaFiler" : true,
         "quarterlyFiler" : false
       } 
     ]
   } 
   ```
   * `/institutions/?domain=<domain>` - defaults to the year 2018

* `/institutions/<period>?domain=<domain>&lei=<lei>&respondentName=<respondentName>&taxId=<taxId>`

   * `GET` - Returns a list of institutions filtered by field values and period (year). If none are found, an HTTP 404 error code (not found) is returned

   Example response, with HTTP code 200:
   
   ```json
   {
     "institutions": [
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
         "hmdaFiler" : true,
         "quarterlyFiler" : false
  ,
       } 
     ]
   } 
   ```
   
* `/institutions/<institutionID>/period/<period>`

    * `GET`

    Retrieves the details of an institution by LEI and period (year). If not found, returns HTTP code 404

    Example Response with HTTP code 200, in `JSON` format:
    
    ```json
    {
      "activityYear" : 2019,
      "LEI" : "54930084UKLVMY22DS16",
      "agency" : 1,
      "institutionType" : 17,
      "institutionId2017" : "12345",
      "taxId" : "99-00000000",
      "rssd" : 12345,
      "emailDomains" : ["bank0.com"],
      "respondent" : {
        "name" : "xvavjuitZa",
        "state" : "NC",
        "city" : "Raleigh"
      },
      "parent" : {
        "idRssd" : 1520162208,
        "name" : "Parent Name"
      },
      "assets" : 450,
      "otherLenderCode" : 1406639146,
      "topHolder" : {
        "idRssd" : 442825905,
        "name" : "TopHolder Name"
      },
      "hmdaFiler" : true,
      "quarterlyFiler" : false
    }     
    ```

## TS Parsing and Validation

### Parsing

* `/ts/parse`

    * `POST` Returns a `JSON` representation of a TS, or a list of errors if the TS fails to parse

    Example body, in `JSON` format:

    ```json
    {
      "ts" : "1|Bank 0|2018|4|Jane|111-111-1111|janesmith@bank.com|123 Main St|Washington|DC|20001|9|100|99-999999|10Bx939c5543TqA1144M"
    }
    ```

    Example response, in `JSON` format:

    ```json
    {
        "id": 1,
        "institutionName": "Bank 0",
        "year": 2018,
        "quarter": 4,
        "contact": {
            "name": "Jane",
            "phone": "111-111-1111",
            "email": "janesmith@bank.com",
            "address": {
                "street": "123 Main St",
                "city": "Washington",
                "state": "DC",
                "zipCode": "20001"
            }
        },
        "agency": 9,
        "totalLines": 100,
        "taxId": "99-999999",
        "LEI": "10Bx939c5543TqA1144M"
    }
    ```
    
### Validation

* `/ts/validate/<year>`
    
    * `POST` - Returns a `JSON` representation of a TS, or a list of edits if the TS fails to validate for the `<year>` passed in (2019 validations are work-in-progress).
    
    Example body, in `JSON` format:
    
    ```json
    {
      "ts": "1|Bank1|2018|4|testname|555-555-5555|test@email.com|1234 Hocus Potato Way|Testtown|UT|84096|9|1000|02-1234567|BANK1LEIFORTEST12345"
    }
    ```
    
    Example successful response, in `JSON` format: See above

    Example failed response, in `JSON` format:
    
    ```json
    {
        "syntactical": {
            "errors": [
              "S300"
            ]
        },
        "validity": {
            "errors": [
                "V600",
                "V601"
            ]
        },
        "quality": {
            "errors": []
        }
    }
    ```


## LAR Parsing and Validation

### Parsing

* `/lar/parse`

    * `POST` - Returns a `JSON` representation of a LAR, or a list of errors if the LAR fails to parse

    Example body, in `JSON` format:

    ```json
    {
      "lar": "2|10Bx939c5543TqA1144M|10Bx939c5543TqA1144M999143X38|20180721|1|1|1|1|1|110500|1|20180721|123 Main St|Beverly Hills|CA|90210|06037|06037264000|1|1|1|1|1||1|1|1|1|1||3|3|5|7|7|7|7||||5|7|7|7|7||||3|3|1|1|3|3|30|30|36|1|0.428|1|1|750|750|1|9|1|9|10|10|10|10||2399.04|NA|NA|NA|NA|4.125|NA|42.95|80.05|360|NA|1|2|1|1|350500|1|1|5|NA|1|1|12345|1|1|1|1|1||1|1|1|1|1||1|1|1"
    }
    ```

    Example response, in `JSON` format:


    ```json
    {
        "larIdentifier": {
            "id": 2,
            "LEI": "10Bx939c5543TqA1144M",
            "NMLSRIdentifier": "12345"
        },
        "loan": {
            "ULI": "10Bx939c5543TqA1144M999143X38",
            "applicationDate": "20180721",
            "loanType": 1,
            "loanPurpose": 1,
            "constructionMethod": 1,
            "occupancy": 1,
            "amount": 110500,
            "loanTerm": "360",
            "rateSpread": "0.428",
            "interestRate": "4.125",
            "prepaymentPenaltyTerm": "NA",
            "debtToIncomeRatio": "42.95",
            "loanToValueRatio": "80.05",
            "introductoryRatePeriod": "NA"
        },
        "larAction": {
            "preapproval": 1,
            "actionTakenType": 1,
            "actionTakenDate": 20180721
        },
        "geography": {
            "street": "123 Main St",
            "city": "Beverly Hills",
            "state": "CA",
            "zipCode": "90210",
            "county": "06037",
            "tract": "06037264000"
        },
        "applicant": {
            "ethnicity": {
                "ethnicity1": 1,
                "ethnicity2": 1,
                "ethnicity3": 1,
                "ethnicity4": 1,
                "ethnicity5": 1,
                "otherHispanicOrLatino": "",
                "ethnicityObserved": 3
            },
            "race": {
                "race1": 5,
                "race2": 7,
                "race3": 7,
                "race4": 7,
                "race5": 7,
                "otherNativeRace": "",
                "otherAsianRace": "",
                "otherPacificIslanderRace": "",
                "raceObserved": 3
            },
            "sex": {
                "sex": 1,
                "sexObserved": 3
            },
            "age": 30,
            "creditScore": 750,
            "creditScoreType": 1,
            "otherCreditScoreModel": "9"
        },
        "coApplicant": {
            "ethnicity": {
                "ethnicity1": 1,
                "ethnicity2": 1,
                "ethnicity3": 1,
                "ethnicity4": 1,
                "ethnicity5": 1,
                "otherHispanicOrLatino": "",
                "ethnicityObserved": 3
            },
            "race": {
                "race1": 5,
                "race2": 7,
                "race3": 7,
                "race4": 7,
                "race5": 7,
                "otherNativeRace": "",
                "otherAsianRace": "",
                "otherPacificIslanderRace": "",
                "raceObserved": 3
            },
            "sex": {
                "sex": 1,
                "sexObserved": 3
            },
            "age": 30,
            "creditScore": 750,
            "creditScoreType": 1,
            "otherCreditScoreModel": "9"
        },
        "income": "36",
        "purchaserType": 1,
        "hoepaStatus": 1,
        "lienStatus": 1,
        "denial": {
            "denialReason1": 10,
            "denialReason2": 10,
            "denialReason3": 10,
            "denialReason4": 10,
            "otherDenialReason": ""
        },
        "loanDisclosure": {
            "totalLoanCosts": "2399.04",
            "totalPointsAndFees": "NA",
            "originationCharges": "NA",
            "discountPoints": "NA",
            "lenderCredits": "NA"
        },
        "nonAmortizingFeatures": {
            "balloonPayment": 1,
            "interestOnlyPayment": 2,
            "negativeAmortization": 1,
            "otherNonAmortizingFeatures": 1
        },
        "property": {
            "propertyValue": "350500.0",
            "manufacturedHomeSecuredProperty": 1,
            "manufacturedHomeLandPropertyInterest": 1,
            "totalUnits": 5,
            "multiFamilyAffordableUnits": "NA"
        },
        "applicationSubmission": 1,
        "payableToInstitution": 2,
        "AUS": {
            "aus1": 1,
            "aus2": 1,
            "aus3": 1,
            "aus4": 1,
            "aus5": 1,
            "otherAUS": ""
        },
        "ausResult": {
            "ausResult1": 1,
            "ausResult2": 1,
            "ausResult3": 1,
            "ausResult4": 1,
            "ausResult5": 1,
            "otherAusResult": ""
        },
        "reverseMortgage": 1,
        "lineOfCredit": 1,
        "businessOrCommercialPurpose": 1
    }
    ```

### Validation

* `/lar/validate/<year>`
    
    * `POST` - Returns a `JSON` representation of a LAR, or a list of edits if the LAR fails to validate for the `<year>` passed in (2019 validations are work-in-progress)
    
    Example body, in `JSON` format:
    
    ```json
    {
      "lar": "2|10Bx939c5543TqA1144M|10Bx939c5543TqA1144M999143X38|20180721|1|1|1|1|1|110500|1|20180721|123 Main St|Beverly Hills|CA|90210|06037|06037264000|1|1|1|1|1||1|1|1|1|1||3|3|5|7|7|7|7||||5|7|7|7|7||||3|3|1|1|3|3|30|30|36|1|0.428|1|1|750|750|1|9|1|9|10|10|10|10||2399.04|NA|NA|NA|NA|4.125|NA|42.95|80.05|360|NA|1|2|1|1|350500|1|1|5|NA|1|1|12345|1|1|1|1|1||1|1|1|1|1||1|1|1"
    }
    ```
    
    Example successful response, in `JSON` format: See above

    Example failed response, in `JSON` format:
    
    ```json
    {
        "syntactical": {
            "errors": []
        },
        "validity": {
             "errors": [
                {
                    "edit": "V614-3",
                    "description": "If Reverse Mortgage equals 1, then Preapproval must equal 2."
                },
                {
                    "edit": "V614-4",
                    "description": "If Open-End Line of Credit equals 1, then Preapproval must equal 2."
                },
                {
                    "edit": "V615-2",
                    "description": "If Manufactured Home Land Property Interest equals 1, 2, 3 or 4, then Construction Method must equal 2."
                }
            ]
        },
        "quality": {
            "errors": [
                {
                    "edit": "Q608",
                    "description": "If Action Taken equals 1, then the Action Taken Date generally should occur after the Application Date."
                }
            ]
        }
    }
    ```

## HMDA File Parsing and Validation

### Parsing

* `/hmda/parse`

    * `POST` - Parses a HMDA file. Returns list of errors per line in `JSON` format, if found.

    Example file:

    ```
    1|Bank 0|2018|4|Jane|111-111-1111|janesmith@bank.com|123 Main St|Washington|DC|20001|9|100|99-999999|10Bx939c5543TqA1144M
    2|10Bx939c5543TqA1144M|10Bx939c5543TqA1144M999143X38|20180721|A|1|1|1|1|110500|1|20180721|123 Main St|Beverly Hills|CA|90210|06037|06037264000|1|1|1|1|1||1|1|1|1|1||3|3|5|7|7|7|7||||5|7|7|7|7||||3|3|1|1|3|3|30|30|36|1|0.428|1|1|750|750|1|9|1|9|10|10|10|10||2399.04|NA|NA|NA|NA|4.125|NA|42.95|80.05|360|NA|1|2|1|1|350500|1|1|5|NA|1|1|12345|1|1|1|1|1||1|1|1|1|1||1|1|1
    ```

    Example Response, in `JSON` format:

    ```json
    {
        "validated": [
            {
                "lineNumber": 2,
                "errors": "loan type is not numeric"
            }
        ]
    }
    ```



* `/hmda/parse/csv`

    * `POST` - Parses a HMDA file. Returns list of errors per line in `CSV` format, if found.

    Example file:

    ```
    1|Bank 0|2018|4|Jane|111-111-1111|janesmith@bank.com|123 Main St|Washington|DC|20001|9|100|99-999999|10Bx939c5543TqA1144M
    2|10Bx939c5543TqA1144M|10Bx939c5543TqA1144M999143X38|20180721|A|1|1|1|1|110500|1|20180721|123 Main St|Beverly Hills|CA|90210|06037|06037264000|1|1|1|1|1||1|1|1|1|1||3|3|5|7|7|7|7||||5|7|7|7|7||||3|3|1|1|3|3|30|30|36|1|0.428|1|1|750|750|1|9|1|9|10|10|10|10||2399.04|NA|NA|NA|NA|4.125|NA|42.95|80.05|360|NA|1|2|1|1|350500|1|1|5|NA|1|1|12345|1|1|1|1|1||1|1|1|1|1||1|1|1
    ```

    Example response, in `CSV` format:

    ```csv
    lineNumber|errors
    2|loan type is not numeric
    ```

### Parsing

* `hmda/validate/<year>`

    * `POST` - Parses and validates a HMDA file.

    Example response, in `JSON` format:

    ```json
    {
        "parserErrors": [
            {
                "rowNumber": 1,
                "estimatedULI": "Transmittal Sheet",
                "errorMessages": [
                    {
                        "fieldName": "Calendar Quarter",
                        "inputValue": "a",
                        "validValues": "Integer"
                    }
                ]
            }
        ],
        "validationErrors": [
            [
                {
                    "uli": "B90YWS6AFX2LGWOXJ1LDNIXOQ6OO7BRA5SLR6FSJJ5R89",
                    "editName": "V619-2",
                    "editDescription": "\"The Action Taken Date must be in the reporting year.\"",
                    "fields": {
                        "Action Taken Date": "20180908",
                        "Application Date": "NA"
                    }
                }
            ]
        ]
    }
    ```


