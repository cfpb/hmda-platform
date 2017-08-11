# Panel CSV Loader

## Overview
The panel loader is designed to read a CSV file and load the data onto the HMDA-Platform.  The CSV file should use the `|` (pipe) delimiter, and should include a header row as the first line.

## Environment Variables
There is only one environment variable used by the panel loader.  It must be set correctly in order for the data to be sent to the admin API.

For testing on an API running in SBT, no changes need to be made.  The default for this variable will point to the correct local admin API.

For loading panel data into a remote system or into a local Docker container, you'll need to set the following environment variable:
```shell
> export HMDA_HTTP_ADMIN_URL={base URL}
```

**IMPORTANT NOTE:** The base URL should *include* `http://` or `https://`, but *exclude* any trailing backslash `/`.  For example:

```shell
> export HMDA_HTTP_ADMIN_URL=http://192.168.99.100:8081
```

## Running the parser
A small example file is located at `panel/src/main/resources/inst_data_2017_dummy.csv`

The real panel file is located at `panel/src/main/resources/inst_data_2017.csv`

In order for the panel data to be loaded locally, the API project must be up and running, along with Docker containers running Cassandra and Zookeper, or run the full `docker-compose` setup.  To load panel data into the cluster, simply find the URL of the admin api (for the release branch: `https://hmda-ops-api.demo.cfpb.gov/admin`).  No other running services are necessary.

In a terminal, execute the following commands:

```shell
> sbt
sbt> project panel
sbt> run /path/to/panelData.csv
```

The project can also be run as a java `.jar` file.  While running `sbt` and in the panel project, run the following commands:
```shell
sbt> clean
sbt> assembly
```
Then the panel loader can be run with `java -jar  panel/target/scala-2.12/panel.jar path/to/institution_file.csv`

## Error codes
There are four ways the panel loader can fail.  The exit code and error message should tell you what happened.

1. There were no command line arguments passed to the loader
2. The path passed to the loader didn't point to a file
3. The call to `institutions/create` didn't return the correct response.  This can indicate that you don't have the correct environment variables set, or that something is wrong with the hmda-platform.
4. The loader didn't finish processing all the institutions.  This will happen when running the real panel file, but unsure as to why this happens.

## Testing
Make sure your authorization header is updated with a few real `id_rssd` fields from the given file.  This can be found in the API log output (first field argument in the `InstitutionQuery` object), or in the CSV file (seventh field).

Try out the endpoint `localhost:8080/institutions`, and you should see a response with real panel data.
