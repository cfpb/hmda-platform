FROM node:lts-alpine3.11
ARG DOCKER_TAG="latest"


# RUN apt-get update && \
#     apt-get install --no-install-recommends -y \
#     ca-certificates \
#     curl \
#     jq
#
# RUN npm install -g newman@4.5.6

ENV NEWMAN_VERSION=4.5.6

# Set environment variables
ENV LC_ALL="en_US.UTF-8" LANG="en_US.UTF-8" LANGUAGE="en_US.UTF-8" ALPINE_NODE_REPO="oznu/alpine-node"

# Bail out early if NODE_VERSION is not provided
RUN if [ ! $(echo $NEWMAN_VERSION | grep -oE "^[0-9]+\.[0-9]+\.[0-9]+$") ]; then \
        echo "\033[0;31mA valid semver Newman version is required in the NEWMAN_VERSION build-arg\033[0m"; \
        exit 1; \
    fi && \
    # Install Newman globally
    npm install --global newman@${NEWMAN_VERSION};

RUN apk add --no-cache --upgrade bash curl jq

RUN mkdir -p /home/newman-trials

WORKDIR /home/newman-trials

COPY newman-pak/*  .
COPY tests/hmda-filing/  tests/hmda-filing/
COPY scripts/  scripts/
