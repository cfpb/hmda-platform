## Purpose 

In order to resolve issues with certs required for TLS/SSL connections within our clusters, these missing certs need to be copied over during pod initialization. 


## How to build/push the Dockerfile  
docker build -t 626560329871.dkr.ecr.us-east-1.amazonaws.com/hmda/hmda-init:v1.0.0 --platform=linux/amd64 .
docker push 626560329871.dkr.ecr.us-east-1.amazonaws.com/hmda/hmda-init:v1.0.0  
