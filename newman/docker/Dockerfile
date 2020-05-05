FROM node:lts-stretch-slim
ARG DOCKER_TAG="latest"

RUN npm install -g newman newman-reporter-html
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
    curl \
    jq

RUN mkdir -p /home/newman-trials

WORKDIR /home/newman-trials

COPY newman-pak/*  .
COPY tests/hmda-filing/  tests/hmda-filing/
COPY scripts/  scripts/
