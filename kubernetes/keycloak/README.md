# Keycloak Deployment

Keycloak is deployed using the [helm chart by Codecentric](https://github.com/codecentric/helm-charts/tree/master/charts/keycloak) and the `values.yaml` contained in this directory.

In order to deploy Keycloak first add the codecentric helm repo:
`helm repo add codecentric https://codecentric.github.io/helm-charts`

Then install keycloak adding the psql address in the command:

`helm install codecentric/keycloak -f kubernetes/keycloak/values.yaml --name keycloak --set keycloak.persistence.dbHost=<psql host>`

Then add the Ambassador Service YAML:

`kubectl apply -f keycloak-ambassador.yaml`