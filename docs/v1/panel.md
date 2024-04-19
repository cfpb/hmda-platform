# Panel CSV Loader

The panel loader is designed to read a CSV file of institution data and load them onto the HMDA-Platform. It can be used to load data either into a local Cassandra instance or a remote one (e.g. in a cluster).

## The Panel File

The CSV file should use the `|` (pipe) delimiter, and should include a header row as the first line.

A small example file (~200 institutions) is located at `data/panel/inst_data_2017_dummy.csv`

The real panel file (~253,000 institutions) is located at `data/panel/inst_data_2017.csv`


## Loading Institutions Remotely

For loading panel data into a remote system or into a local Docker container, you don't need to have any services running on your local environment as dependencies. You will need to set the `HMDA_HTTP_ADMIN_URL` environment variable.

```shell
> export HMDA_PANEL_LOADER_HOST={TCP host}
> export HMDA_PANEL_LOADER_PORT={TCP port}
```

To load panel data into the cluster, simply find the IP of the admin API.  The port will default to `8888` unless overriden.

To load panel data into a Docker container running locally, the URL will depend on your Docker Machine's IP. If it uses the default IP, this will be the correct setting (port doesn't need to change):
```shell
> export HMDA_PANEL_LOADER_HOST=192.168.99.100
```

Once that variable is set, use the instructions in [Running the Loader](#running-the-loader) to load the data.


## Loading Institutions Locally

In order for the panel data to be loaded locally, the API project must be up and running, along with Docker containers running Cassandra, PostgreSQL, and Zookeper. Once the dependencies are running, use the instructions in [Running the Loader](#running-the-loader) to load the data.

### Running the Dependencies

#### Cassandra

The easiest way to run a Cassandra server to support this application for testing is to do it through Docker:

```shell
docker run --name cassandra -p 9042:9042 -p 7000:7000 -p 7199:7199 cassandra:3.10
```

If you want to connect to this server, the following `docker` command will give you access to the Cassandra instance started in the previous step:

```shell
docker run -it --link cassandra:cassandra --rm cassandra cqlsh cassandra
```

#### Apache Zookeeper

The `HMDA Platform` is a distributed system that is meant to be run as a clustered application in production.
As such, it needs a mechanism for storing configuration information for additional nodes joining the cluster.
`Apache Zookeeper` is used to store this information. To run the project, zookeeper must be running and available in the local network.
An easy way to satisfy this requirement is to launch a docker container with `ZooKeeper`, as follows:

```shell
$ docker run --rm -p 9092:9092 -p 2888:2888 -p 3888:3888 jplock/zookeeper
```

#### PostgreSQL

To run Postgres from a Docker container with the correct ports to connect to the HMDA Platform, use the following command:

```shell
docker run -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=hmda -p 54321:5432 postgres:9.6.1
```

#### HMDA API

* Set the environement variables for Zookeper. `ZOOKEEPER_HOST` uses your Docker Machine's IP address. In this example, we use the default Docker Machine IP:

```shell
export ZOOKEEPER_HOST=192.168.99.100
export ZOOKEEPER_PORT=9092
```

* Set the environment variables for the local Cassandra instance. `CASSANDRA_CLUSTER_HOSTS` also uses the Docker Machine IP:

```shell
export CASSANDRA_CLUSTER_HOSTS=192.168.99.100
export CASSANDRA_CLUSTER_PORT=9042
```

* Tell the platform to use Cassandra as its database instead of LevelDB:

```shell
export HMDA_IS_DEMO=false
```

* Start sbt using the command `sbt`, then use these commands at the sbt prompt:

```shell
project api
clean
re-start
```

## Running the Loader

In a terminal, execute the following commands:

```shell
> sbt
sbt> project loader
sbt> run /path/to/panelData.csv
```

At the `Multiple main classes detected` prompt, choose `2`, for `hmda.loader.panel.PanelCsvLoader`.

The project can also be run as a java `.jar` file.  While running `sbt` and in the panel project, run the following commands:
```shell
sbt> clean
sbt> assembly
```
Then the panel loader can be run with `java -jar  loader/target/scala-2.12/panel.jar path/to/institution_file.csv`


## Error codes
There are three ways the panel loader can fail.  The exit code and error message should tell you what happened.

1. There were no command line arguments passed to the loader
2. The path passed to the loader didn't point to a file
3. If the TCP Host or Port arguments are incorrect, the program will exit successfully.  The only way to troubleshoot this error is to watch the API logs for the actor creations.  A successful run of the loader will create two actors for every institution, which will appear in the logs.

## Testing

Once you have run the Panel Loader with an institution file, you can check the HMDA API to see that the data loaded correctly.

Make sure your authorization header is updated with a few real `id_rssd` fields from the given file.  This can be found in the API log output (first field argument in the `InstitutionQuery` object), or in the CSV file (seventh field).

Try out the endpoint `localhost:8080/institutions`, and you should see a response with real panel data.
