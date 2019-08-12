## HMDA Newmen K8 Cron Jobs

### Create the job using helm
helm install \
--name=hmda \
--set env.HOST_URL=<url> \
--set env.USERNAME=<username> \
--set env.PASSWORD=<pwd> \
.


### View the k8 cronjobs

kubectl get cronjobs


