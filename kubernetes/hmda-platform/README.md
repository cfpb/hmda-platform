Pre-requisites
- [k8ssandra](https://k8ssandra.io/)
- [Strimzi](https://strimzi.io/)
- Create schema for platform
```
cqlsh -u 
```
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
- Create schema for platform
```
cqlsh -u username -p passsword -f ../hmda-sql-doc/cassandra-scripts.txt
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
