## Check Digit

### Check digit generation

* `/uli/checkDigit`

   * `POST` - Calculates check digit and full ULI from a loan id.

Example payload, in `JSON` format:

```json
{
  "loanId": "10Bx939c5543TqA1144M999143X"
}
```

Example response:

```json
{
    "loanId": "10Cx939c5543TqA1144M999143X",
    "checkDigit": 10,
    "uli": "10Cx939c5543TqA1144M999143X10"
}
```

A file with a list of Loan Ids can also be uploaded to this endpoint for batch check digit generation.

Example file contents:

```
10Cx939c5543TqA1144M999143X
10Bx939c5543TqA1144M999143X
```

Example response in `JSON` format:

```json
{
    "loanIds": [
        {
            "loanId": "10Bx939c5543TqA1144M999143X",
            "checkDigit": 38,
            "uli": "10Bx939c5543TqA1144M999143X38"
        },
        {
            "loanId": "10Cx939c5543TqA1144M999143X",
            "checkDigit": 10,
            "uli": "10Cx939c5543TqA1144M999143X10"
        }
    ]
}
```

* `/uli/checkDigit/csv`

   * `POST` - calculates check digits for loan ids submitted as a file

Example file contents:

```
10Cx939c5543TqA1144M999143X
10Bx939c5543TqA1144M999143X
```

Example response in `CSV` format:

```csv
loanId,checkDigit,uli
10Bx939c5543TqA1144M999143X,38,10Bx939c5543TqA1144M999143X38
10Cx939c5543TqA1144M999143X,10,10Cx939c5543TqA1144M999143X10
```

### ULI Validation

* `/uli/validate`

   * `POST` - Validates a ULI (correct check digit)

Example payload, in `JSON` format:

```json
{
	"uli": "10Bx939c5543TqA1144M999143X38"
}
```

Example response:

```json
{
    "isValid": true
}
```

A file with a list of ULIs can also be uploaded to this endpoint for batch ULI validation.

Example file contents:

```
10Cx939c5543TqA1144M999143X10
10Bx939c5543TqA1144M999143X38
10Bx939c5543TqA1144M999133X38
```

Example response in `JSON` format:

```json
{
    "ulis": [
        {
            "uli": "10Cx939c5543TqA1144M999143X10",
            "isValid": true
        },
        {
            "uli": "10Bx939c5543TqA1144M999143X38",
            "isValid": true
        },
        {
            "uli": "10Bx939c5543TqA1144M999133X38",
            "isValid": false
        }
    ]
}
```

* `/uli/validate/csv`

   * `POST` - Batch validation of ULIs

Example file contents:

```
10Cx939c5543TqA1144M999143X10
10Bx939c5543TqA1144M999143X38
10Bx939c5543TqA1144M999133X38
```

Example response in `CSV` format:

```csv
uli,isValid
10Cx939c5543TqA1144M999143X10,true
10Bx939c5543TqA1144M999143X38,true
10Bx939c5543TqA1144M999133X38,false
```
