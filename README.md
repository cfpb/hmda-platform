[![Build Status](https://travis-ci.org/cfpb/hmda-platform.svg?branch=master)](https://travis-ci.org/cfpb/hmda-platform) [![codecov.io](https://codecov.io/github/cfpb/hmda-platform/coverage.svg?branch=master)](https://codecov.io/github/cfpb/hmda-platform?branch=master)

# HMDA Platform

## Introduction

The Home Mortgage Disclosure Act (HMDA) Platform is a [Regulatory technology](https://en.wikipedia.org/wiki/Regulatory_technology) application for financial institutions to submit mortgage information as described in the [Filing Instruction Guide (FIG)](https://s3.amazonaws.com/cfpb-hmda-public/prod/help/2020-hmda-fig.pdf). The HMDA-Platform parses data as submitted by mortgage leading institutions and validates the information for edits (Syntactical, Validity, Quality, and Macro as-per the instructions in the FIG) before submitting the data. The HMDA-Platform supports [quarterly](https://ffiec.cfpb.gov/documentation/2020/quarterly-filing-dates/) and [yearly](https://ffiec.cfpb.gov/documentation/2019/annual-filing-dates/) filing periods. For detailed information on Home Mortgage Disclosure Act (HMDA), checkout the [About HMDA page](https://www.consumerfinance.gov/policy-compliance/rulemaking/final-rules/regulation-c-home-mortgage-disclosure-act/) on the CFPB website.

[Please watch this short video](https://youtu.be/C_73Swgyc4g) to view how HMDA Platform transforms the data upload, validation, and submission process.

## TS and LAR File Specs

The data is submitted in a flat pipe (`|`) delimited TXT file. The text file is split into two parts: Transmission (TS) File -- first line in the file and Loan Application Register (LAR) -- all remaining lines of the file. Below are the links to the file specifications for data collected in years 2018 - current.
- [Transmission  File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/v2/spec/2018_File_Spec_TS.csv)
- [Loan Application Register File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/v2/spec/2018_File_Spec_LAR.csv)

## Technical Overview

This repository contains the code for the entirety of the public facing [HMDA Platform](http://ffiec.cfpb.gov/) backend. This platform has been designed to accommodate the needs of the HMDA filing process by financial institutions, as well as the data management, publication, aggregation, reporting, analyzing, visualizing, and downloading the HMDA data set.

The HMDA Platform follows a loosely coupled [event driven](https://en.wikipedia.org/wiki/Event-driven_architecture) [micro-services architecture](https://en.wikipedia.org/wiki/Microservices) with API-first [(API Documentation)](https://cfpb.github.io/hmda-platform/#hmda-api-documentation) design principles. The entire platform is built on open source frameworks and remains cloud vendor agnostic.

### Microservices

The code base contained in this repository includes the following microservices that work together in support of the HMDA Platform.

- [HMDA Platform](https://github.com/cfpb/hmda-platform/tree/master/hmda): The entire backend API for [public facing filing platform](https://ffiec.cfpb.gov/filing/2019/). Used for processing the uploaded TXT files and validating them in a non-blocking, I/O streaming way. The APIs are built to be able to process various file sizes, from small (few lines) to large (1.5M+ lines), text files simultaneously without impeding the scalability or availability of the platform. The platform contains code for customizable data edits, a [Domain Specific Language (DSL)](https://en.wikipedia.org/wiki/Domain-specific_language) for coding the data edits, and submitting events to Kafka topics.    

- [Check Digit](https://github.com/cfpb/hmda-platform/tree/master/check-digit): The entire backend API for [public facing check digit tool](https://ffiec.cfpb.gov/tools/check-digit). The Check Digit tool is used to (1) Generate a two character check-digit based on an Legal Entity Identifier (LEI) and (2) Validate that a check-digit is calculated correctly for any complete Universal Loan Identifier (ULI). This APIs are built to process multiple row CSV files as well as one time processing.

- [Institutions API](https://github.com/cfpb/hmda-platform/tree/master/institutions-api): Read only API for fetching details about an LEI. This microservice also listens to events put on the `institutions-api` Kafka topic for Creating, updating, and deleting institution data from PostgreSQL.

- [Data Publisher](https://github.com/cfpb/hmda-platform/tree/master/hmda-data-publisher): This microservice runs on a scheduled basis to make internal / external data available for research purposes via object stores such as S3. The schedule for the job is configurable via [K8s config map](https://github.com/cfpb/hmda-platform/blob/master/kubernetes/config-maps/schedule-configmap.yaml)

- [Ratespread](https://github.com/cfpb/hmda-platform/tree/master/ratespread-calculator): Public facing API for the [ratespread calculator](https://ffiec.cfpb.gov/tools/rate-spread). This calculator provides rate spreads for HMDA reportable loans with a final action date on or after January 1st, 2018. This API supports streaming CSV uploads as well as one-time calculations.

- [Modified LAR](https://github.com/cfpb/hmda-platform/tree/master/modified-lar): Event driven service of [modified-lar reports](https://ffiec.cfpb.gov/data-publication/modified-lar/2019). Each time a filer successfully submits the data, the modified-lar micro-service generates a modified-lar report and puts it in the public object store (e.g. S3). Any re-submissions automatically re-generate new modified-lar reports.

- [IRS Publisher](https://github.com/cfpb/hmda-platform/tree/master/irs-publisher): Event driven service of [irs-disclosure-reports](https://ffiec.cfpb.gov/data-publication/disclosure-reports/). Each time a filer successfully submits the data, the irs-publisher microservice generates the IRS report.

- [HMDA Reporting](https://github.com/cfpb/hmda-platform/tree/master/hmda-reporting): Real-time, public facing API for getting information (LEI number, institution name, and year) on LEIs who have successfully submitted their data.

- [HMDA Analytics](https://github.com/cfpb/hmda-platform/tree/master/hmda-analytics): Event driven service to insert, delete, update information in PostgreSQL each time there is a successful submission. The data inserted maps with the Census data to provide information for MSAMds. It also adds race, sex, and ethnicity categorization to the data.

- [HMDA Dashboard](https://github.com/cfpb/hmda-platform/tree/master/hmda-dashboard): Authenticated APIs to view realtime analytics for the filings happening on the platform. The dashboard includes summarized statistics, data trends, and supports data visualizations via frontend.

- [Rate imit](https://github.com/cfpb/hmda-platform/tree/master/rate-limit): Rate limiter service working in-sync with [ambassador](https://www.getambassador.io/docs/latest/topics/running/services/rate-limit-service/) to limit the number of times in a given time period that the API can be called. If the rate limit is reached, a 503 error code is sent.

- [HMDA Data Browser](https://github.com/cfpb/hmda-platform/tree/master/data-browser): Public facing API for [HMDA Data Browser](https://ffiec.cfpb.gov/data-browser/). This API makes the entire dataset available for summarized statistics, deep analysis, as well as geographic map layout.

- [Email Service](https://github.com/cfpb/hmda-platform/tree/master/email-service): Event driven service to send an automated email to the filer on each successful submission.


## HMDA Platform Technical Architecture

The image below shows the cloud vendor agnostic technical architecture for the HMDA Platform.

<a href="diagrams/hmda_platform_diagram.png"><img src="diagrams/hmda_platform_diagram.png" style="border: 2px solid #000;" /></a>

## HMDA Data Browser Technical Architecture

The image below shows the cloud vendor agnostic technical architecture for the HMDA Data Browser.

<a href="diagrams/hmda_data_browser.png"><img src="diagrams/hmda_data_browser.png" style="border: 2px solid #000;" /></a>

## Running with sbt

The HMDA Platform can run locally using [`sbt`](https://www.scala-sbt.org/) with an [embedded Cassandra](https://doc.akka.io/docs/alpakka-kafka/current/) and [embedded Kafka](https://doc.akka.io/docs/alpakka-kafka/current/). To get started:

```bash
git clone https://github.com/cfpb/hmda-platform.git
cd hmda-platform
export CASSANDRA_CLUSTER_HOSTS=localhost
sbt
[...]
sbt:hmda-root> project hmda-platform
sbt:hmda-platform> reStart

```

## One-line Cloud Deployment to Dev/Prod

The platform and all of the related microservices explained above are deployed on [Kubernetes](https://kubernetes.io/) using [Helm](https://helm.sh/). Each deployment is a single Helm command. Below is an example for the deployment of the email-service:

```shell
helm upgrade --install --force \                                        
--namespace=default \
--values=kubernetes/email-service/values.yaml \
--set image.repository=hmda/email-service \
--set image.tag=<tag name> \
--set image.pullPolicy=Always \
email-service \
kubernetes/email-service
```

## One-line Local Development Environment

The platform and it's dependency services, Kafka, Cassandra and PostgreSQL, can run locally using [Docker Compose](https://docs.docker.com/compose/).

```shell
# Bring up hmda-platform, hmda-analytics, institutions-api
docker-compose up

# Bring up the hmda-platform
docker-compose up hmda-platform
```

## Automated Testing

The HMDA Platform takes a rigorous automated testing approach. In addtion to Travis and CodeCov, we've prepared a suite of [Newman](https://github.com/cfpb/hmda-platform/tree/master/newman) test scripts that perform end-to-end testing of the APIs on a recurring basis. The testing process for Newman is containerized and runs as a Kubernetes CronJob to act as a monitoring and alerting system.

## Postman Collection

In addition to using Newman for our internal testing, we've created a [HMDA Postman](https://github.com/cfpb/hmda-platform/tree/master/newman/postman) collection that makes it easier for  users to perform a end-to-end filing of HMDA Data, including upload, parsing data, flagging edits, resolving edits, and submitting data when S/V edits are resolved.

## API Documentation

The [HMDA Platform Public API Documentation](https://cfpb.github.io/hmda-platform/#hmda-api-documentation) is hosted in the [HMDA Platform API Docs repo](https://github.com/cfpb/hmda-platform-api-docs) and deployed to GitHub Pages using the [`gh-pages`](https://github.com/cfpb/hmda-platform/tree/gh-pages) branch.


## Sprint Cadence

Our team works in two week sprints. The sprints are managed as [Project Boards](https://github.com/cfpb/hmda-platform/projects). The backlog grooming happens every two weeks as part of Sprint Planning and Sprint Retrospectives.

## Code Formatting

Our team uses [Scalafmt](https://scalameta.org/scalafmt/) to format our codebase. 

## Development Process

Below are the steps the development team follows to fix issues, develop new features, etc.

1. Create a fork of this repository
2. Work in a branch of the fork
3. Create a PR to merge into master
4. The PR is automatically built, tested, and linted using: Travis, Snyk, and CodeCov
5. Manual review is performed in addition to ensuring the above automatic scans are positive
6. The PR is deployed to development servers to be checked using Newman
7. The PR is merged only by a separate member in the dev team

## Contributing

[`CFPB`](https://www.consumerfinance.gov/) is developing the `HMDA Platform` in the open to maximize transparency and encourage third party contributions. If you want to contribute, please read and abide by the terms of the [License](LICENSE) for this project. [Pull Requests](https://help.github.com/articles/using-pull-requests/) are always welcome.

## Issues

We use GitHub issues in this repository to track features, bugs, and enhancements to the software.

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)

## Credits and references

Related projects
  - https://github.com/cfpb/hmda-frontend - ReactJS Front-end repository powering the [HMDA Platform](http://ffiec.cfpb.gov/)
  - https://github.com/cfpb/hmda-help - ReactJS Front-end repository powering HMDA Help - used to resolve and troubleshoot issues in filing
  - https://github.com/cfpb/hmda-platform-larft - Repo for the [Public Facing LAR formatting tool](https://ffiec.cfpb.gov/tools/lar-formatting)
  - https://github.com/cfpb/hmda-test-files - Repo for automatically generating various different test files for HMDA Data
  - https://github.com/cfpb/hmda-census - ETL for geographic and Census data used by the HMDA Platform
  - https://github.com/cfpb/HMDA_Data_Science_Kit - Repo for HMDA Data science work as well as Spark codebase for [Public Facing A&D Reports](https://ffiec.cfpb.gov/data-publication/disclosure-reports/2018)
