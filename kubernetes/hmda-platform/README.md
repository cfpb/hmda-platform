Pre-requisites
- [AWS Keyspaces](https://docs.aws.amazon.com/keyspaces/latest/devguide/getting-started.ddl.html)
- [Strimzi](https://strimzi.io/)
- Create keyscapes
```
CREATE KEYSPACE IF NOT EXISTS hmda2_journal
    WITH replication = {'class': 'SingleRegionStrategy'};
CREATE KEYSPACE IF NOT EXISTS hmda2_snapshot
    WITH replication = {'class': 'SingleRegionStrategy'};
```
- Create tables (after some modification, not all fields are supported)    
https://github.com/cfpb/hmda-platform/blob/master/hmda-sql-doc/cassandra-scripts.txt
```
cqlsh -u username -p passsword -f ../hmda-sql-doc/cassandra-scripts.txt
```
Install
- Add Secrets
```
curl https://certs.secureserver.net/repository/sf-class2-root.crt -O
openssl x509 -outform der -in sf-class2-root.crt -out temp_file.der
keytool -import -alias cassandra -keystore cassandra_truststore.jks -file temp_file.der
...
Trust this certificate? [no]:  yes
Certificate was added to keystore

kubectl create secret generic cassandra-truststore --from-file=cassandra_truststore.jks
kubectl create secret generic cassandra-truststore-password --from-literal=password=XXXX
kubectl create secret generic cassandra-keyspace-credentials --from-literal=aws-access-key-id=XXXX --from-literal=aws-secret-access-key=XXX
kubectl create secret generic inst-postgres-credentials --from-literal=username= --from-literal=password= --from-literal=host= --from-literal=url="jdbc:postgresql://postgresql:5432/hmda?user= &password= &sslmode=false"
```
- Add Configmap
```
kubectl apply -f https://github.com/cfpb/hmda-platform/tree/master/kubernetes/config-maps
```

Update
```
helm upgrade --install --namespace=default --values=kubernetes/hmda-platform/values-dev.yaml \
--set image.tag=latest \
--set service.name=hmda-platform \
hmda-platform kubernetes/hmda-platform
```

Delete
```
helm uninstall hmda-platform
kubectl delete secret cassandra-credentials
kubectl delete secret inst-postgres-credentials
kubectl delete -f https://github.com/cfpb/hmda-platform/tree/master/kubernetes/config-maps
```
