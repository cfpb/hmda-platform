Pre-requisites
- [k8ssandra](https://k8ssandra.io/)
- [Strimzi](https://strimzi.io/)
- [Postresql](https://github.com/bitnami/charts/tree/main/bitnami/postgresql)
- [Keycloak](https://github.com/bitnami/charts/tree/main/bitnami/keycloak)   
- S3 Buckets

Install
- Add Secrets
```
kubectl create secret generic cassandra-credentials --from-literal=cassandra.username=  --from-literal=cassandra.password=
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
export $PLATNS=default

helm upgrade --install --namespace=$PLATNS --values=kubernetes/hmda-platform/values.yaml \
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
kubectl delete -f https://github.com/cfpb/hmda-platform/tree/master/kubernetes/config-maps
```
