# Newman Jenkins API Testing


## Build Node Docker Image:
From the `newman-tests` directory build and push the following image to generate a docker container that can execute `newman` test scripts:


`docker build  . -t <docker-host>/<image-name>:<image-tag> -f <path to Dockerfile>`

`docker push  <docker-host>/<image-name>:<image-tag>`

## Install on Kubernetes
From the `kubernetes/newman` directory create a Kubernetes CronJob that references this image

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
```sh
kubectl delete -n newman cronjob --all
```

## Testing with Newman

### Running Neman Locally:
When running `newman` locally, running the tests will require the following variables:
```sh
HOST_FILING=
HOST_ADMIN=
HOST_PUBLIC=
NEWMAN_YEAR=
```

### Running Newman locally and Platform locally
Start the platform locally and then run `./scripts/run_one_newman.sh`.

### Running Newman locally and Platform in Docker
Start the platform with `docker-compose up hmda-platform` from the root folder of the project, then run `./scripts/run_one_newman.sh`.

## Running Neman with `docker-compose`

### Running Newman and Platform in Docker
Start with running `docker-compose up` from the root folder of the project followed by another `docker-compose up` from the `newman` folder.
