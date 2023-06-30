# Keycloak

## Helm Install 
Add bitnami repo
```bash 
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Install the keycloak chart.
Database hostname must be set in-line:
```bash
helm upgrade --install keycloak bitnami/keycloak --version 13.1.0 \
    --values keycloak21_values.yaml \
    --set externalDatabase.host="$DB_HOST"
```

## Keycloak/Ambassador Service
Ambassador/Emissary use a service to route traffic:

Apply the applicable service(s) for your target env
```bash
kubectl apply -f keycloak-ambassador/XXX.keycloak-ambassador.service.yaml
```

## Theme provider and Spi Provider images
See the README's in the `theme-provider` and `spi-provider` sub-folders for instructions on how to build those images.
