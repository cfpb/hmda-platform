# Keycloak Deployment

Keycloak is deployed using the [helm chart by Codecentric](https://github.com/codecentric/helm-charts/tree/master/charts/keycloak) and the `values.yaml` contained in this directory.

In order to deploy Keycloak first add the codecentric helm repo:
`helm repo add codecentric https://codecentric.github.io/helm-charts`

Then install keycloak adding the psql address in the command:

```sh
helm upgrade --install --force --namespace=default \
--values=kubernetes/keycloak/values.yaml \
--set keycloak.persistence.dbHost=<psql-host> \
--set keycloak.username=<psql-username> \
--set keycloak.password=<psql-password> \
keycloak stable/keycloak
```

Then add the Ambassador Service YAML:

`kubectl apply -f keycloak-ambassador.yaml`