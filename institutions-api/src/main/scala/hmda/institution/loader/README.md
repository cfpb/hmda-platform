## How to run the institution loader

1. Run the platform locally by modifying the `KAFKA_CLUSTER_HOSTS` and `KAFKA_INSTITUTIONS_TOPIC`
2. Run the institution loader by doing the following:


```shell script
$ env JAVA_OPTS="-Xmx8096m" sbt
$ project institutions-api
$ runMain hmda.institution.loader.InstitutionLoader <path to institutions file> <post or put>
```
As the institution loader runs, you'll start seeing logs of institutions being added in the local instance of the platform. Additionally, the kafka topic will start being populated. 
