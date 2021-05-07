# Building Keycloak

Keycloak is built from a modified version of the [dockerfile](https://repo1.dsop.io/dsop/opensource/keycloak/keycloak) published by Ironbank Containers. This Dockerfile is then modified to use the official OpenJDK on Dockerhub. The following changes are made to the dockerfile:

1. The `BASE_IMAGE` args at the top are modified to point to the official OpenJDK image on Dockerhub.
1. The base image `FROM` command is modified to remove the `BASE_REGISTRY` argument.
1. The package mamager in the first `RUN` command is modified to use `apt` rather than `dnf`

This Keycloak image can be built by running `docker build -t <registry>/keycloak .` from this directory.

# Deploying Keycloak

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
keycloak codecentric/keycloak
```

Then add the Ambassador Service YAML:

`kubectl apply -f kubernetes/keycloak/keycloak-ambassador.yaml`
