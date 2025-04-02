## Specific documentation related to AWS Keyspace and AWS MSK
### MSK
This includes the MSK configuration for hmda-platform, modified-lar, irs-publisher and email-service 

- Required env variables for MSK 
```
export KAFKA_SECURITY="SASL_SSL" 
export AWS_ACCESS_KEY_ID="acesskeyvalue1" 
export AWS_SECRET_ACCESS_KEY="secretkeyvalue1" 
```
- To use plaintext KAFKA
`unset KAFKA_SECURITY`

### Keyspaces
This includes the Keyspaces configuration for hmda-platform, modified-lar, irs-publisher and hmda-analytics
- For Apache Cassandra(k8ssandra) with PlainTextAuthProvider use [persistence.conf](https://github.com/cfpb/hmda-platform/blob/master/common/src/main/resources/persistence.conf)
- For AWS Keyspace with PlainTextAuthProvider, not used reference only [persistence-keyspace.conf](https://github.com/cfpb/hmda-platform/blob/master/common/src/main/resources/persistence-keyspace.conf)
- For AWS Keyspace with [SigV4AuthProvider](https://github.com/aws/aws-sigv4-auth-cassandra-java-driver-plugin) [persistence-keyspace-sigv4.conf](https://github.com/cfpb/hmda-platform/blob/master/common/src/main/resources/persistence-keyspace-sigv4.conf)
- Changes in application files, required to connect to keyspaces
```
hmda/src/main/resources/application.conf (for local platform to connect to keyspaces)
hmda/src/main/resources/application-kubernetes.conf (for platform run on kubernetes cluster)
hmda-analytics/src/main/resources/application.conf
modified-lar/src/main/resources/application.conf
irs-publisher/src/main/resources/application.conf
```
- Changes in application files
```
-include "persistence.conf"
+include "persistence-keyspace-sigv4.conf"
```

- Environment variables for Apache Cassandra(k8ssandra) with PlainTextAuthProvider use [persistence.conf](https://github.com/cfpb/hmda-platform/blob/master/common/src/main/resources/persistence.conf)
```
export CASSANDRA_CLUSTER_HOSTS="10.x.x.x:9042"
export CASSANDRA_JOURNAL_KEYSPACE=hmda2_journal
export CASSANDRA_SNAPSHOT_KEYSPACE=hmda2_snapshot
export CASSANDRA_CLUSTER_USERNAME=xxx
export CASSANDRA_CLUSTER_DC=main
export CASSANDRA_CLUSTER_PASSWORD=xxx
```
- Setup java truststore
```
curl -o /tmp/sf-class2-root.crt https://certs.secureserver.net/repository/sf-class2-root.crt
openssl x509 -outform der -in /tmp/sf-class2-root.crt -out /tmp/temp_file.der
keytool -import -alias cassandra -keystore /tmp/cassandra_truststore.jks -file /tmp/temp_file.der
...
Trust this certificate? [no]:  yes
Certificate was added to keystore

kubectl create secret generic cassandra-truststore --from-file=/tmp/cassandra_truststore.jks
kubectl create secret generic cassandra-truststore-password --from-literal=password=xxx
```
- Environment variables for AWS Keyspace with PlainTextAuthProvider not used reference only [persistence-keyspace.conf](https://github.com/cfpb/hmda-platform/blob/master/common/src/main/resources/persistence-keyspace.conf)
```
export CASSANDRA_TRUSTSTORE_FILE="/tmp/cassandra_truststore.jks"
export CASSANDRA_TRUSTSTORE_PASSWORD="xxx"
export CASSANDRA_JOURNAL_KEYSPACE=hmda2_journal
export CASSANDRA_SNAPSHOT_KEYSPACE=hmda2_snapshot
export CASSANDRA_CLUSTER_DC="us-east-1"
export CASSANDRA_CLUSTER_USERNAME="xxx"
export CASSANDRA_CLUSTER_PASSWORD="xxx"
```

- Environment variables for AWS Keyspace with SigV4AuthProvider [persistence-keyspace-sigv4.conf](https://github.com/cfpb/hmda-platform/blob/master/common/src/main/resources/persistence-keyspace-sigv4.conf)
```
export CASSANDRA_TRUSTSTORE_FILE="/tmp/cassandra_truststore.jks"
export CASSANDRA_TRUSTSTORE_PASSWORD="xxx"
export CASSANDRA_JOURNAL_KEYSPACE=hmda2_journal
export CASSANDRA_SNAPSHOT_KEYSPACE=hmda2_snapshot
export CASSANDRA_CLUSTER_DC="us-east-1"
export CASSANDRA_CLUSTER_USERNAME="xxx" (not used)
export CASSANDRA_CLUSTER_PASSWORD="xxx" (not used)
export AWS_ACCESS_KEY_ID="acesskeyvalue" 
export AWS_SECRET_ACCESS_KEY="secretkeyvalue"
```

- Configmap difference keyspaces/k8ssandra
```
kubectl get configmap cassandra-configmap -ojson | jq .data
{
  "cassandra-cluster-dc": "us-east-1",
  "cassandra-hosts": "vpce-xxx.cassandra.us-east-1.vpce.amazonaws.com:9142",
  "cassandra-keyspace": "hmda2_journal",
  "cassandra-keyspace-snapshot": "hmda2_snapshot"
}

kubectl get configmap cassandra-configmap -ojson | jq .data
{
  "cassandra-cluster-dc": "main",
  "cassandra-hosts": "cluster1-main-service.k8ssandra-operator:9042",
  "cassandra-keyspace": "hmda2_journal",
  "cassandra-keyspace-snapshot": "hmda2_snapshot"
}
```
