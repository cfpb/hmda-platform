[![Build Status](https://travis-ci.org/cfpb/hmda-platform.svg?branch=master)](https://travis-ci.org/cfpb/hmda-platform) [![codecov.io](https://codecov.io/github/cfpb/hmda-platform/coverage.svg?branch=master)](https://codecov.io/github/cfpb/hmda-platform?branch=master)

# HMDA Platform

## Introduction

By using [regtech](https://en.wikipedia.org/wiki/Regulatory_technology), the Home Mortgage Disclosure Act (HMDA) Platform has coded the [Filing Instruction Guide (FIG)](https://s3.amazonaws.com/cfpb-hmda-public/prod/help/2020-hmda-fig.pdf) to ensure the [quarterly](https://ffiec.cfpb.gov/documentation/2020/quarterly-filing-dates/) and [yearly](https://ffiec.cfpb.gov/documentation/2019/annual-filing-dates/) data being submitted () is parsed, validated for data edits (Syntactical, Validity, Quality, and Macro), and submitted as-per the instructions in the FIG. 
[Please watch this short video](https://youtu.be/C_73Swgyc4g) on how HMDA Platform transforms the data upload, validation, and submission process.


For detailed information on Home Mortgage Disclosure Act (HMDA), checkout the [About HMDA page](https://www.consumerfinance.gov/policy-compliance/rulemaking/final-rules/regulation-c-home-mortgage-disclosure-act/) on the CFPB website.

## TS and LAR File Specs

The data is submitted in a flat pipe (`|`) delimitted TXT file. The text file is split into two parts: Transmission (TS) File -- first line in the file and Loan Application Register (LAR) -- all remaining lines of the file. Below are the links to the file specifications for data collected in years 2018 - current. 
- [Transmission  File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/v2/spec/2018_File_Spec_TS.csv)
- [Loan Application Register File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/v2/spec/2018_File_Spec_LAR.csv) 

## Technical Overview

This repository contains the code for the entirety of the public facing [HMDA Platform](http://ffiec.cfpb.gov/) backend. This platform has been designed to accommodate the needs of the HMDA filing process by financial institutions, as well as the data management, publication, aggregation, reporting, analyzing, visualizing, and downloading the HMDA data set. 

The HMDA Platform follows a loosely coupled [event driven](https://en.wikipedia.org/wiki/Event-driven_architecture) [micro-services architecture](https://en.wikipedia.org/wiki/Microservices) with API-first [(API Documentation)](https://cfpb.github.io/hmda-platform/#hmda-api-documentation) design principles. The entire platform is built on open source frameworks and remains cloud vendor agnostic. The code base contained in this repository includes the following micro-services that work together in support of the HMDA Platform.

- [Filing Platform](https://github.com/cfpb/hmda-platform/tree/master/hmda): The entire backend API for [public facing filing platform](https://ffiec.cfpb.gov/filing/2019/). This is used for processing the uploaded TXT files and validating them in a non-blocking I/O streaming way. The APIs are built to be able to process large (1.5M+ lines) and small text files simultaneously without impeding the scalability of the platform. This contains the code for customizable data edits, a [Domain Specific Language (DSL)](https://en.wikipedia.org/wiki/Domain-specific_language) for coding the data edits, and submitting events to Kafka topics.    

- [Check Digit](https://github.com/cfpb/hmda-platform/tree/master/check-digit): The entire backend API for [public facing check digit tool](https://ffiec.cfpb.gov/tools/check-digit). The check digit tool is used to 1) Generate a two character check-digit based on an Legal Entity Identifier (LEI) and 2) Validate that a check-digit is calculated correctly for any complete Universal Loan Identifier (ULI). The APIs are built to process multiple row CSV files as well as one time processing.  

- [Institutions API](https://github.com/cfpb/hmda-platform/tree/master/institutions-api): Read only API for fetching details about an LEI. This microservice also listens to events put on the `institutions-api` kafka topic for Creating, updating, and deleting institution data from PostgreSQL. 

- [Data Publisher](https://github.com/cfpb/hmda-platform/tree/master/hmda-data-publisher): This micro-service runs on a scheduled basis to make internal / external data available for research purposes on object stores such as S3. The schedule for the job is configurable via [K8s config map](https://github.com/cfpb/hmda-platform/blob/master/kubernetes/config-maps/schedule-configmap.yaml) 

- [hmda-dashboard](https://github.com/cfpb/hmda-platform/tree/master/hmda-dashboard): Authenticated APIs to view realtime analytics for the filings happening on the platform. The dashboard includes summarized statistics, data trends, and supports data visualizations via frontend. 

- [Ratespread](https://github.com/cfpb/hmda-platform/tree/master/ratespread-calculator): Public facing API for the [ratespread calculator](https://ffiec.cfpb.gov/tools/rate-spread). This calculator provides rate spreads for HMDA reportable loans with a final action date on or after January 1st, 2018. The API supports streaming CSV uploads as well as one-time calculations.

- [modified-lar](https://github.com/cfpb/hmda-platform/tree/master/modified-lar): Event driven service of [modified-lar reports](https://ffiec.cfpb.gov/data-publication/modified-lar/2019). Each time a filer successfully submits the data, the modified-lar micro-service generates a modified-lar report and puts it in the public object store (e.g. S3). Any re-submissions automatically re-generate new modified-lar reports.

- [irs-publisher](https://github.com/cfpb/hmda-platform/tree/master/irs-publisher): Event driven service of [irs-disclosure-reports](https://ffiec.cfpb.gov/data-publication/disclosure-reports/). Each time a filer successfully submits the data, the irs-publichser micro-service generates the IRS report.

- [hmda-reporting](https://github.com/cfpb/hmda-platform/tree/master/hmda-reporting): Real-time Public facing API for getting information (LEI number, institution name, and year) on LEIs who have successfully submitted their data.

- [hmda-analytics](https://github.com/cfpb/hmda-platform/tree/master/hmda-analytics): Event driven service to insert, delete, update information in PostgreSQL each time there is a successful submission. The data inserted maps with the Census data to provide information for MSAMds. It also adds race, sex, and ethnicity categorization to the data.

- [rate-limit](https://github.com/cfpb/hmda-platform/tree/master/rate-limit): Rate limiter service working in-sync with [ambassador](https://www.getambassador.io/docs/latest/topics/running/services/rate-limit-service/) to limit the number of times in a given time period that the API can be called. If the rate limit is reached, a 503 error code is sent. 

- [data-browser](https://github.com/cfpb/hmda-platform/tree/master/data-browser): Public facing API for [HMDA Data Browser](https://ffiec.cfpb.gov/data-browser/). This API makes the entire dataset available for summarized statistics, deep analysis, as well as geographic map layout. 

- [email-service](https://github.com/cfpb/hmda-platform/tree/master/email-service): Event driven service to send an automated email to the filer on each successful submission. 


## Technical Architecture

<Diagram with one paragraph explanation>

## One-line cloud deployment to dev/prod

The platform and all of the related microservices explained above are deployed on Kubernetes using Helm. Each deployment is a single Helm command. Below example shows the deployment for the email-service:

```
helm upgrade --install --force \                                                                                                                                                          
--namespace=default \
--values=kubernetes/email-service/values.yaml \
--set image.repository=hmda/email-service \
--set image.tag=<tag name> \
--set image.pullPolicy=Always \
email-service \
kubernetes/email-service
```

## One-line local setup of development environment

<docker compose setup>

## Automated Testing

The HMDA Platform takes a rigorous automated testing approach. We've prepared [Newman](https://github.com/cfpb/hmda-platform/tree/master/newman) test scripts that perform end-to-end testing of the APIs on a recurring basis. The testing process for Newman is containerized and runs as a K8s cron job. 

## Sprint Cadence

Our team works in two week sprints. The sprints are managed as [Project Boards](https://github.com/cfpb/hmda-platform/projects). The backlog grooming happens every two weeks as part of Sprint Planning and Sprint Retrospectives.

## Development Process

Below are the steps the development team follows to fix issues, develop new features, etc. 

1. Create a fork of this repository
2. Work in a branch of the fork
3. Create a PR to merge into master
4. The PR is automatically built, tested, and linted using: Travis, Snyk, and CodeCov
5. Manual review is performed in addition to ensuring the above automatic scans are positive
6. The PR is deployed to development servers to be checked using Newman
7. The PR is merged only by a separate member in the dev team

## Postman Collection

We've created a [HMDA Postman](https://github.com/cfpb/hmda-platform/tree/master/newman/postman) collection that makes it easier to do an end-to-end filing of the HMDA Data including upload, parsing data, flagging edits, resolving edits, and submitting data when S/V edits are resolved. 

## Contributing

`CFPB` is developing the `HMDA Platform` in the open to maximize transparency and encourage third party contributions. If you want to contribute, please read and abide by the terms of the [License](LICENSE) for this project.

We use GitHub issues in this repository to track features, bugs, and enhancements to the software. [Pull Requests](https://help.github.com/articles/using-pull-requests/) are welcome

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)

## Credits and references

Related projects
  - https://github.com/cfpb/hmda-frontend - ReactJS Front-end repository powering the [HMDA Platform](http://ffiec.cfpb.gov/)
  - https://github.com/cfpb/hmda-help - ReactJS Front-end repository powering HMDA Help - used to resolve and troubleshoot issues in filing
  - https://github.com/cfpb/hmda-platform-auth
  - https://github.com/cfpb/hmda-platform-larft - Repo for the [Public Facing LAR formatting tool](https://ffiec.cfpb.gov/tools/lar-formatting)
  - https://github.com/cfpb/hmda-test-files - Repo for automatically generating various different test files for HMDA Data
  - https://github.com/cfpb/hmda-census - ETL for geographic and Census data used by the HMDA Platform
  - https://github.com/cfpb/hmda-platform-api-docs - Repo for [Public facing API Documentation](https://cfpb.github.io/hmda-platform/#hmda-api-documentation). This gets deployed to GH Pages using the [`gh-pages`](https://github.com/cfpb/hmda-platform/tree/gh-pages) branch
  - https://github.com/cfpb/HMDA_Data_Science_Kit - Repo for HMDA Data science work as well as Spark codebase for [Public Facing A&D Reports](https://ffiec.cfpb.gov/data-publication/disclosure-reports/2018)