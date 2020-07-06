# Newman Jenkins API Testing


## Build Node Docker Image:

* From the newman-tests directory build and push the following image to generate a docker container that can execute newman test scripts:


`docker build  . -t <docker-host>/<image-name>:<image-tag> -f <path to Dockerfile>`

`docker push  <docker-host>/<image-name>:<image-tag>`


* From the kubernetes/newman directory create a K8 cron job that references this image

`kubectl config use-context <dev-context>`

```sh
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

### Newman as a docker container
* Run docker container
```sh
docker run -it -e KC_UN=$KC_UN -e KC_PW=$KC_PW -e KC_URL=$KC_URL -e KC_CLIENT_ID=$KC_CLIENT_ID \
-e HOST_FILING=$HOST_FILING -e HOST_ADMIN=$HOST_ADMIN -e HOST_PUBLIC=$HOST_PUBLIC -e HMDA_ENV=$HMDA_ENV \
-e MM_HOOK=$MM_HOOK hmda/newman-automation:latest /bin/sh
```
* Create newman bank
```
curl --location --request PUT $HOST_ADMIN'/institutions' \
--header 'Content-Type: application/json' \
--data-raw '{
    "activityYear": 2019,
    "lei": "NEWMANLEITEST1234678",
    "agency": 9,
    "institutionType": -1,
    "institutionId2017": "",
    "taxId": "84-7652134",
    "rssd": -1,
    "emailDomains": [
        "newmantest.com"
    ],
    "respondent": {
        "name": "Newman Bank",
        "state": "",
        "city": ""
    },
    "parent": {
        "idRssd": -1,
        "name": ""
    },
    "assets": 5,
    "otherLenderCode": -1,
    "topHolder": {
        "idRssd": -1,
        "name": ""
    },
    "hmdaFiler": true,
    "quarterlyFiler": true,
    "quarterlyFilerHasFiledQ1": false,
    "quarterlyFilerHasFiledQ2": false,
    "quarterlyFilerHasFiledQ3": false
}'
```
* Run newmantest
```sh
 ./scripts/newmanFiling.sh $KC_UN $KC_PW $KC_URL $KC_CLIENT_ID $HOST_FILING $HOST_ADMIN \
 $HOST_PUBLIC $HMDA_ENV $MM_HOOK $NEWMAN_NOTIFY
```

### Running via Docker Compose
You can use `docker-compose` to run a single Newman filing test. If the `HOST_FILING` url is set to `http://localhost:8080`, the script will bypess getting an authentication token.
```sh
docker-compose up
```
