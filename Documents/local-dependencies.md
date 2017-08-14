This project depends on several services, which all need to be running in order for the project to operate fully. 

Assuming you have Docker-Compose installed, the easiest way to get all of the platform's dependencies up and running with the provided docker-compose dev setup:

`docker-compose -f docker-dev.yml up`

Alternatively, you can start each one individually with the following instructions.


##### Write Journal

* The write side of this system is supported by either a local `leveldb` database or Cassandra. By default, the local `leveldb` is utilized, and some sample data is loaded automatically.

If using `Cassandra` is desired, set the following environment variable:

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
A sample file is located in the following folder: `panel/src/main/resources/inst_data_2017_dummy.csv`

##### Read Journal

* In order to support the read side, a local PostgreSQL and Cassandra server are needed. Assuming it runs on the default port, on the same machine as the API, the following environment variable needs to be set:

```shell
export JDBC_URL='jdbc:postgresql://localhost/hmda?user=postgres&password=postgres'
```

where `hmda` is the name of the `PostgreSQL` database, owned by the default user with default password (`postgres`)

**Note: if you are running the backend only through sbt, the database needs to be created manually in advance, see instructions [here](https://www.postgresql.org/docs/9.1/static/manage-ag-createdb.html)**

For Cassandra, the following environment variables need to be set (assuming Cassandra is running on a docker container as described above):

```shell
export CASSANDRA_CLUSTER_HOSTS=192.168.99.100
export CASSANDRA_CLUSTER_PORT=9042
```

##### Apache Zookeeper

* The `HMDA Platform` is a distributed system that is meant to be run as a clustered application in production.
As such, it needs a mechanism for storing configuration information for additional nodes joining the cluster.
`Apache Zookeeper` is used to store this information. To run the project, zookeeper must be running and available in the local network.
An easy way to satisfy this requirement is to launch a docker container with `ZooKeeper`, as follows:

```shell
$ docker run --rm -p 2181:2181 -p 2888:2888 -p 3888:3888 jplock/zookeeper
```

* Set the environement variables for Zookeper

```shell
export ZOOKEEPER_HOST=192.168.99.100
export ZOOKEEPER_PORT=2181
```
