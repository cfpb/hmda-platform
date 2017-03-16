# Panel CSV Parser

## Overview
The panel parser is designed to read a CSV file and load the data onto the HMDA-Platform.  The CSV file should use the `|` (pipe) delimiter, and should include a header row as the first line.

## Running the parser
An example panel file is located at `hmda-platform/panel/src/main/resources/inst_data_2015_20170308_dummy.csv`

While in the `hmda-platform` project, start up `sbt`, then execute the following commands:
```shell
> project panel
> clean
> run /path/to/panelData.csv
```
The project currently has to be force-stopped with `Ctrl+C`.

At this point, make sure your `HMDA_IS_DEMO` environment variable is set to `false`, so the real data will be loaded.
```shell
export HMDA_IS_DEMO=false
```

Restart `sbt`, then execute the following commands (don't run `clean`!):
```shell
> project api
> reStart
```
You should see the institutions table being populated with the institutions contained in the given panel file.  Note that any institutions with duplicate `id_rssd` fields will not be inserted, so the final row count may be lower than the file's line count.

## Testing
Make sure your authorization header is updated with a few real `id_rssd` fields from the given file.  This can be found in the log output (first field argument in the `InstitutionQuery` object), or in the CSV file (seventh field).

Try out the endpoint `localhost:8080/institutions`, and you should see a response with real panel data.
