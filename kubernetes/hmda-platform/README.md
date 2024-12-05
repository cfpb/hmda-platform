Pre-requisites
- [k8ssandra](https://k8ssandra.io/)
- [AWS Keyspaces](https://docs.aws.amazon.com/keyspaces/latest/devguide/getting-started.ddl.html)
- [Strimzi](https://strimzi.io/)
- [Postresql](https://github.com/bitnami/charts/tree/main/bitnami/postgresql)
- [Keycloak](https://github.com/bitnami/charts/tree/main/bitnami/keycloak)   
- S3 Buckets

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
# kubectl create secret generic cassandra-credentials --from-literal=cassandra.username=  --from-literal=cassandra.password=
kubectl create secret generic inst-postgres-credentials --from-literal=username= --from-literal=password= --from-literal=host= --from-literal=url="jdbc:postgresql://postgresql:5432/hmda?user= &password= &sslmode=false"
```
- Add Configmap
```
kubectl apply -f https://github.com/cfpb/hmda-platform/tree/master/kubernetes/config-maps
```
- Update configmaps  
- Create schema for platform
```
cqlsh -u username -p passsword -f ../hmda-sql-doc/cassandra-scripts.txt
```

Update
```
export PLATNS=default

helm upgrade --install --namespace=$PLATNS --values=kubernetes/hmda-platform/values.yaml \
--set image.repository=hmda/hmda-platform \
--set image.tag=latest \
--set rbac.enabled=true \
--set grpc.check_digit.host=check-digit-grpc.$PLATNS \
--set grpc.census.host=census-api-grpc.$PLATNS \
hmda-platform kubernetes/hmda-platform
```

Delete
```
helm uninstall hmda-platform
kubectl delete secret cassandra-credentials
kubectl delete secret inst-postgres-credentials
kubectl delete secret cassandra-truststore
kubectl delete secret cassandra-truststore-password
kubectl delete -f https://github.com/cfpb/hmda-platform/tree/master/kubernetes/config-maps
```