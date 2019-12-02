FROM node:8.16.0-jessie as build-stage

RUN mkdir -p /home/newman-trials/scripts/
RUN mkdir -p /home/newman-trials/hmda-filing/
RUN mkdir -p /home/newman-trials/data-browser/
WORKDIR /home/newman-filing-yearly-test

COPY dev/hmda-filing/*  hmda-filing/
COPY dev/data-browser/*  data-browser/
COPY scripts/*  scripts/
COPY newman-pak/*  .
RUN npm i  --prefix ./ newman-4.5.6.tgz
RUN chmod -R 755 /home/newman-filing-yearly-test
RUN rm -rf newman-4.5.6.tgz
