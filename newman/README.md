# Newman Jenkins API Testing


## Build Node Docker Image:

* From the newman-tests directory build and push the following image to generate a docker container that can execute newman test scripts:


`docker build  . -t <docker-host>/<image-name>:<image-tag> -f <path to Dockerfile>`

`docker push  <docker-host>/<image-name>:<image-tag>`


* From the kubernetes/newman directory create a K8 cron job that references this image 

`kubectl config use-context <dev-context>`

```
helm install  --namespace=newman  \
--set env.KC_UN=$KC_UN \
--set env.KC_PW=$KC_PW \
--set env.KC_URL=$KC_URL \
--set env.KC_CLIENT_ID=$KC_CLIENT_ID \
--set env.HOST_FILING=$HOST_FILING \
--set env.HOST_ADMIN=$HOST_ADMIN \
--set env.HOST_PUBLIC=$HOST_PUBLIC \
--set env.HMDA_ENV=$HMDA_ENV \
--set env.MM_HOOK=$MM_HOOK .
```


### delete cronjob
 `kubectl delete -n newman cronjob --all`
