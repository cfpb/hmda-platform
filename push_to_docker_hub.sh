#!/usr/bin/env bash

set -e

echo "$TRAVIS_PULL_REQUEST_BRANCH <-- TRAVIS_PULL_REQUEST_BRANCH"
echo "$TRAVIS_BRANCH <-- TRAVIS_BRANCH"
echo "$TRAVIS_TAG <-- TRAVIS_TAG"
if [[ "$TRAVIS_PULL_REQUEST_BRANCH" == "push-to-dockerhub-from-travis" ]]; then
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