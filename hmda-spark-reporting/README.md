## How to run spark reports to generate A&D (Aggregate & Disclosure) reports

### Pre-requisite

- K8s
- Spark Operator

### To test the spark operator is working well

Execute spark-pi.yaml

```
$ k apply -f spark-pi.yaml
```

Monitor the status

```
$ k describe -f spark-pi.yaml
Events:
  Type    Reason                     Age                From            Message
  ----    ------                     ----               ----            -------
  Normal  SparkApplicationAdded      32m                spark-operator  SparkApplication spark-pi was added, enqueuing it for submission
  Normal  SparkApplicationSubmitted  32m                spark-operator  SparkApplication spark-pi was submitted successfully
  Normal  SparkDriverRunning         32m                spark-operator  Driver spark-pi-driver is running
  Normal  SparkExecutorPending       32m (x2 over 32m)  spark-operator  Executor spark-pi-1585012650633-exec-1 is pending
  Normal  SparkExecutorRunning       31m                spark-operator  Executor spark-pi-1585012650633-exec-1 is running
  Normal  SparkDriverCompleted       31m                spark-operator  Driver spark-pi-driver completed
  Normal  SparkApplicationCompleted  31m                spark-operator  SparkApplication spark-pi completed
```

If the above output reads `spark-pi completed` that means that the spark operator is working as expected.

### Running A&D reports

There are two options to run A&D reports: In production  or In Development. If running in development, the production data must be copied to development. This README assumes that the reports are being run in development. 

In summary, the spark jobs that run the reports do the following things:

- Disclosure Reports: Read a record from kafka topic of type (<lei>:<submission-id>). The spark job will fetch all the data for the lei from PG tables, perform analysis for each msamd, and generate an S3 file for each msamd. 
- Aggregate Reports: Aggregate reports will run on the entire dataset for each msamd and S3 files will be published. 

### Building Disclosure report docker image:

Build the docker image, tag it, push it. 

```
$ env JAVA_OPTS="-Xms256m -Xmx4096m -Xss8m" sbt -batch hmda-spark-reporting/docker && docker tag cfpb/hmda-spark-reporting hmda/hmda-spark-reporting:<imagetag> && docker push hmda/hmda-spark-reporting:<imagetag>
```

Mention the docker image in `hmda-spark-disclosure.yaml` in `image: "hmda/hmda-spark-reporting:<imagetag>"`

The spark jobs depend on the following secrets:

- inst-postgres-credentials: JDBC URL connection to the DB
- aws-credentials: containing access and secret keys
- kafka-hosts: connection informatino for kafka
- aws-ev: dev vs. prod


### Running A&D Reports:

The reports can be run by `apply`. They can be deleted by `delete`. They can be described by `describe`. Below are examples:

#### Applying:

`kubectl apply  -f hmda-spark-disclosure.yaml`

#### Deleting:

`kubectl delete  -f hmda-spark-disclosure.yaml`

#### Describing:

`kubectl describe  -f hmda-spark-disclosure.yaml`

### Verifying:

### Disclosure reports:

There are two types of disclosure reports 
- 1.json (Applications by Tract)
- 2.json (Loans Purchased by Tract)

Below are the S3 paths for disclosure reports

`aws s3 ls s3://cfpb-hmda-public/<dev | prod>/reports/disclosure/<year>/<lei>/<msamd>/<1.json | 2.json>`

### Aggregate reports:

There are seven types of aggregate reports

- 1.json (Applications by Tract)
- 2.json (Loans Purchased by Tract)
- 3.json (Applications by Race and Sex)
- 4.json (Applications by Ethnicity and Sex)
- 5.json (Applications by Income, Race, Ethnicity)
- 9.json (Applications by Median Age of Homes)
- i.json (Reporting Financial Institutions)

Below are the S3 paths for aggregate reports

`aws s3 ls s3://cfpb-hmda-public/<dev | prod>/reports/aggregate/<year>>/<msamd>/`

### Port-forwarding

While the spark jobs are running, their status can be visualized by doing port-forwarding:

`kubectl port-forward hmda-spark-disclosure-driver 4040:4040`

It will look something like this:

![Spark jobs screenshot](https://user-images.githubusercontent.com/44377678/77762269-a4355380-700f-11ea-9b64-067d5221c9b4.png)