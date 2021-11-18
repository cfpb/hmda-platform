[Mailhog](https://artifacthub.io/packages/helm/codecentric/mailhog)    

Install
```
helm repo add codecentric https://codecentric.github.io/helm-charts
helm install mailhog codecentric/mailhog
kubectl apply -f kubernetes/mailhog/ambassador-mailhog.yaml
```
Uninstall
```
helm uninstall mailhog
```
