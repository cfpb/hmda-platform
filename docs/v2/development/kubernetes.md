## CI/CD in Kubernetes (local)

To build and run the application in Kubernetes (local development), the following steps must be taken:

1. Make sure that [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/) is installed and configured for your system
2. Make sure that [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) is installed and configured. When properly
installed, you should be able to do `minikube dashboard` to open up the `kubernetes` cluster dashboard in your browser. Make sure that
`kubectl` is properly configured to point to `minikube` when working in local development mode. `Minikube` should have a minimum of 6 GB of RAM
to be able to run all the necessary containers for the `HMDA Platform`.
3. Make sure that [Helm](https://helm.sh/) is installed, as well as Tiller, the server side component.
4. Add credentials for Cassandra

```shell
kubectl create secret generic cassandra-credentials --from-literal=cassandra.username=<username> --from-literal=cassandra.password=<password>
```

5. Install the `Jenkins` Helm Chart, as follows:

* Create namespace for `Jenkins`: 

```bash
kubectl apply -f kubernetes/jenkins-namespace.yaml
```

* Bind `default` service account to cluster admin role: 

```bash
kubectl apply -f kubernetes/cluster-admin-rolebinding.yaml
```

* First, make sure the `Helm` repo is up to date:

```shell
helm repo update
```

List Helm Charts installed:

```shell
helm list
```

In some cases, this command will fail with a permissions error. In that case, run the following:

```shell
kubectl create clusterrolebinding add-on-cluster-admin --clusterrole=cluster-admin --serviceaccount=kube-system:default
```

If the `helm list` command doesn't work (usually with an error of "connection refused"), do the following:

```shell
kubectl --namespace=kube-system edit deployment/tiller-deploy
```

And change the `automountServiceAccountToken` to `true`. Save and exit

* Add Ambassador Helm Repository

```shell
helm repo add datawire https://www.getambassador.io
```

* Install Ambassador Helm Chart

```shell
helm upgrade --install --wait ambassador datawire/ambassador
```

* Create Persistent Volume for Jenkins


* Install Jenkins Chart

```shell
helm install --name jenkins -f kubernetes/jenkins-values.yaml stable/jenkins --namespace jenkins-system
```

You can access `Jenkins` by issuing `minikube service --n jenkins-system jenkins` and logging in with `admin/admin`.

Follow the on screen instructions to finalize `Jenkins` setup. When logged in, update plugins if necessary.

* Docker Hub Credentials

Add credentials in Jenkins for `Docker Hub` so that images can be pushed as part of `Jenkins` pipeline builds.

### Install Keycloak

Make sure the two secrets are created: `realm` from the file under `/kubernetes/keycloak`, and `keycloak-credentials`
with the key `password` set to the Postgres password.  Find the URL of the Postgres database, and then install Keycloak with 
this command:

```bash
helm upgrade -i -f kubernetes/keycloak/values.yaml keycloak stable/keycloak --set keycloak.persistence.dbHost="<db URL>"
```

### Install Institutions API

The institutions API chart has two secret dependencies: `cassandra-credentials` (which is also needed by the hmda-platform)
and `inst-postgres-credentials`.  These keys need to be created if they don't already exist.  
* Cassandra secret keys: `cassandra.username` and `cassandra.password` 
* InstApi secret keys: `host`, `username` and `password`

If running locally, the Institutions API must be pointed at a local instance of Cassandra.  This can be done in the install command:
```bash
helm upgrade -i -f kubernetes/institutions-api/values.yaml institutions-api ./kubernetes/institutions-api/ --set cassandra.hosts="<Docker IP>"
```
If deploying to HMDA4, run the above command without the `set` flag and it will connect automatically.

If deploying and pointing to a new database, run with the flag `--set postgres.create-schema="true"`


6. OPTIONAL: Install [Istio](https://istio.io/) Service Mesh

* Install Istio with Helm. Download the Istio distribution and run from the Istio root path:

```bash
helm install install/kubernetes/helm/istio --name istio --namespace istio-system
```

* Make sure automatic sidecar injection is supported: 

```bash
kubectl api-versions | grep admissionregistration
```

* Enable automatic sidecar injection in the `default` namespace: 

```bash
kubectl label namespace default istio-injection=enabled
``` 

To check that this operation succeeded: 

```bash
kubectl get namespace -L istio-injection
```
