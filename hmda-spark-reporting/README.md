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

```
$ env JAVA_OPTS="-Xms256m -Xmx4096m -Xss8m" sbt -batch hmda-spark-reporting/docker && docker tag cfpb/hmda-spark-reporting hmda/hmda-spark-reporting:<imagetag> && docker push hmda/hmda-spark-reporting:<imagetag>
```
