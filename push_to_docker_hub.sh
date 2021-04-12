#!/usr/bin/env bash

set -e


if [[ "$TRAVIS_BRANCH" == "push-to-dockerhub-from-travis" ]]; then
    echo "Pushing master branch to latest tag on Docker Hub"
    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD ;
    sbt -batch hmda-platform/docker:publishLocal
    docker tag hmda/hmda-platform:latest hmda/hmda-platform:latest
    docker push hmda/hmda-platform:latest
fi
if [ ! -z "${TRAVIS_TAG}" ]; then
    echo "Pushing ${TRAVIS_TAG} tag on Docker Hub"
    docker tag hmda/hmda-platform:latest hmda/hmda-platform:${TRAVIS_TAG}
    docker push hmda/hmda-platform:${TRAVIS_TAG}
fi