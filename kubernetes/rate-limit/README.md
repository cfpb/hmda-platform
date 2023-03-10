Install
```
helm upgrade --install \
--set image.tag=latest \
--set image.repository=hmda/rate-limit \
rate-limit \
kubernetes/rate-limit 
```
