# Panel CSV Parser

## Overview
The panel parser is designed to read a CSV file and load the data onto the HMDA-Platform.  The CSV file should use the `|` (pipe) delimiter, and should include a header row as the first line.

## Running the parser
An example panel file is located at `hmda-platform/panel/src/main/resources/inst_data_2017_dummy.csv`

In order for the panel data to be loaded, the API project must be up and running, along with Docker containers running Cassandra and Zookeper.  In a terminal, execute the following commands:

```shell
> sbt
sbt> project panel
sbt> clean
sbt> run /path/to/panelData.csv
```

## Testing
Make sure your authorization header is updated with a few real `id_rssd` fields from the given file.  This can be found in the API log output (first field argument in the `InstitutionQuery` object), or in the CSV file (seventh field).

Try out the endpoint `localhost:8080/institutions`, and you should see a response with real panel data.
