FROM node:lts-stretch-slim
ARG DOCKER_TAG="latest"


RUN apt-get update && \
    apt-get install --no-install-recommends -y \
    ca-certificates \
    curl \
    jq

RUN npm install -g newman@4.5.6

RUN mkdir -p /home/newman-trials

WORKDIR /home/newman-trials

COPY newman-pak/*  .
COPY tests/hmda-filing/  tests/hmda-filing/
COPY scripts/  scripts/
