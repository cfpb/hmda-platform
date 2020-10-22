* Install
```
kubectl apply -f kubernetes/redis/hmda-redis-claim.yaml

helm upgrade --install --force \
--set image.tag=latest \
--set cluster.enabled=true \
--set serviceAccount.create=true \
--set serviceAccount.name=hmda-redis \
--set usePassword=XXXX \
--set master.persistence.enabled=true \
--values=kubernetes/redis/values.yaml  \
hmda-redis \
stable/redis
```
* Uninstall
```
helm delete --purge hmda-redis
kubectl delete -f kubernetes/redis/hmda-redis-claim.yaml 
```
