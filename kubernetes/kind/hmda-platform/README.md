# HMDA-Platform, Cassandra and Strimzi with TLS

**NOTE:** I have Docker configured with 8GB Ram and 6 CPUs. YMMV

### Build init container
```sh
docker build . -t dtr-registry.cfpb.gov/hmda/hmda-platform:hmda_init  && \
docker push dtr-registry.cfpb.gov/hmda/hmda-platform:hmda_init
```

### Create Kind cluster
```sh
kind create cluster --config kind-config.yaml
```

### Deploy Cassandra
```sh
# Install the operator
kubectl create -f https://raw.githubusercontent.com/k8ssandra/cass-operator/v1.7.0/docs/user/cass-operator-manifests.yaml --context kind-kind

# Install storageclass (may have some errors)
kubectl create -f https://raw.githubusercontent.com/k8ssandra/cass-operator/master/operator/k8s-flavors/kind/rancher-local-path-storage.yaml --context kind-kind

# Create dc2 cluster
# This will take a few (~7 mins)
kubectl create -n cass-operator -f dc2.yaml --context kind-kind
```

### Deploy Kafka
```sh
helm repo add strimzi https://strimzi.io/charts/

# Install the operator
# This will take a few  (~3mins)
helm install strimzi strimzi/strimzi-kafka-operator --namespace cass-operator

# Create cluster
# This will take a few  (~3mins)
kubectl apply -f strimzi-kafka-cluster.yaml --context kind-kind --namespace cass-operator
```

### Deploy akhq (Optional)
```sh
helm repo add akhq https://akhq.io/
cd akhq && helm install akhq . --namespace cass-operator && cd ..
```

### Apply `configmaps`
```sh
kubectl apply -f kubernetes/cassandra-configmap.yaml
kubectl apply -f kubernetes/http-configmap.yaml
kubectl apply -f kubernetes/kafka-configmap.yaml
kubectl apply -f kubernetes/timed-guards.yaml
```

### Deploy the platform (~4min)
```sh
# from the hmda-platform folder
helm upgrade --install --force \
--namespace=cass-operator \
--values=kubernetes/kind/hmda-platform/helm/values.yaml \
--set image.repository=dtr-registry.cfpb.gov/hmda/hmda-platform \
--set image.tag=kind-tls-build \
hmda-platform \
kubernetes/kind/hmda-platform/helm       
```

### Test deployment
```sh
# deploy centos pod
kubectl apply -f centos-pod.yaml

# exec into pod
kubectl -n cass-operator  exec -it pod/$(kubectl get pods -n cass-operator | grep centos | awk '{print $1}' | head -n 1)  -- bash

# Check the platform status
curl hmda-platform:8080
curl hmda-platform:8558/cluster/members
``
