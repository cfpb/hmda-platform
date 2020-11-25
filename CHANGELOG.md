# Changelog

## HMDA Platform v2.16.0 (20/10/2020)
- Resolved Institution Note History bugs (#3808)
- Fetch Edit details from cassandra (#3813)
- HMDA Data Publisher Zip Dynamic Public LAR File on Upload to S3 (#3819)
- Implement endpoint for hmda-help to download submission file #3828
- Add quarterly endpoints to hmda-dashboard(#3830)
- Create an endpoint for hmda-help to find the most recent signed submission id of a filer (#3846)
- Increase timeouts on filing api endpoints (#3847
- Create admin endpoint to find the first signed submission for a period (#3864)
- Update Platform Diagram (#3883)
- Update dashboard to use materialized views (#3861)
- Institution Note History Tracker: Bug Resolution(#3808)
---

## HMDA Platform v2.15.0 (13/08/2020)
- Update RateSpread Calculator Logs and Error Handling #3744 
- Add age applicant to Data Browser as independent variable #3758 
- Add create-date, update-date, and notes to institutions model #3741 
- Add logic to the data publisher to generate snapshot files #3715
- Change filings endpoint to return submissions in an order #3368
- Prevent institution creation when LEI is an LOU #3757
- History of Institution Update Notes #3773
- HMDA analytics improvements (selective delete) #3775
- Add Admin Route for Adding Publication Kafka Topics #3774
- Progress tracking for submissions with Websockets #3779
- Create Primary Key mapping between loan application register and Modified Lar PG table #3766
- Create Route to validate whole HMDA File in public-api #2762 
- Upgrade Keycloak Image and Update Documentation #3796
---

## HMDA Platform v2.14.6 (07/07/2020)
- alpine base docker image update
---

## HMDA Platform v2.14.5 (23/06/2020)
- Additional HMDA Data Publisher 2019 Publication Support (#3703)
- Add configuration options to modified lar (#3713)
- Optimize the slowness of hmda-analytics (#3518)
- Add redis flag to data-browser-api response (#3008)
- HMDA Analytics bug reading kafka events (#3675)
- Provide multiple year support to Data Browser (#3702)


---

## HMDA Platform v2.14.4 (03/06/2020)
- Resolved Quartz Scheduling Bug ( #3685)
- Added age filter to data browser (#3643)
- Removed V718 from yearly edit (#3692)

---

## HMDA Platform v2.14.3 (21/05/2020)
- Upgrade to Akka 2.6(#3601)
- Add Pagination Option for Complete Submission History (#3642)
- Update README (#3604)
- Institution Loader Error on POST (#3670)
- Data browser file names not matching (#3680)
---

## HMDA Platform v2.14.2 (22/04/2020)
- Updates base docker image to Open JDK-Slim 14.0.1
---

## HMDA Platform v2.14.1 (10/04/2020)
- Updates to reflect new data publisher file generation logic for msa/md fields
- Prevent lower case LEIs in TS table (#3586)
---

## HMDA Platform v2.14.0 (31/03/2020)
- Resolved errors in hmda-reporting (#3505)
- Resolved Q627 Loan Amount showing up in scientific notation (#3573)
- New Data Publisher File Generation For Agency LAR Data (#3549)
- Resolved error with S306 & Q600 Edit table display (#3528)
- Resolve Q649_2 not handling 9999 (#3535)
- Add links to API Docs for Postman collection (#3553)
- Updates for Quarterly Filing

---

## HMDA Platform v2.13.11 (15/03/2020)
Fix conforming loan limit (#3531)
---

## HMDA Platform v2.13.10 (11/03/2020)

---

## HMDA Platform v2.13.9 (10/02/2020)
- Version upgrades to address security updates
- Remove dependency of irs-publisher from census-api (#3479)
- Unit Testing and Code Coverage for Data Browser (#3463)
- Improve institution loader (#3419)
---

## HMDA Platform v2.13.8 (06/02/2020)
- Update response text for Q606 (https://github.com/cfpb/hmda-platform/pull/3474)
- Update empty sign dates to null (https://github.com/cfpb/hmda-platform/pull/3472)
- Improvements to institution loader (https://github.com/cfpb/hmda-platform/pull/3469)
- Updates to 2020 edits naming convention (https://github.com/cfpb/hmda-platform/pull/3476)
- Upgrade to JDK11 Images 
---

## HMDA Platform v2.13.7 (03/02/2020)
Notable Changes:

- Updated Institution Admin API  behavior (#3361)
- Check-digit:  better logic for processing large files (#3429)
- Updates for Quarterly Filer Submission Status(#3396 )
- Additional Quality Edits for 2020 Quarterly Filing (#3457  #3214 )
- Added headers to modified-lar file generation (#2983)
- Update Institutions API LEI Yearly Look Up for 2020(#3427 )
- Converting TransmittalSheet PG entries with no sign date to "Not Available"  (#3431 )
---

## HMDA Platform v2.13.6 (22/01/2020)
Notable Changes:

- Better Institution API error message responses (#3415)
- Performance improvements to the platform base on load testing results (#3316)
- Change CLTV name in headers and descriptions (#3152)
- Move 2017 Filers Endpoint to 2018 Platform (#3343)
- Update Validity Edit results display (#3414)

---

## HMDA Platform v2.13.6 (22/01/2020)

---

## HMDA Platform v2.13.5 (04/01/2020)
Notable Changes: 

- Change keycloak theme url to 2019 (#3394)
- Add hmda-frontend to list of public repositories (#3395)
- Fix intermittent 405 errors on POST on sign (#3399)
---

## HMDA Platform v2.13.4 (31/12/2019)
Notable Changes: 

- Fix images in keycloak for login page (https://github.com/cfpb/hmda-platform/issues/3386)
---

## HMDA Platform v2.13.3 (30/12/2019)
Notable Changes:

- Change institutions API to show LEI's for the current year when searching via email domain (#3378)
---

## HMDA Platform v2.13.2 (26/12/2019)
Notable Changes:

- Internal dashboard API (#3289)
- Toggling Jenkins jobs (#3372)
- Improvements to authentication (#3387)
- Updating data-publisher config maps (#3382)
---

## HMDA Platform  v2.13.0 (19/12/2019)
Notable Changes:

- Edge case test for lar and ts row sanitizing (#3374  )
- Resolved Parser Spelling errors(#3379 )
- Corrected helm files for email deployment (#3377)
- Documentation updates for 2019 release ops(#3367 )
---

## HMDA Platform v2.12.9 (17/12/2019)
Notable Changes:

- Sanitize LAR and TS CSV rows ( remove control characters, BOM, and a trailing pipe character) (#3291 )
- Bank Filter for Email Service (#3330)
- Better logging for Quality and Macro edits (#3338)
- Hmda-Analytics and Data Publisher updates for transmittalsheet table submission sign date updates(#3295)
- Updated performance metrics generation/collection (#3349)
- Corrected Helm deployments via Jenkins(#3331 )

---

## HMDA Platform v2.12.8 (03/12/2019)
Notable Changes:

- Optimize S305 and Q600 edit check performance (#3271 )
- Updated Tax-ID and LEI verification checks in Institutions Api (#2669)
- Remove 2018 LEI constraint from Institutions API email table update(#3294)
- Data Browser Content-Disposition header too long (#3249)
- Update Data Publisher For quarterly regulator file generation (#3225)
- Resolved Institution API bug  when requesting  quarterly filings (#3301)
- Fix Keycloak Impersonation Feature (#3279)
- Updated unit test for macro errors (#3296)

---

## HMDA Platform v2.12.7 (26/11/2019)
Notable Changes:

- Release for Data Browser
- Fix compiler warnings (#3227)
- Create 2020 tables for quarterly filing (#3225)
- Add logging to data browser (#3282)
- Fix verification logic for files with Quality but no Macro (#3280)
- Pull filers data for data-browser from cache (#3284)
---

## HMDA Platform v2.12.6 (22/11/2019)
Notable Changes:

- Deployment of hmda-platform to beta
- Endpoint to fetch LEIs for data-browser (#3193 , #3269)
- Removing foreign key from institution table (#3250)
- Update instructions for running data-browser locally (#3259)
- Place modified lar in buckets based on years (#3219)
- Add timed guards for hmda-platform (#3198)
- Remove affinity rules from hmda-platform beta (#3245)
- Helm chart and jenkins files of email service (#3253 #3244)
- Update public lar endpoints (#3221)
- Send submission receipt to email topic (#3252)
- Fix being able to sign files with Quality / Macro Errors (#3272)
---

## HMDA Platform v2.12.5 (06/11/2019)
**Notable Changes**

- Add more logging to the platform to better track filer progress (https://github.com/cfpb/hmda-platform/issues/3196)
- Fix settings and configuration issues with the email microservice (https://github.com/cfpb/hmda-platform/issues/3239)
- Modify hmda-analytics to include quarterly submissions (https://github.com/cfpb/hmda-platform/issues/3197)
- Implementing Quarterly Filing (https://github.com/cfpb/hmda-platform/issues/3167)
- Authenticate quarterly filers for front end and API filing (https://github.com/cfpb/hmda-platform/issues/3174)
- Add validity edit V718 for Quarterly filing (https://github.com/cfpb/hmda-platform/issues/3200)
- Fix Q645 not displaying the loan amount or loan purpose (https://github.com/cfpb/hmda-platform/issues/3235)
- Pick up edit descriptions based on the filing year (https://github.com/cfpb/hmda-platform/issues/3233)
- Remove edits from quarterly filing (https://github.com/cfpb/hmda-platform/issues/3220)
- Return TS and LAR counts for S304 (https://github.com/cfpb/hmda-platform/issues/3224)
- Fixing pipe endpoint in data browser (https://github.com/cfpb/hmda-platform/issues/3022)
---

## HMDA Platform v2.12.4 (29/10/2019)
**Notable Changes**

- Show field value instead of "invalid data" on edits (https://github.com/cfpb/hmda-platform/pull/3217)
- Add post/put arguments to institution loader (https://github.com/cfpb/hmda-platform/pull/3202)
- Increases the amount of data in the api return for parsing errors (https://github.com/cfpb/hmda-platform/issues/3146)
---

## HMDA Platform v2.12.3 (24/10/2019)
**Notable Changes:**

- Add date to transmittal sheet for regulator data (https://github.com/cfpb/hmda-platform/issues/3190)
- Display the edits in sorted order (https://github.com/cfpb/hmda-platform/issues/3192)
- Add county filter option to Data Browser API (https://github.com/cfpb/hmda-platform/issues/3182)
- Add LEI filter option to Data Browser API (https://github.com/cfpb/hmda-platform/issues/3184)
- Add 2019 Census file (https://github.com/cfpb/hmda-platform/issues/3172)
- Show additional messaging on parsing errors (https://github.com/cfpb/hmda-platform/issues/3146)
---

## HMDA Platform v2.12.2 (10/10/2019)
2019 Beta Updates

- Q647 not triggering (#3132)
- Fixing LAR Engine selector between years (#3122)
- Fixing Q632 and Q633 edits for 2019 (#3121)
- Updating V696-2 & V699 for 2019 (#3107)
---

## HMDA Platform v2.12.1 (03/09/2019)
- Fix connection pools in hmda-reporting (https://github.com/cfpb/hmda-platform/pull/3052)
- Change redirect in data-browser-api (https://github.com/cfpb/hmda-platform/pull/3064)