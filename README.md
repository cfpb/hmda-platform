
# HMDA Platform

## This project is a work in progress

Information contained in this respository should be considered provisional and a work in progress, and not the final implementation for the HMDA Platform, unless otherwise indicated. 

## Introduction

The Home Mortgage Disclosure Act (HMDA) requires many financial institutions to maintain, report, and publicly disclose information about mortgages. HMDA was originally enacted by Congress in 1975 and is implemented by [Regulation C](https://www.gpo.gov/fdsys/pkg/CFR-2012-title12-vol8/xml/CFR-2012-title12-vol8-part1003.xml). The Dodd-Frank Act transferred HMDA rulemaking authority from the Federal Reserve Board to the Consumer Financial Protection Bureau (CFPB) on July 21, 2011.

This regulation provides the public loan data that can be used to assist:
* in determining whether financial institutions are serving the housing needs of their communities;
* public officials in distributing public-sector investments so as to attract private investment to areas where it is needed;
* and in identifying possible discriminatory lending patterns.

This regulation applies to certain financial institutions, including banks, savings associations, credit unions, and other mortgage lending institutions.

## The HMDA Platform

This repository contains the code for the entirety of the HMDA platform backend. This platform has been designed to accomodate the needs of the HMDA filing process by financial institutions, as well as the data management and publication needs of the HMDA data asset. 

The HMDA Platform is composed of the following modules: 

### Parser

Component responsible for reading incoming data and making sure that it conforms to the HMDA File Specification

### Data Validation

Module responsible for validating incoming data by executing validation rules as per the Edit Checks documentation

### Persistence

This module is responsible for persisting information into the system. It becomes the system of record for HMDA data

### API

This module contains both public APIs for HMDA data for general use by third party clients and web applications, as well as endpoints for receiving data and providing information about the filing process for Financial Institutions


## Contributing

CFPB is developing the HMDA Platform in the open to maximize transparency and encourage third party contributions. If you want to contribute, please read and abide by the terms of the [License](LICENSE) for this project.

We use GitHub issues in this repository to track features, bugs, and enhancements to the software. [Pull Requests](https://help.github.com/articles/using-pull-requests/) are welcome



----

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)

