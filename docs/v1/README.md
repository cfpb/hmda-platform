# HMDA Platform

## Introduction

For more information on HMDA, checkout the [About HMDA page](http://www.consumerfinance.gov/data-research/hmda/learn-more) on the CFPB website.

## The HMDA Platform

This repository contains the code for the entirety of the HMDA platform backend. This platform has been designed to accommodate the needs of the HMDA filing process by financial institutions, as well as the data management and publication needs of the HMDA data asset.

The HMDA Platform uses sbt's multi-project builds, each project representing a specific task. The platform is an Akka Cluster
application that can be deployed on a single node or as a distributed application. For more information on how Akka Cluster
is used, see the documentation [here](Documents/cluster.md)

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

### Java 8 SDK

The HMDA Platform runs on the Java Virtual Machine (JVM), and requires the Java 8 JDK to build and run the project. This project is currently being built and tested on [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). See [Oracle's JDK Install Overview](http://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) for install instructions.

The HMDA Platform should also run on OpenJDK 8.

### Scala

The HMDA Platform is written in [Scala](http://www.scala-lang.org/). To build it, you will need to [download](http://www.scala-lang.org/download/) and [install](http://www.scala-lang.org/download/install.html) Scala 2.11.x

In addition, you'll need Scala's interactive build tool [sbt](http://www.scala-sbt.org/0.13/tutorial/index.html). Please refer to sbt's [installation instructions](http://www.scala-sbt.org/0.13/tutorial/Setup.html) to get started.

### Docker

Though Docker is not a dependency of the Scala project, it is very useful for running and smoke testing locally.
Use the following steps to prepare a local environment for running the Platform with docker:

First, make sure that you have the [Docker Toolbox](https://www.docker.com/docker-toolbox) installed.

If you don't have a Docker machine created, you can create one with the default parameters using the command below.
This will be sufficient for running most docker containers (e.g. the dev dependencies for the API), but not for running the entire platform.

```shell
docker-machine create --driver virtualbox dev
```

If you wish to run the entire platform using Docker (currently the only way to run the entire platform),
you'll need to dedicate more resources to the Docker machine.
We've found that for the full stack to run efficiently, you need approximately:

* 4 CPUs
* 6 GB RAM
* 80 GB Disk space

Assuming you are using Docker Machine to provision your Docker
environment, you can check you current settings with the following
(ignore the second `Memory`):

```shell
    $ docker-machine inspect | grep 'CPU\|Memory\|DiskSize'
        "CPU": 4,
        "Memory": 6144,
        "DiskSize": 81920,
        "Memory": 0,
```

If your settings are below these suggestions, you should create a new
Docker VM. The following will create a VM named `hmda-platform` with
the appropriate resources:

```shell
    $ docker-machine create \
    --driver virtualbox \
    --virtualbox-disk-size 81920 \
    --virtualbox-cpu-count 4 \
    --virtualbox-memory 6144 \
    hmda-platform
```

After the machine is created, make sure that you connect your shell with the newly created machine
```shell
$ eval "(docker-machine env dev)"
```


## Building and Running

### Building the .jar

* To build JVM artifacts (the default, includes all projects), from the sbt prompt:

```shell
> clean assembly
```

This task will create a `fat jar`, which can be executed directly on any JDK8 compliant JVM:

```shell
java -jar target/scala-2.11/hmda.jar
```


### Running Interactively

#### Running the Dependencies

Assuming you have Docker-Compose installed (according to the [Docker](#docker) instructions above),
the easiest way to get all of the platform's dependencies up and running with the provided docker-compose dev setup:

```shell
docker-compose -f docker-dev.yml up
```

When finished, use `docker-compose down` to gracefully stop the running containers.

In order to not get Postgres connection error upon signing a submission, you may want to start up the keycloak image as well.
```shell
docker-compose up keycloak
```
Not having the keycloak image running will produce an error in the console, but will not cause the platform to crash.

#### Running the API

Once the dependencies (above) are running, follow these steps in a separate terminal session to get the API running with sbt:

* For smoke testing locally, add the following two environment variables:
  * `EDITS_DEMO_MODE`: This will allow you to use the sample files in this repo for testing the app. Otherwise, edit S025 will trigger for all files.
  * `HMDA_IS_DEMO`: This uses configuration files that allow running the app locally, instead of in a cluster.

```shell
export EDITS_DEMO_MODE=true
export HMDA_IS_DEMO=true
```

* Start `sbt`

```shell
$ sbt
```

* Select project to build and run.This will retrieve all necessary dependencies, compile Scala source, and start a local server. It also listens for changes to underlying source code, and auto-deploys to local server.

```shell

> project cluster
> clean
> ~re-start
```

Confirm that the platform is up and running by browsing to http://localhost:8080

When finished, press enter to get the sbt prompt, then stop the project by entering `reStop`.


### Running the Project with Docker

#### To run only the API

First, ensure there's a compiled jar to create the Docker image with:
```shell
sbt clean assembly
```

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

By default, the `HDMA Platform` runs with a log level of `INFO`. This can be changed by establishing a different log level in the `HMDA_LOGLEVEL` environment variable.
For the different logging options, see the [reference.conf](https://github.com/akka/akka/blob/master/akka-actor/src/main/resources/reference.conf#L38) default configuration file for `Akka`.

#### API Load Testing

A load testing scenario for the API has been built using [Gatling](https://gatling.io/). The parameters for this test can be configured in the `cluster/src/test/resources/application.conf` file.
To run the default scenario:

```shell
$ sbt
> project cluster
> gatling:test
```

After the test is run, some statistics will be presented on the screen. To further study the results of the test, a report can be opened on a browser from the sbt prompt:

```shell
> gatling:lastReport
```

** NOTE: The load test requires the sample panel [file](loader/src/main/resources/inst_data_2017_dummy.csv) to be loaded before running it.

#### To run the entire platform

1. Ensure you have a Docker Machine with sufficient resources, as described in the [Docker](#docker) section above.

1. Clone [hmda-platform-ui](https://github.com/cfpb/hmda-platform-ui), 

    [hmda-platform-auth](https://github.com/cfpb/hmda-platform-auth), [hmda-pub-ui](https://github.com/cfpb/hmda-pub-ui), [hmda-homepage](https://github.com/cfpb/hmda-homepage), and [hmda-platform-tools](https://github.com/cfpb/hmda-platform-tools) into the same
    directory as hmda-platform.

        ~/dev/hmda-project$ ls -l
        drwxr-xr-x  22 lortone  staff   748B Jul 27 16:28 hmda-platform/
        drwxr-xr-x  25 lortone  staff   850B Jul 25 17:13 hmda-platform-ui/
        drwxr-xr-x  23 lortone  staff   796B Jul 28 17:15 hmda-platform-auth/
        drwxr-xr-x  23 lortone  staff   796B Jul 28 17:15 hmda-pub-ui/
        drwxr-xr-x  23 lortone  staff   796B Jul 28 17:15 hmda-platform-tools/
        drwxr-xr-x  23 lortone  staff   796B Jul 28 17:15 hmda-homepage/

1. Build hmda-platform-ui

        cd hmda-platform-ui && \
        yarn && \
        cd ..

    **Note:** This requires [yarn](https://yarnpkg.com/lang/en/docs/install/) to be installed.

1. Build hmda-pub-ui

        cd hmda-pub-ui && \
        yarn && \
        cd ..

    **Note:** This requires [yarn](https://yarnpkg.com/lang/en/docs/install/) to be installed.

1. Build hmda-homepage

        cd hmda-homepage && \
        yarn && \
        cd ..

    **Note:** This requires [yarn](https://yarnpkg.com/lang/en/docs/install/) to be installed.

1. Build hmda-platform-tools

        cd hmda-platform-tools && \
        yarn && \
        cd ..

    **Note:** This requires [yarn](https://yarnpkg.com/lang/en/docs/install/) to be installed.

1. Build hmda-platform

        cd hmda-platform && \
        sbt clean assembly

1. Launch the stack with Docker Compose

        docker-compose up

    This will bring up all the HMDA Platform services. The first run may take several minutes.

1. Discover your Docker host's IP

        echo $DOCKER_HOST

    ...or if using Docker Machine...

        docker-machine ip

    **Note:** Docker Machine generally defaults to `192.168.99.100`.  We reference that
    IP throught this doc.  If your Docker host IP differs, please adjust these instructions
    to match the Docker host IP provided by your system.

1. Use it!  Below are steps representing a standard HMDA filing:
    1. Browse to the homepage at http://192.168.99.100.
    1. Select the "Get started filing your HMDA data" link. This will redirect your browser to the filing app.
    1. Select the "Login" button.  This will redirect your browser to the Keycloak login screen.
    1. Select "create and account" on the login screen.
    1. Enter you account information and select "Register".

        **Note:** You must register with an email address from our whitelist of email domains.
        For convenience, `bank0.com` and `bank1.com` address should be available automatically.

    1. Browse to the mock email server at https://192.168.99.100:8443/mail/, and select the
        verification link in the email found there.  This should take you back to the HMDA
        filing web app, now logged in.

        **Note:** This "MailDev" services is for development purposes only.  In the case of
        an actual HMDA filing, you would receive a confirmation to your actual email account.

    1. Submit a HMDA filing.  Several sample files can be found [here](https://github.com/cfpb/hmda-platform/tree/master/parser/jvm/src/test/resources/txt).

1. To view the publication UI (hmda-pub-ui) go to http://192.168.99.100/data-publication/.
1. To view the tools (hmda-platform-tools) go to http://192.168.99.100/tools/.

##### Updating an existing system

If you've updated any of the hmda-platform services, and would like to see those changes reflected in the Docker Compose setup, the simplest way to do this is to rebuild everything from scratch.  The following command should be executed from within the `hmda-platform` directory.

    docker-compose stop -t0 && \
    docker-compose rm -vf && \
    cd ../hmda-platform-ui && \
    yarn && \
    cd ../hmda-platform-tools && \
    yarn && \
    cd ../hmda-pub-ui && \
    yarn && \
    cd ../hmda-platform && \
    sbt clean assembly && \
    docker-compose build --no-cache && \
    docker-compose up


##### Service URLs

When running the full stack via Docker Compose, the following services are available:

| Service                | URL                                           |
|------------------------|-----------------------------------------------|
| Filing UI              | https://192.168.99.100                        |
| Filing API (Unsecured) | http://192.168.99.100:8080                    |
| Filing API (Secured)   | https://192.168.99.100:4443/hmda/             |
| Data Publication       | https://192.168.99.100:8443/data-publication/ |
| Tools                  | https://192.168.99.100:8443/tools/            |
| Admin API              | http://192.168.99.100:8081                    |
| Public API             | https://192.168.99.100:4443/public/           |
| Keycloak               | https://192.168.99.100:8443                   |
| MailDev                | https://192.168.99.100:8443/mail/             |

#### Development conveniences

##### Mounted volumes

For convenience when doing development on the UI, Auth setup, and API, the `docker-compose` file uses a `volumes` which mounts

- the ui's `dist/` directory into the `hmda-platform-ui` container,
- the publication ui's `data-publication/` directory into the `hmda-pub-ui` container,
- the tools `tools/` directory into the `hmda-platform-tools` container,
- the `hmda.jar` into `hmda-platform` container,
- and the `hmda` themes directory in the auth repo into the `keycloak` container.

```shell
# from the hmda-platform-ui, hmda-pub-ui, and/or hmda-platform-tools directory
npm run watch
```

## Resources

### API Documentation

* [Public API Documentation](public-api.md)
* [Admin API Documentation](admin-api.md)
* [Filing API Documentation](API.md)

### Data Specifications

* [Filing Status](filing-status.md)
* [Submission Status](submission-status.md)
* [Panel](panel.md)
* [TS File Spec](2017_File_Spec_TS.csv)
* [LAR File Spec](2017_File_Spec_LAR.csv)
* [Modified LAR File Spec](2017_Modified_LAR_Spec.csv)
* [RID Spec](2017_File_Spec_RID.csv)
* [Loading 2016 Data](2016_Data_load.md)
