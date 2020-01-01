## HMDA Newmen K8 Cron Jobs

### Create the job using helm
helm install  --name=allclear-cronjobs --namespace=automated-jobs  \
--set env.KC_UN=$KC_UN \
--set env.KC_PW=$KC_PW_PROD \
--set env.KC_URL=$KC_URL_PROD \
--set env.KC_CLIENT_ID=$KC_CLIENT_ID \
--set env.HOST_FILING=$HOST_FILING_PROD \
--set env.HOST_ADMIN=$HOST_ADMIN_PROD \
--set env.HOST_PUBLIC=$HOST_PUBLIC_PROD \
--set env.HMDA_ENV=$HMDA_ENV_PROD \
--set env.MM_HOOK=$MM_HOOK \
--set env.ALT_HOOK=$ALT_HOOK .


### View the k8 cronjobs

kubectl get cronjobs


### Delete Cron jobs 

kubectl delete -n newman cronjob --all

