[![Build Status](https://travis-ci.org/cfpb/hmda-platform.svg?branch=master)](https://travis-ci.org/cfpb/hmda-platform) [![codecov.io](https://codecov.io/github/cfpb/hmda-platform/coverage.svg?branch=master)](https://codecov.io/github/cfpb/hmda-platform?branch=master)

# HMDA Platform

## This project is a work in progress

Information contained in this repository should be considered provisional and a work in progress, and not the final implementation for the HMDA Platform, unless otherwise indicated.

## Introduction

For more information on HMDA, checkout the [About HMDA page](http://www.consumerfinance.gov/data-research/hmda/learn-more) on the CFPB website.

## The HMDA Platform

This repository contains the code for the entirety of the HMDA platform backend. This platform has been designed to accommodate the needs of the HMDA filing process by financial institutions, as well as the data management and publication needs of the HMDA data asset.

The HMDA Platform is composed of the following modules:

### Parser (JS/JVM)

Module responsible for reading incoming data and making sure that it conforms to the HMDA File Specification

### Data Validation

Module responsible for validating incoming data by executing validation rules as per the Edit Checks documentation

### Persistence

Module responsible for persisting information into the system. It becomes the system of record for HMDA data

### API

This module contains both public APIs for HMDA data for general use by third party clients and web applications, as well as endpoints for receiving data and providing information about the filing process for Financial Institutions

### Query

This module is responsible for interacting with the back-end database, as well as conversion between model objects and database objects.

### Panel

This module is responsible for parsing and persisting a CSV-format panel file

### Model (JS/JVM)

This module is responsible for maintaining the objects used in our platform

### Census

This module is responsible for geographic translation (e.g. state number -> state code)

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

* The write side of this system is supported by either a local `leveldb` database or Cassandra. By default, the local `leveldb` is utilized, and some sample data is loaded automatically.
If using `Cassandra` is desired, the following environment variable needs to be set:

```shell
export HDMA_IS_DEMO=false
```

The easiest way to run a Cassandra server to support this application for testing is to do it through Docker:

```shell
docker run --name cassandra -p 9042:9042 -p 7000:7000 -p 7199:7199 cassandra:3.10
```

If you want to connect to this server, the following `docker` command will give you access to the Cassandra instance started in the previous step:

```shell
docker run -it --link cassandra:cassandra --rm cassandra cqlsh cassandra
```

Once the `Cassandra` server is running, set the following environment variable to the appropriate Cassandra host (in this example, the default local docker host for a machine running MacOs X):

```shell
export CASSANDRA_CLUSTER_HOSTS=192.168.99.100
```

To load data into `Cassandra`, you can run the following (the Cassandra server needs to be running and correct environment variables configured as per the previous instructions):

```shell
$ sbt
project panel
run <full local path to sample file>
```
A sample file is located in the following folder: `hmda-platform/persistence/src/main/resources/demoInstitutions.csv`


* In order to support the read side, a local PostgreSQL server is needed. Assuming it runs on the default port, on the same machine as the API, the following environment variable needs to be set:

```shell
export JDBC_URL='jdbc:postgresql://localhost/hmda?user=postgres&password=postgres'
```

where `hmda` is the name of the `PostgreSQL` database, owned by the default user with default password (`postgres`)

**Note: if you are running the backend only through sbt, the database needs to be created manually in advance, see instructions [here](https://www.postgresql.org/docs/9.1/static/manage-ag-createdb.html)**

* Start `sbt`

```shell
$ sbt
```

* Select project to build and run.This will retrieve all necessary dependencies, compile Scala source, and start a local server. It also listens for changes to underlying source code, and auto-deploys to local server.

```shell

> project api
> clean
> ~re-start
```

Confirm that the platform is up and running by browsing to http://localhost:8080

* To build JVM artifacts (the default, includes all projects), from the sbt prompt:

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
docker run -d -p "8080:8080 -p 8082:8082" hmda-api
```

The Filing API will run on `$(docker-machine ip):8080`
The Public API will run on `$(docker-machine ip):8082`

#### To run the entire platform
Clone the [HMDA Platform UI](https://github.com/cfpb/hmda-platform-ui) repo and the [HMDA Platform Auth](https://github.com/cfpb/hmda-platform-auth) repo into sibling directories of this one. Your directory structure should look like this:
```shell
~/dev/hmda-project$ ls -la
total 16
drwxr-xr-x   6 lortone  staff   204B Jul 25 17:44 ./
drwxr-xr-x   9 lortone  staff   306B Jul 25 17:50 ../
drwxr-xr-x  22 lortone  staff   748B Jul 27 16:28 hmda-platform/
drwxr-xr-x  25 lortone  staff   850B Jul 25 17:13 hmda-platform-ui/
drwxr-xr-x  23 lortone  staff   796B Jul 28 17:15 hmda-platform-auth/
```

From the _`hmda-platform-ui`'s_ root directory, run `yarn`. (Get yarn [here](https://yarnpkg.com/lang/en/docs/install/) if you don't have it installed.)

From _`hmda-platform`'s_ root directory, run the following:

```shell
sbt clean assembly
docker-compose up
```

This will bring up all the HMDA Platform services. The first run may take several minutes.

Next, find your docker machine's endpoint.

```shell
# Typically defaults to 192.168.99.100, which will be used in the following examples
docker-machine ip dev
```

Then, visit the following URLS and click advanced -> proceed. This will bypass self-signed cert errors from your browser when running the app.

- https://192.168.99.100:8443/
- https://192.168.99.100:4443/
- https://192.168.99.100:9443/

Visit the app at http://192.168.99.100, click the "Login" button, and click "Register" when redirected to the keycloak login screen.

To use demo data there are two institutions available; Bank 0 and Bank 1. To register for either of these institutions you have to use the corresponding domain:

- Bank 0 = bank0.com
- Bank 1 = bank1.com

Confirm your signup via MailDev by visiting http://192.168.99.100:1080, opening the email, and clicking the verifying link.

You can now interact with the app/begin uploading files, etc.

#### Development conveniences

##### Mounted volumes

For convenience when doing development on the UI, Auth setup, and API, the `docker-compose` file uses a `volumes` which mounts

- the ui's `dist/` directory into the `hmda-platform-ui` container,
- the `hmda.jar` into `hmda-platform` container,
- and the `hmda` themes directory in the auth repo into the `keycloak` container.

This means you can make changes to the UI, Keycloak theme, or API and (in most cases) view them without needing to rebuild their respective containers.

In order to view changes in the API you need to rebuild the jar and then restart the container:

```shell
# from the hmda-platform directory
sbt clean assembly
docker-compose stop
docker-compose up
```

To allow continued rebuilding of the front-end, you can run the following:

```shell
# from the hmda-platform-ui directory
npm run watch
```

## Contributing

CFPB is developing the HMDA Platform in the open to maximize transparency and encourage third party contributions. If you want to contribute, please read and abide by the terms of the [License](LICENSE) for this project.

We use GitHub issues in this repository to track features, bugs, and enhancements to the software. [Pull Requests](https://help.github.com/articles/using-pull-requests/) are welcome

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)

## Credits and references

1. Projects that inspired you
  - https://github.com/cfpb/hmda-pilot
2. Related projects
  - https://github.com/cfpb/hmda-platform-ui
  - https://github.com/cfpb/hmda-platform-auth
