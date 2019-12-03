## HMDA Newmen K8 Cron Jobs

### Create the job using helm
helm upgrade --install --force  --name=hmda --namespace=newman  \
--set env.KC_UN=$KC_UN \
--set env.KC_PW=$KC_PW \
--set env.KC_URL=$KC_URL \
--set env.KC_CLIENT_ID=$KC_CLIENT_ID \
--set env.HOST_FILING=$HOST_FILING \
--set env.HOST_ADMIN=$HOST_ADMIN \
--set env.HOST_PUBLIC=$HOST_PUBLIC \
--set env.HMDA_ENV=$HMDA_ENV \
--set env.MM_HOOK=$MM_HOOK .


### View the k8 cronjobs

kubectl get cronjobs


### Delete Cron jobs 

kubectl delete -n newman cronjob --all

