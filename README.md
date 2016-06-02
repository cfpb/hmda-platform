[![Build Status](https://travis-ci.org/cfpb/hmda-platform.svg?branch=master)](https://travis-ci.org/cfpb/hmda-platform) [![codecov.io](https://codecov.io/github/cfpb/hmda-platform/coverage.svg?branch=master)](https://codecov.io/github/cfpb/hmda-platform?branch=master)

# HMDA Platform

## This project is a work in progress

Information contained in this repository should be considered provisional and a work in progress, and not the final implementation for the HMDA Platform, unless otherwise indicated.

## Introduction

The Home Mortgage Disclosure Act (HMDA) requires many financial institutions to maintain, report, and publicly disclose information about mortgages. HMDA was originally enacted by Congress in 1975 and is implemented by [Regulation C](https://www.gpo.gov/fdsys/pkg/CFR-2012-title12-vol8/xml/CFR-2012-title12-vol8-part1003.xml). The Dodd-Frank Act transferred HMDA rulemaking authority from the Federal Reserve Board to the Consumer Financial Protection Bureau (CFPB) on July 21, 2011.

This regulation provides the public loan data that can be used to assist:
* in determining whether financial institutions are serving the housing needs of their communities;
* public officials in distributing public-sector investments so as to attract private investment to areas where it is needed;
* and in identifying possible discriminatory lending patterns.

This regulation applies to certain financial institutions, including banks, savings associations, credit unions, and other mortgage lending institutions.

## The HMDA Platform

This repository contains the code for the entirety of the HMDA platform backend. This platform has been designed to accommodate the needs of the HMDA filing process by financial institutions, as well as the data management and publication needs of the HMDA data asset.

The HMDA Platform is composed of the following modules:

### Parser

Module responsible for reading incoming data and making sure that it conforms to the HMDA File Specification

### Data Validation

Module responsible for validating incoming data by executing validation rules as per the Edit Checks documentation

### Persistence

Module responsible for persisting information into the system. It becomes the system of record for HMDA data

### API

This module contains both public APIs for HMDA data for general use by third party clients and web applications, as well as endpoints for receiving data and providing information about the filing process for Financial Institutions


## Dependencies

### Java 8 SDK

The HMDA Platform runs on the Java Virtual Machine (JVM), and requires the Java 8 JDK to build and run the project. This project is currently being built and tested on [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). See [Oracle's JDK Install Overview](http://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) for install instructions.

The HMDA Platform should also run on OpenJDK 8.

### Scala

The HMDA Platform is written in [Scala](http://www.scala-lang.org/). To build it, you will need to [download](http://www.scala-lang.org/download/) and [install](http://www.scala-lang.org/download/install.html) Scala 2.11.x

In addition, you'll need Scala's interactive build tool [sbt](http://www.scala-sbt.org/0.13/tutorial/index.html). Please refer to sbt's [installation instructions](http://www.scala-sbt.org/0.13/tutorial/Setup.html) to get started.

## Building and Running

The HMDA Platform uses sbt's multi-project builds, each project representing a specific task.

### Interactive

1. Start `sbt`

```shell
$ sbt
```

2. Select project to build and run.This will retrieve all necessary dependencies, compile Scala source, and start a local server. It also listens for changes to underlying source code, and auto-deploys to local server.

```shell
> projects
[info] In file:/Users/marinj/Development/hmda-platform/
[info]     api
[info]   * hmda
[info]     model
[info]     parser
[info]     platformTest
[info]     validation

> ~re-start
```

Confirm that the platform is up and running by browsing to http://localhost:8080

3. To build JVM artifacts (the default, includes all projects), from the sbt prompt:

```shell
> assembly
```

This task will create a `fat jar`, which can be executed directly on any JDK8 compliant JVM:

```shell
java -jar target/scala-2.11/hmda.jar
```


### Docker

First, make sure that you have the [Docker Toolbox](https://www.docker.com/docker-toolbox) installed.

If you don't have a Docker machine created, you can create one by issuing the following:

```shell
docker-machine create --driver virtualbox dev
```

After the machine is created, make sure that you connect your shell with the newly created machine

```shell
$ eval "(docker-machine env dev)"
```

From the project's root directory, run the following:

```shell
docker-compose up -d
```

This will bring up all the HMDA Platform services (the first run might take a while). You can navigate to the HMDA HTTP API by
going to the docker machine's endpoint. To find your docker machine endpoint:

```shell
docker-machine ip dev
```

The HMDA Platform will be available at `<dev-ip>:8080`


## Contributing

CFPB is developing the HMDA Platform in the open to maximize transparency and encourage third party contributions. If you want to contribute, please read and abide by the terms of the [License](LICENSE) for this project.

We use GitHub issues in this repository to track features, bugs, and enhancements to the software. [Pull Requests](https://help.github.com/articles/using-pull-requests/) are welcome



----

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)

