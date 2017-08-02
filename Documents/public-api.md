# HMDA Platform Public API

This documenatation describes de public HMDA Platform HTTP API

## Institutions

### Search

* `/institutions?domain=<domain>`

   * `GET` - Returns a list of institutions filtered by their email domain. If none are found, an HTTP 404 error code (not found) is returned

   Example response, with HTTP code 200:

   ```json
   {
     "institutions":
     [
        {
          "id": "0",
          "name": "Bank 0",
          "domains": ["test@bank0.com"],
          "externalIds":[
            {
              "value": "1234",
              "name": "occ-charter-id"
            },
            {
              "value": "1234",
              "name": "ncua-charter-id"
            }
          ]
        }
     ]
   }
   ```

### Modified LAR

* `/institutions/<institutionId>/filings/<period>/lar`

   * `GET` - Returns the Modified LAR, in CSV format. The schema for the data is as follows:

```
   id
   respondent_id
   agency_code
   preapprovals
   action_taken_type
   purchaser_type
   rate_spread
   hoepa_status
   lien_status
   loan_type
   property_type
   purpose
   occupancy
   amount
   msa
   state
   county
   tract
   ethnicity
   co_ethnicity
   race1
   race2
   race3
   race4
   race5
   co_race1
   co_race2
   co_race3
   co_race4
   co_race5
   sex
   co_sex
   income
   denial_reason1
   denial_reason2
   denial_reason3
   period
```

For a definition of these fields, please consult the [HMDA Filing Instructions Guide](http://www.consumerfinance.gov/data-research/hmda/static/for-filers/2017/2017-HMDA-FIG.pdf).
Please note that the Modified LAR does not include the fields `Loan Application Number`, `Date Application Received` or `Date of Action` described in HMDA Filing Instructions Guide.




