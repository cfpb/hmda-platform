## Releasing Image to ECR

> **_NOTE:_**  This document contains info on how to publish the `hmda-platform` image to ECR.
> This job, located in `ecr-push.yaml`, can **only** be run locally on your machine.

### Prerequisites 

- `act` - version >=0.2.71
  - https://nektosact.com/installation/index.html
- Logged in and authenticated to AWS

### Copy Job
At the root of the repo, run:

```shell
act -j 'release_to_ecr' --env=IMAGE_TAG=v0.1.2 --env=AWS_URL=aws.com --env-file <(aws configure export-credentials --format env)
```
Make sure to set:
- `IMAGE_TAG` 
  - The tag of what image you want to copy from Dockerhub to ECR
- `AWS_URL`
  - The URL to our AWS instance