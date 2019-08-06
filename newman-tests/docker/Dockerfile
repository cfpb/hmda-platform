FROM node:8.16.0-jessie as build-stage

RUN mkdir -p /home/newman-trials/scripts/
RUN mkdir -p /home/newman-trials/hmda-filing/
RUN mkdir -p /home/newman-trials/data-browser/
WORKDIR /home/newman-trials

COPY dev/hmda-filing/*  hmda-filing/
COPY dev/data-browser/*  data-browser/
COPY scripts/*  scripts/
COPY newman-pak/newman-4.5.1.tgz  .
RUN npm i  --prefix ./ newman-4.5.1.tgz
RUN chmod -R 755 /home/newman-trials
