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

* First, make sure the `Helm` repo is up to date:

```shell
helm repo update
```

List Helm Charts installed:

```shell
helm list
```

* Add Ambassador Helm Repository

```shell
helm repo add datawire https://www.getambassador.io
```

* Install Ambassador Helm Chart

```shell
helm upgrade --install --wait ambassador datawire/ambassador
```

### Install Keycloak

Make sure the two secrets are created: `realm` from the file under `/kubernetes/keycloak`, and `keycloak-credentials`
with the key `password` set to the Postgres password.  Find the URL of the Postgres database, and then install Keycloak with 
this command:

```bash
helm upgrade -i -f kubernetes/keycloak/values.yaml keycloak stable/keycloak --set keycloak.persistence.dbHost="<db URL>"
```

### Install institutions-api
The institutions-api chart has two secret dependencies: `cassandra-credentials` (which is also needed by the hmda-platform)
and `inst-postgres-credentials`.  These keys need to be created if they don't already exist.  
* Cassandra secret keys: `cassandra.username` and `cassandra.password` 
* institutions-api secret keys: `host`, `username` and `password`

If deploying and pointing to a new database, run with the flag `--set postgres.create-schema="true"`
```bash

kubectl apply -f inst-postgres-credentials.yaml 

helm upgrade --install --force \
--namespace=default \
--values=kubernetes/institutions-api/values.yaml \
--set image.repository=hmda/institutions-api \
--set image.tag=latest \
--set postgresql.enabled=false \
institutions-api \
kubernetes/institutions-api
```
### Install modified-lar
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/modified-lar/values.yaml \
--set image.repository=hmda/modified-lar
--set image.tag=latest \
--set image.pullPolicy=Always \
modified-lar \
kubernetes/modified-lar
```
### Install hmda-data-publisher
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-data-publisher/values.yaml \
--set image.tag=latest \
--set image.pullPolicy=Always \
hmda-data-publisher \
kubernetes/hmda-data-publisher
```
### Install hmda-analytics
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-analytics/values.yaml \
--set image.tag=latest \
--set image.pullPolicy=Always \
hmda-analytics \
kubernetes/hmda-analytics
```
### Install hmda-platform
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-platform/values.yaml 
--set image.tag=latest 
--set image.pullPolicy=Always \
hmda-platform \
kubernetes/hmda-platform
```
### Install check-digit
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/check-digit/values.yaml \
--set image.repository=hmda/check-digit \
--set image.tag=latest \
check-digit \
kubernetes/check-digit
```
### Install irs-publisher
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/irs-publisher/values.yaml \
--set image.repository=/hmda/irs-publisher \
--set image.tag=latest \
irs-publisher \
kubernetes/irs-publisher
```
### Install hmda-reporting
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-reporting/values.yaml \
--set image.repository=hmda/hmda-reporting \
--set image.tag=latest \
hmda-reporting \
kubernetes/hmda-reporting
```
### Install ratespread-calculator
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/ratespread-calculator/values.yaml \
--set image.tag=latest \
--set image.pullPolicy=Always \
ratespread-calculator \
kubernetes/ratespread-calculator
```
### Install hmda-data-browser-api
```bash
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-data-browser-api/values.yaml \
--set image.repository=hmda/hmda-data-browser-api \
--set image.tag=latest \
hmda-data-browser-api \
kubernetes/hmda-data-browser-api
```
### Install email-service
```
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/email-service/values.yaml \
--set image.repository=hmda/email-service \
--set image.tag=latest \
email-service \
kubernetes/email-service
```
### Install hmda-auth
```
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-auth/values.yaml \
--set image.repository=hmda/hmda-auth \
--set image.tag=latest \
hmda-auth \
kubernetes/hmda-auth
```
### Install hmda-quaterly-data-service
```
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-quarterly-data-service/values.yaml \
--set image.repository=hmda/hmda-quarterly-data-service \
--set image.tag=latest \
hmda-quarterly-data-service \
kubernetes/hmda-quarterly-data-service
```
### Install file-proxy
```
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/file-proxy/values.yaml \
--set image.repository=hmda/file-proxy \
--set image.tag=latest \
file-proxy \
kubernetes/file-proxy
```
### Install hmda-dashboard
```
helm upgrade --install --force \
--namespace=default \
--values=kubernetes/hmda-dashboard/values.yaml \
--set image.repository=hmda/hmda-dashboard \
--set image.tag=latest \
hmda-dashboard \
kubernetes/hmda-dashboard
```
