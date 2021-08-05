# HMDA-Platform, Cassandra and Strimzi with TLS

**tl;dr** To run the hmda-platform with TLS we need to start an init container with the certs for Cassandra and Strimzi mounted. Then create a java keystore and mount it on the hmda-platform container.

**Future _ToDo_s:**
- Separate Cassandra, Strimzi and Platofrm to different namespace using KMS to manage certs
- Abstract the helm commands so they could be used for any environment

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

### Create namespace
```sh
kubectl create namespace hmda-kind
```

### Deploy Cassandra
```sh
# Install the operator
kubectl create -f https://raw.githubusercontent.com/k8ssandra/hmda-kind/v1.7.0/docs/user/hmda-kind-manifests.yaml --context kind-kind

# Install storageclass (may have some errors)
kubectl create -f https://raw.githubusercontent.com/k8ssandra/hmda-kind/master/operator/k8s-flavors/kind/rancher-local-path-storage.yaml --context kind-kind

# Create dc2 cluster
# This will take a few (~7 mins)
kubectl create -n hmda-kind -f dc2.yaml --context kind-kind
```

### Deploy Kafka
```sh
helm repo add strimzi https://strimzi.io/charts/

# Install the operator
# This will take a few  (~3mins)
helm install strimzi strimzi/strimzi-kafka-operator --namespace hmda-kind

# Create cluster
# This will take a few  (~3mins)
kubectl apply -f strimzi-kafka-cluster.yaml --context kind-kind --namespace hmda-kind
```

### Deploy akhq (Optional)
```sh
helm repo add akhq https://akhq.io/
cd akhq && helm install akhq . --namespace hmda-kind && cd ..
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
--namespace=hmda-kind \
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
kubectl -n hmda-kind  exec -it pod/$(kubectl get pods -n hmda-kind | grep centos | awk '{print $1}' | head -n 1)  -- bash

# Check the platform status
curl hmda-platform:8080
curl hmda-platform:8558/cluster/members
``
