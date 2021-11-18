* Install
```
kubectl create secret generic redis-tls-secret --from-file=tests/tls/redis.crt --from-file=tests/tls/redis.key --from-file=tests/tls/ca.crt --from-file=tests/tls/redis.dh --from-file=tests/tls/server.crt
secret/redis-tls-secret created

kubectl create secret generic redis-password --from-literal=redis-password='XXXX'

kubectl apply -f kubernetes/redis/hmda-redis-claim.yaml

# Add Redis helm repo
helm repo add bitnami https://charts.bitnami.com/bitnami

helm upgrade --install --force \
--set image.tag=6.2.6-debian-10-r21 \
--set auth.enabled=true \
--set auth.existingSecret=redis-password \
--set master.persistence.enabled=true \
--set persistence.existingClaim=hmda-redis-claim \
--set tls.enabled=true \
--set tls.authClients=false \
--set tls.certificatesSecret="redis-tls-secret" \
--set tls.certFilename=redis.crt \
--set tls.certKeyFilename=redis.key \
--set tls.certCAFilename=ca.crt \
--set tls.dhParamsFilename=redis.dh \
--set metrics.extraArgs.skip-tls-verification=true \
--set metrics.enabled=true \
--set master.livenessProbe.enabled=false \
-f kubernetes/redis/values.yaml \
hmda-redis \
bitnami/redis

```
* Uninstall
```
helm delete hmda-redis
kubectl delete -f kubernetes/redis/hmda-redis-claim.yaml 
```
