## HMDA Newmen K8 Cron Jobs

### Create the job using helm
helm install \
--name=hmda \
--namespace=newman-data-browser \
--set env.HOST_DATA_BROWSER=<data browser host URL> \
.

### View the k8 cronjobs

kubectl get cronjobs


