# Newman Jenkins API Testing


## Build Node Docker Image:

* From the newman-tests directory build and push the following image to generate a docker container that can execute newman test scripts:


`docker build  . -t <docker-host>/<image-name>:<image-tag> -f <path to Dockerfile>`

`docker push  <docker-host>/<image-name>:<image-tag>`


* From the kubernetes/newman directory create a K8 cron job that references this image 

`kubectl config use-context <dev-context>`

```
helm install  --name=hmda --namespace=newman 
--set env.HOST_FILING=<complete filing API host/endpoint>
 --set env.HOST_ADMIN=<complete admin API host/endpoint>
 --set env.HOST_PUBLIC=<complete public API host/endpoint>
 --set env.USERNAME_NM=<authorized username>
--set env.PASSWORD_NM=<authorized user password> .
```


## Sample script executed by cronjob

```
authToken=$(./scripts/authTokenGen.sh $USERNAME_NM $PASSWORD_NM) &&
 ./node_modules/.bin/newman run hmda-filing/hmda-filing-api-test.json -d hmda-filing/hmda-filing-api-config.json 
--env-var host_filing=$HOST_FILING
 --env-var host_admin_api=$HOST_ADMIN 
--env-var host_public=$HOST_PUBLIC 
--env-var authToken= $authToken
```