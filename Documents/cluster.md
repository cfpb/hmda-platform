# HMDA Akka Cluster Configuration

## Cluster Management

The project uses the [Akka Cluster HTTP Management](http://developer.lightbend.com/docs/akka-management/current/cluster-http-management.html) module to report on cluster member status as well as to perform cluster
management tasks. Please refer to the documentation of this module for cluster management tasks (i.e. downing a node from the cluster)

## Roles

There are four different cluster roles, each managed within the cluster project.

### API

The API cluster role is responsible for the four different APIs: Admin, Public, Filing, and the TCP Panel Loader

### Persistence

The persistence cluster role contains actors responsible for tracking and persisting information about submissions, institutions, and LAR validation

### Query

This cluster role contains actors responsible for interacting with the backend database (currently Cassandra)

### Publication

The publication role is responsible for creating and publishing the A&D reports from submitted LAR data

## Running cluster roles

The configuration setting `"akka.cluster.roles"` in `cluster/src/main/resources/application.conf` defines which cluster roles will start when the HMDA Platform starts.  By default, all four roles are set to start at the same time, but this can be changed to any single role or combination of roles.  Currently, this can only be done from within the config file; there is no environment variable override.
