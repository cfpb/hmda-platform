# 2016 HMDA DATA LOAD

The easiest way to load data to a running application is through a couple of shell scripts, listed below. The appropriate
host and port need to be set up to point to the server running the `Filing API`.
These script need to run in directory where HMDA 2016 .txt files are located.

## Data load script

Loads 2016 data.

```shell
host=localhost
port=8080

for i in *.txt
  do
    echo 'Processing' $i
    x=${i%.txt}
    http POST http://$host:$port/institutions/$x/filings/2016/submissions cfpb-hmda-username :user cfpb-hmda-institutions:$x
    sleep 0.25
    SUB=$(http http://$host:$port/institutions/$x/filings/2016 cfpb-hmda-username:user cfpb-hmda-institutions:$x | jq '.submissions[0].id.sequenceNumber')
    echo 'Submission' $SUB
    http --form --timeout 240 POST http://$host:$port/institutions/$x/filings/2016/submissions/$SUB file@$i cfpb-hmda-username:user cfpb-hmda-institutions:$x
    sleep 0.25
  done
```

# Data check script

Checks the status of the loaded data. Should be 5 (parsing errors), 8 or 9 (validated with errors, or validated).
Needs to run after validation has been completed.

```shell
host=localhost
port=23615

for i in *.txt
   do
     echo 'Checking' $i
     x=${i%.txt}
     STATUS=$(http http://$host:$port/institutions/$x/filings/2016 cfpb-hmda-username:user     cfpb-hmda-institutions:$x | jq '.submissions[0].status.code')
     echo $x','$STATUS >> files_checked.txt
   done
```


NOTE: These two scripts require [httpie](https://httpie.org/) and [jq](https://stedolan.github.io/jq/) to be installed in order to run.


# 2016 HMDA DATA LOAD - DEPRECATED

The 2016 LAR data is necessary for some macro edits that will make comparisons between the file submitted and the previous year's data.

In order to load the data, the 2017 panel should be loaded already. The LAR Loader will iterate through a file directory where the `HMDA` files reside,
and send their contents to the `HMDA Platform`. The `HMDA` files must be in the pipe delimited format, with `.txt` extension and contain both Transmittal Sheet
and Loan Application Register data, as per the [2017 Filing Instruction Guide](https://www.consumerfinance.gov/data-research/hmda/static/for-filers/2017/2017-HMDA-FIG.pdf)


## Loading LAR data

By default, the loader will use `localhost` as the host where the HMDA Platform cluster is running. For remote systems, use the `HMDA_CLUSTER_HOST` environment variable.

When starting the cluster, take note of the port that Akka uses to communicate (i.e. 2551). This value needs to be set in an environment variable.
The persistence cluster role needs to be active, and its dependencies (`Zookeeper`, `Cassandra`) running and properly configured.

```shell
$ export HMDA_CLUSTER_PORT=2551
$ sbt
> project loader
> run /tmp/sample_lar 2016
```

This will connect to the running cluster and send the contents of each file in the directory to the appropriate Actor responsible for storing `LARs` in the 2016 period.

## Troubleshooting

* In cases where there is no connection, make sure that the `HmdaSupervisor` and `HmdaQuerySupervisor` are receiving messages from the cluster client. Both of these actors
need to have the Cluster Receptionist enabled. For more information please consult the current Akka documentation on [Cluster Client](https://doc.akka.io/docs/akka/current/scala/cluster-client.html)

* When loading large number of files, it might be necessary to increase the `hmda.persistent-actor-timeout` in the `Persistence-model` project's configuration file (or by passing the relevant
runtime flag in a deployed application).

