# HMDA Platform

## Introduction

For more information on HMDA, checkout the [About HMDA page](http://www.consumerfinance.gov/data-research/hmda/learn-more) on the CFPB website.

## The HMDA Platform

The new HMDA Platform (“V2”) was totally updated over the past year to utilize a more flexible and resilient approach (Kubernetes Microservices) in order to better serve our customers. This repository contains the code for the entirety of the HMDA platform backend. This platform has been designed to accommodate the needs of the HMDA filing process by financial institutions, as well as the data management and publication needs of the HMDA data asset.

The HMDA Platform uses sbt's multi-project builds, each project representing a specific task. The platform is an Akka Cluster application that can be deployed on a single node or as a distributed application. For more information on how Akka Cluster is used, see the documentation [here](Documents/cluster.md)

The HMDA Platform is composed of the following modules:

### Parser (JS/JVM)

Module responsible for reading incoming data and making sure that it conforms to the HMDA File Specification

### Data Validation

Module responsible for validating incoming data by executing validation rules as per the Edit Checks documentation

### Persistence

Module responsible for persisting information into the system. It becomes the system of record for HMDA data

### Cluster

Module responsible for managing the various cluster roles, as well as starting the Hmda Platform

### API

This module contains both public APIs for HMDA data for general use by third party clients and web applications, as well as endpoints for receiving data and providing information about the filing process for Financial Institutions

### API Model

This module contains objects and JSON protocols for use by the API project

### Query

This module is responsible for interacting with the back-end database, as well as conversion between model objects and database objects.

### Panel

This module is responsible for parsing and persisting a CSV-format panel file

### Model (JS/JVM)

This module is responsible for maintaining the objects used in our platform

### Census

This module is responsible for geographic translation (e.g. state number -> state code)

### Publication

This module generates Aggregate and Disclosure reports, as required by HMDA statute.

## Dependencies


## Dependencies

### Java 19 SDK

The HMDA Platform runs on the Java Virtual Machine (JVM), and requires the Java 19 JDK to build and run the project. This project is currently being built and tested on [Oracle JDK 19](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html). See [Oracle's JDK Install Overview](https://docs.oracle.com/en/java/javase/19/install/index.html) for install instructions.

The HMDA Platform should also run on JDK 8.

### Scala

The HMDA Platform is written in [Scala](http://www.scala-lang.org/). To build it, you will need to [download](http://www.scala-lang.org/download/) and [install](http://www.scala-lang.org/download/install.html) Scala 2.12.x

In addition, you'll need Scala's interactive build tool [sbt](https://www.scala-sbt.org/). Please refer to sbt's [installation instructions](https://www.scala-sbt.org/1.x/docs/Setup.html) to get started.

## Project structure

The HMDA Platform is divided into individual projects, each responsible for a subset of the functionality, as follows:

### hmda-platform

This is the main filing application, exposing the APIs necessary to upload, validate and store HMDA files. 

### check-digit 

Microservice that exposes functionality to create a check digit from a loan id, and to validate `Univeral Loan Identifiers` 


## Building and Running

### Running from the SBT prompt

* To run the project from the `SBT` prompt for development purposes, issue the following commands on a terminal:

```shell
$ sbt
sbt:root> project hmda-platform
sbt:hmda-platform> reStart
```


### Building and running the .jar

* To build JVM artifacts, from the sbt prompt first choose the project you want to build and use the assembly command:

```shell
$ sbt
sbt:root> project check-digit
sbt:check-digit>assembly
```
This task will create a `fat jar`, which can be executed on any `JDK9` compliant `JVM`:

`java -jar target/scala-2.12/check-digit.jar`

### Building and running the Docker image

* To build a `Docker` image that runs the `hmda-platform` as a single node cluster, from the sbt prompt:

```shell
$sbt
sbt:root> project hmda-platform
sbt:hmda-platform> docker:publishLocal
```
This task will create a `Docker` image. To run a container with the `HMDA Platform` filing application as a single node cluster:

`docker run -e CASSANDRA_CLUSTER_HOSTS=localhost --rm -ti -p 8080:8080 -p 8081:8081 -p 8082:8082 -p 19999:19999 hmda/hmda-platform` 

The same approach can be followed to build and run Docker containers for the other microservices that form the HMDA Platform. 

Certain environment variables can be passed in to set the log level of the micro service

```
ZOOKEEPER_LOG_LEVEL (Defaulted to WARN)
KAFKA_LOG_LEVEL (Defaulted to INFO)
CASSANDRA_LOG_LEVEL (Defaulted to INFO)
PLATFORM_LOG_LEVEL (Defaulted to WARN)
INSTITUTION_LOG_LEVEL (Defaulted to INFO)
CHECKDIGIT_LOG_LEVEL (Defaulted to DEBUG)
```

## Resources

### API Documentation

* [HMDA Platform Public API Documentation](api/public-api.md)
* [HMDA Platform ULI API Documentation](api/uli.md)
* [HMDA Platform Filers API Documentation](api/filers-api.md)

### Development

* [Local Kubernetes CI/CD](development/kubernetes.md)

### Data Specifications

* [TS File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/spec/Public_File_TS_Spec.csv)
* [LAR File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/spec/Public_File_LAR_Spec.csv)
* [Institution Data Model Spec](spec/2018_Institution_Data_Model_Spec.csv)

