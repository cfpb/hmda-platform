FROM node:8.16.0-jessie as build-stage


COPY newman-pak/*  .
COPY dev/hmda-filing/*  hmda-filing/
COPY dev/data-browser/*  data-browser/
COPY scripts/*  scripts/

RUN JQ_URL="https://circle-downloads.s3.amazonaws.com/circleci-images/cache/linux-amd64/jq-latest" \
  && curl --silent --show-error --location --fail --retry 3 --output /usr/bin/jq $JQ_URL \
  && chmod +x /usr/bin/jq \
  && mkdir -p /home/newman-trials/scripts/ \
  && mkdir -p /home/newman-trials/hmda-filing/ \
  && mkdir -p /home/newman-trials/data-browser/ \
  && npm i  --prefix ./ newman-4.5.6.tgz \
  && chmod -R 755 /home/newman-trials \
  && rm -rf newman-4.5.6.tgz \

WORKDIR /home/newman-trials


