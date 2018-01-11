[![Build Status](https://travis-ci.org/cfpb/hmda-platform.svg?branch=master)](https://travis-ci.org/cfpb/hmda-platform) [![codecov.io](https://codecov.io/github/cfpb/hmda-platform/coverage.svg?branch=master)](https://codecov.io/github/cfpb/hmda-platform?branch=master)

# HMDA Platform

## This project is a work in progress

Information contained in this repository should be considered provisional and a work in progress, and not the final implementation for the HMDA Platform, unless otherwise indicated.

## Introduction

For more information on HMDA, checkout the [About HMDA page](http://www.consumerfinance.gov/data-research/hmda/learn-more) on the CFPB website.

## The HMDA Platform

This repository contains the code for the entirety of the HMDA platform backend. This platform has been designed to accommodate the needs of the HMDA filing process by financial institutions, as well as the data management and publication needs of the HMDA data asset.


## Dependencies

### Java 9 SDK

The HMDA Platform runs on the Java Virtual Machine (JVM), and requires the Java 9 JDK to build and run the project. This project is currently being built and tested on [Oracle JDK 9](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). See [Oracle's JDK Install Overview](http://docs.oracle.com/javase/9/docs/technotes/guides/install/install_overview.html) for install instructions.

The HMDA Platform should also run on JDK 8.

### Scala

The HMDA Platform is written in [Scala](http://www.scala-lang.org/). To build it, you will need to [download](http://www.scala-lang.org/download/) and [install](http://www.scala-lang.org/download/install.html) Scala 2.12.x

In addition, you'll need Scala's interactive build tool [sbt](https://www.scala-sbt.org/). Please refer to sbt's [installation instructions](https://www.scala-sbt.org/1.x/docs/Setup.html) to get started.

## Building and Running

### Building the .jar

* To build JVM artifacts (the default, includes all projects), from the sbt prompt:

```shell
> clean assembly
```
This task will create a `fat jar`, which can be executed on any `JDK9` compliant `JVM`:

`java -jar target/scala-2.12/hmda2.jar`

### Building the Docker image

* To build a `Docker` image that runs as a single node cluster, from the sbt prompt:

```shell
> docker:publishLocal
```
This task will create a `Docker` image. To run a container with the `HMDA Platform` as a single node cluster, will all dependencies:

`docker run --rm -ti -p 19999:19999 jmarin/hmda`

### Running as a local cluster

The application can be run as a distributed cluster on a local machine, by running it on separate `JVM` processes.
The following example starts 2 instances of the application that work as a cluster, with roles `persistence` and `health`:

* First node, acts as seed node and has role `persistence`. From a terminal window:

```shell
export HMDA_RUNTIME_MODE=prod
export HMDA_CLUSTER_ROLES=persistence
java -Dakka.cluster.seed-nodes.0=akka://hmda@127.0.0.1:2551 -jar target/scala-2.12/hmda2.jar
```


* Second node, joins the cluster through the previous seed node and has role `health`. From a separate terminal window:

```shell
export HMDA_RUNTIME_MODE=prod
export APP_PORT=0
export HMDA_CLUSTER_ROLES=health
java -Dakka.cluster.seed-nodes.0=akka://hmda@127.0.0.1:2551 -jar target/scala-2.12/hmda2.jar
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
