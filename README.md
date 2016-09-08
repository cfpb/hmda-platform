[![Build Status](https://travis-ci.org/cfpb/hmda-platform.svg?branch=master)](https://travis-ci.org/cfpb/hmda-platform) [![codecov.io](https://codecov.io/github/cfpb/hmda-platform/coverage.svg?branch=master)](https://codecov.io/github/cfpb/hmda-platform?branch=master)

# HMDA Platform

## This project is a work in progress

Information contained in this repository should be considered provisional and a work in progress, and not the final implementation for the HMDA Platform, unless otherwise indicated.

## Introduction

For more information on HMDA, checkout the [About HMDA page](http://www.consumerfinance.gov/data-research/hmda/learn-more) on the CFPB website.

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
> clean assembly
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

Ensure there's a compiled jar to create the Docker image with:
```shell
sbt clean assembly
```
#### To run only the API

Build the docker image
```shell
docker build -t hmda-api .
```

Then, run the docker image
```shell
docker run -d -p "8080:8080" hmda-api
```

The API will run on `$(docker-machine ip):8080`

#### To run the entire platform
Clone the [HMDA Platform UI](https://github.com/cfpb/hmda-platform-ui) directory into a sibling directory of this one. Your directory structure should look like this:
```shell
~/dev/hmda-project$ ls -la
total 16
drwxr-xr-x   6 lortone  staff   204B Jul 25 17:44 ./
drwxr-xr-x   9 lortone  staff   306B Jul 25 17:50 ../
drwxr-xr-x  22 lortone  staff   748B Jul 27 16:28 hmda-platform/
drwxr-xr-x  25 lortone  staff   850B Jul 25 17:13 hmda-platform-ui/
```

From `hmda-platform`'s root directory, run the following:

```shell
docker-compose up -d --build
```

This will bring up all the HMDA Platform services. The first run may take several minutes.

For convenience when doing development on the UI, the `docker-compose` file uses a `volumes` which mount the local directory into the `hmda-platform-ui` container. This means you can make UI changes and refresh the browser to view them.

To build the front-end and allow "watching" for changes you can run:

``` shell
# while still in the hmda-platform directory
cd ../hmda-platform-ui
npm install # optional, to make sure you get the dependencies
npm run watch
```

If you don't need to "watch" for changes you can run:

``` shell
# while still in the hmda-platform directory
cd ../hmda-platform-ui
npm install # optional, to make sure you get the dependencies
npm run build
```

This will simply build the front-end, still taking advantage of the mounted volume.

View the app by visiting your docker machine's endpoint in the browser.
To find your docker machine endpoint:

```shell
docker-machine ip dev
```

## Contributing

CFPB is developing the HMDA Platform in the open to maximize transparency and encourage third party contributions. If you want to contribute, please read and abide by the terms of the [License](LICENSE) for this project.

We use GitHub issues in this repository to track features, bugs, and enhancements to the software. [Pull Requests](https://help.github.com/articles/using-pull-requests/) are welcome



----

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)
