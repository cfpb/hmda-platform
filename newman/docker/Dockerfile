FROM node:8.16.0-jessie as build-stage


RUN JQ_URL="https://circle-downloads.s3.amazonaws.com/circleci-images/cache/linux-amd64/jq-latest" \
  && curl --silent --show-error --location --fail --retry 3 --output /usr/bin/jq $JQ_URL \
  && chmod +x /usr/bin/jq

RUN mkdir -p /home/newman-trials/scripts/
RUN mkdir -p /home/newman-trials/hmda-filing/
RUN mkdir -p /home/newman-trials/data-browser/
WORKDIR /home/newman-trials

COPY dev/hmda-filing/*  hmda-filing/
COPY dev/data-browser/*  data-browser/
COPY scripts/*  scripts/
COPY newman-pak/*  .
RUN npm i  --prefix ./ newman-4.5.6.tgz
RUN chmod -R 755 /home/newman-trials
RUN rm -rf newman-4.5.6.tgz
