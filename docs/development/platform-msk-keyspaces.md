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
- For Apache Cassandra(k8ssandra) with PlainTextAuthProvider use [persistence.conf](common/src/main/resources/persistence.conf)
- For AWS Keyspace with PlainTextAuthProvider use [persistence-keyspace.conf](common/src/main/resources/persistence-keyspace.conf) (do not use for cluster deployment)
- For AWS Keyspace with SigV4AuthProvider [persistence-keyspace-sigv4.conf](common/src/main/resources/persistence-keyspace-sigv4.conf)
- List of platform related files to updated
```
hmda/src/main/resources/application.conf (local deployment)
hmda/src/main/resources/application-kubernetes.conf (cluster deployment)
hmda-analytics/src/main/resources/application.conf
modified-lar/src/main/resources/application.conf
irs-publisher/src/main/resources/application.conf
```

- Environment variables for Apache Cassandra(k8ssandra) with PlainTextAuthProvider use [persistence.conf](common/src/main/resources/persistence.conf)
```
export CASSANDRA_CLUSTER_HOSTS="10.x.x.x:9042"
export CASSANDRA_JOURNAL_KEYSPACE=hmda2_journal
export CASSANDRA_SNAPSHOT_KEYSPACE=hmda2_snapshot
export CASSANDRA_CLUSTER_USERNAME=xxx
export CASSANDRA_CLUSTER_DC=xxx
export CASSANDRA_CLUSTER_PASSWORD=xxx
```
- Setup java truststore
```
curl https://certs.secureserver.net/repository/sf-class2-root.crt -O
openssl x509 -outform der -in sf-class2-root.crt -out temp_file.der
keytool -import -alias cassandra -keystore cassandra_truststore.jks -file temp_file.der
...
Trust this certificate? [no]:  yes
Certificate was added to keystore
```
- Environment variables for AWS Keyspace with PlainTextAuthProvider use [persistence-keyspace.conf](common/src/main/resources/persistence-keyspace.conf)
```
export CASSANDRA_TRUSTSTORE_FILE="/tmp/cassandra_truststore.jks"
export CASSANDRA_TRUSTSTORE_PASSWORD="xxx"
export CASSANDRA_JOURNAL_KEYSPACE=hmda2_journal
export CASSANDRA_SNAPSHOT_KEYSPACE=hmda2_snapshot
export CASSANDRA_CLUSTER_DC="us-east-1"
export CASSANDRA_CLUSTER_USERNAME="xxx"
export CASSANDRA_CLUSTER_PASSWORD="xxx"
```

- Environment variables for AWS Keyspace with SigV4AuthProvider [persistence-keyspace-sigv4.conf](common/src/main/resources/persistence-keyspace-sigv4.conf)
```
export CASSANDRA_TRUSTSTORE_FILE="/tmp/cassandra_truststore.jks"
export CASSANDRA_TRUSTSTORE_PASSWORD="xxx"
export CASSANDRA_JOURNAL_KEYSPACE=hmda2_journal
export CASSANDRA_SNAPSHOT_KEYSPACE=hmda2_snapshot
export CASSANDRA_CLUSTER_DC="us-east-1"
export CASSANDRA_CLUSTER_USERNAME="cassandrausernamevalue1"
export CASSANDRA_CLUSTER_PASSWORD="xxx"
export AWS_ACCESS_KEY_ID="acesskeyvalue" 
export AWS_SECRET_ACCESS_KEY="secretkeyvalue" 
```
- Setup java truststore
```
curl https://certs.secureserver.net/repository/sf-class2-root.crt -O
openssl x509 -outform der -in sf-class2-root.crt -out temp_file.der
keytool -import -alias cassandra -keystore cassandra_truststore.jks -file temp_file.der
...
Trust this certificate? [no]:  yes
Certificate was added to keystore
```
- Backward compatibility update configmap to accommodate for k8ssandra
```
kubectl patch configmap/cassandra-configmap \
  --type merge \
  -p '{ "data": { "cassandra-hosts": "cluster1-main-service.k8ssandra-operator:9042" }}'

kubectl -n beta patch configmap/cassandra-configmap \
  --type merge \
  -p '{ "data": { "cassandra-hosts": "cluster1-main-service.k8ssandra-operator:9042" }}'
```
